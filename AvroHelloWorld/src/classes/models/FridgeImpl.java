package classes.models;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.SaslSocketServer;
import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.avro.ipc.specific.SpecificResponder;

import sourcefiles.*;
import utility.Heartbeat;
import utility.LANIp;
import utility.NetworkDiscoveryClient;
import utility.ReplicationGenerator;

public class FridgeImpl implements FridgeProtocol {
    private List<CharSequence> inventory;
    private Transceiver client;
    private ServerProtocol proxy;
    private String id;
    private String ip;
    private Server server = null;
    private int port;
    private NeighbourData electionNeighbour;
    private boolean inElection;
    InetSocketAddress serverAddress;
    private boolean serverFound;
    private Heartbeat heartbeat;
    private Thread heartbeatThread;

    private ReplicationData repdata;

    Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
        public void uncaughtException(Thread th, Throwable ex) {
            //Catches the exceptions thrown by the heartbeat thread(indicating server wasnt found)
            System.out.println("Couldnt find server during heartbeat");
            serverAddress = new InetSocketAddress("0.0.0.0", 0);
            serverFound = false;

            //Search for server before starting election to check if original server came back online
            try {
                NetworkDiscoveryClient FindServer = new NetworkDiscoveryClient();
                serverAddress = FindServer.findServer();
                serverFound = true;
                heartbeat.setServer(serverAddress);
                heartbeat.setuserName(id);
                heartbeatThread = new Thread(heartbeat);
                heartbeatThread.setUncaughtExceptionHandler(h);
                heartbeatThread.start();
            } catch (IOException e) {
                //Server can't be found
                serverFound = false;
                heartbeat.setServer(new InetSocketAddress("0.0.0.0", 0));
                startElection();
            }
        }
    };
    private boolean isServer;

    public FridgeImpl() {
        electionNeighbour = null;
        inElection = false;
        heartbeat = new Heartbeat();
        inventory = new ArrayList<CharSequence>();
        inventory.add("Appel");
        try {
            ServerSocket s = new ServerSocket(0);
            port = s.getLocalPort();
            s.close();
            InetAddress localaddress = LANIp.getAddress();
            ip = localaddress.toString().split("/")[1];
            server = new SaslSocketServer(new SpecificResponder(FridgeProtocol.class, this), new InetSocketAddress(ip, port));
            server.start();
            System.out.println("booted FridgeServer on: " + port);
            connectToServer(true);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<CharSequence> getInventory() throws AvroRemoteException {
        return inventory;
    }

    @Override
    public CharSequence addItem(CharSequence item) throws AvroRemoteException {
        inventory.add(item);
        return item + " has been added";
    }

    @Override
    public CharSequence removeItem(CharSequence item) throws AvroRemoteException {
        boolean deletedItem = false;
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).toString().equals(item.toString())) {
                inventory.remove(i);
                if (inventory.size() == 0) {
                    try {
                        client = new SaslSocketTransceiver(serverAddress);
                        proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
                        System.out.println(proxy.notifyUsersOfEmptyFridge(id));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                deletedItem = true;
            }
        }

        if (deletedItem) {
            return item + " has been removed";
        } else {
            return item + " was not in the fridge";
        }
    }

    @Override
    public Void enter(CharSequence userName, CharSequence ip, CharSequence type) throws AvroRemoteException {
        switch (type.toString()) {
            case "light":
                repdata.connectedLights.put(userName.toString(), ip);
                break;
            case "temperature sensor":
                repdata.connectedTS.put(userName.toString(), ip);
                break;
            case "fridge":
                repdata.connectedFridges.put(userName.toString(), ip);
                break;
            case "user":
                repdata.connectedUsers.put(userName.toString(), ip);
                repdata.userlocation.put(userName.toString(), false);
                break;
        }
        return null;
    }

    @Override
    public Void leave(CharSequence userName, CharSequence type) throws AvroRemoteException {
        switch (type.toString()) {
            case "light":
                repdata.connectedLights.remove(userName.toString());
                break;
            case "temperature sensor":
                repdata.connectedTS.remove(userName.toString());
                break;
            case "fridge":
                repdata.connectedFridges.remove(userName.toString());
                break;
            case "user":
                repdata.connectedUsers.remove(userName.toString());
                repdata.userlocation.remove(userName.toString());
                break;
        }
        return null;
    }

    @Override
    public Void enterHouse(CharSequence userName) throws AvroRemoteException {
        // TODO Auto-generated method stub
        repdata.userlocation.put(userName.toString(), false);
        return null;
    }

    @Override
    public Void leaveHouse(CharSequence userName) throws AvroRemoteException {
        // TODO Auto-generated method stub
        repdata.userlocation.put(userName.toString(), true);
        return null;
    }

    @Override
    public Void updateTemperature(TemperatureAggregate temperature) throws AvroRemoteException {
        int index = 0;
        String toCompare = temperature.getRecord().getTime().toString();
        for (TemperatureAggregate x : repdata.getTemperatures()) {
            if (toCompare.contentEquals(x.getRecord().getTime().toString())) {
                break;
            }
            index++;
        }
        if (index == repdata.getTemperatures().size()) {
            //temperatureaggregate doestn exist yet
            repdata.getTemperatures().add(temperature);
        } else {
            repdata.getTemperatures().set(index, temperature);
        }
        return null;
    }

    @Override
    public CharSequence addNeighbour(CharSequence neighbourIp, CharSequence neighbourType) throws AvroRemoteException {
    	InetSocketAddress test = new InetSocketAddress(this.ip, this.port);
    	String[] xxx = neighbourIp.toString().split(",");
    	InetSocketAddress newneighbour = new InetSocketAddress(xxx[0], Integer.parseInt(xxx[1]));
    	if(newneighbour.equals(test)){
    		electionNeighbour = null;
    	}else{
	        System.out.println("changing current neighbour to: " + neighbourIp);
	        electionNeighbour = new NeighbourData(neighbourIp, neighbourType);
    	}	
        return "Neighbour added to Fridge";
    }


    public void setNewServer(CharSequence serverIp) throws AvroRemoteException {
        // new ServerImpl(repdata);
        System.out.println("got setNewServer");
        if (!isServer) {
            System.out.println("creating new server");
            this.isServer = true;
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    ServerImpl tempServer = new ServerImpl(repdata, id);
                    while (tempServer.isStayOpen()) {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {

                        }
                    }
                    isServer = false;
                    serverFound = false;
                    connectToServer(true);
                }
            });
        } else {
        }
    }

    @Override
    public Void sendElectionMessage(CharSequence previousId) throws AvroRemoteException {
        System.out.println("received electionMessage");
        int ownId = Integer.parseInt(id);
        int incId = Integer.parseInt(previousId.toString());
        String[] electionNeighbourIpValue = electionNeighbour.getIp().toString().split(",");
        if (incId > ownId) {
            System.out.println("inc id is bigger than mine");
            inElection = true;
            try {
                Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(electionNeighbourIpValue[0]), Integer.parseInt(electionNeighbourIpValue[1])));
                switch (electionNeighbour.getType().toString()) {
                    case "fridge":
                        FridgeProtocol fridgeProxy = (FridgeProtocol) SpecificRequestor.getClient(FridgeProtocol.class, client);
                        fridgeProxy.sendElectionMessage(previousId);
                        break;
                    case "user":
                        UserProtocol userProxy = (UserProtocol) SpecificRequestor.getClient(UserProtocol.class, client);
                        userProxy.sendElectionMessage(previousId);
                        break;
                }
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (incId < ownId) {
            System.out.println("inc id is smaller than mine");
            if (inElection == false) {
                inElection = true;
                try {
                    Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(electionNeighbourIpValue[0]), Integer.parseInt(electionNeighbourIpValue[1])));
                    switch (electionNeighbour.getType().toString()) {
                        case "fridge":
                            FridgeProtocol fridgeProxy = (FridgeProtocol) SpecificRequestor.getClient(FridgeProtocol.class, client);
                            fridgeProxy.sendElectionMessage(id);
                            break;
                        case "user":
                            UserProtocol userProxy = (UserProtocol) SpecificRequestor.getClient(UserProtocol.class, client);
                            userProxy.sendElectionMessage(id);
                            break;
                    }
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("I have the highest ID");
            setNewServer(ip + "," + port);
            inElection = false;
            try {
                Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(electionNeighbourIpValue[0]), Integer.parseInt(electionNeighbourIpValue[1])));
                switch (electionNeighbour.getType().toString()) {
                    case "fridge":
                        System.out.println("sending electedMessage to fridge");
                        FridgeProtocol fridgeProxy = (FridgeProtocol) SpecificRequestor.getClient(FridgeProtocol.class, client);
                        fridgeProxy.sendElectedMessage(id, ip + "," + port);
                        break;
                    case "user":
                        System.out.println("sending electedMessage to user");
                        UserProtocol userProxy = (UserProtocol) SpecificRequestor.getClient(UserProtocol.class, client);
                        userProxy.sendElectedMessage(id, ip + "," + port);
                        break;
                }
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public Void sendElectedMessage(CharSequence electedId, CharSequence electedIp) throws AvroRemoteException {
        System.out.println("received ELECTED id");
        if (!electedId.toString().equalsIgnoreCase(id.toString())) {
            inElection = false;
            String[] electionNeighbourIpValue = electionNeighbour.getIp().toString().split(",");
            try {
                Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(electionNeighbourIpValue[0]), Integer.parseInt(electionNeighbourIpValue[1])));
                switch (electionNeighbour.getType().toString()) {
                    case "fridge":
                        FridgeProtocol fridgeProxy = (FridgeProtocol) SpecificRequestor.getClient(FridgeProtocol.class, client);
                        fridgeProxy.sendElectedMessage(electedId, electedIp);
                        break;
                    case "user":
                        UserProtocol userProxy = (UserProtocol) SpecificRequestor.getClient(UserProtocol.class, client);
                        userProxy.sendElectedMessage(electedId, electedIp);
                        break;
                }
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        connectToServer(false);
        return null;
    }

    private void connectToServer(boolean setId) {
        System.out.println("serverFound status: " + serverFound);
        while (!serverFound) {
            try {
                System.out.println("We are looking for a server");
                NetworkDiscoveryClient FindServer = new NetworkDiscoveryClient();
                serverAddress = FindServer.findServer();
                serverFound = true;
           	    client = new SaslSocketTransceiver(serverAddress);
                proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
                repdata = ReplicationGenerator.generateReplica(proxy.getReplication());
                if(setId){
                	//Need a new id(first join or after original server came back online)
                    id = proxy.enter("fridge", ip + "," + port).toString();              	
                }
                client.close();
                heartbeat.setServer(serverAddress);
                heartbeat.setuserName(id);
                heartbeatThread = new Thread(heartbeat);
                heartbeatThread.setUncaughtExceptionHandler(h);
                heartbeatThread.start();

            } catch (IOException e) {
                //Server can't be found
                serverFound = false;
                heartbeat.setServer(new InetSocketAddress("0.0.0.0", 0));
            }
        }
        System.out.println("connectToServer done");
    }

    @Override
    public NeighbourData getNeighbour() throws AvroRemoteException {
        return electionNeighbour;
    }

    @Override
    public Void clearNeighbour() throws AvroRemoteException {
        electionNeighbour = null;
        return null;
    }

    @Override
    public CharSequence updateRepDataNeighbours(Map<CharSequence, NeighbourData> neighbourList, CharSequence lastNeighbourId) throws AvroRemoteException {
        repdata.neighbourList = neighbourList;
        repdata.lastNeighbourId = lastNeighbourId;
        return "Neighbours updated";
    }

    @Override
    public CharSequence updateRepDataIdCounter(int idCounter) throws AvroRemoteException {
        repdata.idCounter = idCounter;
        return "IdCounter updated";
    }

    private void startElection() {
    	if(inElection || serverFound || isServer){
    		return;
    	}
        if (electionNeighbour != null) {
            inElection = true;
            String[] electionNeighbourIpValue = electionNeighbour.getIp().toString().split(",");
            try {
                Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(electionNeighbourIpValue[0]), Integer.parseInt(electionNeighbourIpValue[1])));
                switch (electionNeighbour.getType().toString()) {
                    case "fridge":
                        FridgeProtocol fridgeProxy = (FridgeProtocol) SpecificRequestor.getClient(FridgeProtocol.class, client);
                        fridgeProxy.sendElectionMessage(id);
                        break;
                    case "user":
                        UserProtocol userProxy = (UserProtocol) SpecificRequestor.getClient(UserProtocol.class, client);
                        userProxy.sendElectionMessage(id);
                        break;
                }
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                setNewServer(ip + "," + port);
            } catch (AvroRemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
