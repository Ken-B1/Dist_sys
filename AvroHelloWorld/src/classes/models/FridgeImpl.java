package classes.models;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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
    private List<String> previousNeighbour;
    private List<String> nextNeighbour;
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
            try{
            	Thread.sleep(1000);
            } catch(Exception e){}
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
        previousNeighbour = new ArrayList<String>();
        nextNeighbour = new ArrayList<String>();
        inElection = false;
        heartbeat = new Heartbeat();
        inventory = new ArrayList<CharSequence>();
        inventory.add("Appel");
        connectToServer();
        try {
            ServerSocket s = new ServerSocket(0);
            port = s.getLocalPort();
            s.close();
            InetAddress localaddress = LANIp.getAddress();
            ip = localaddress.toString().split("/")[1];
            client = new SaslSocketTransceiver(serverAddress);
            proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
            repdata = ReplicationGenerator.generateReplica(proxy.getReplication());
            server = new SaslSocketServer(new SpecificResponder(FridgeProtocol.class, this), new InetSocketAddress(ip, port));
            server.start();
            id = proxy.enter("fridge", ip + "," + port).toString();
            heartbeat.setuserName(id);
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
                    System.out.println(proxy.notifyUsersOfEmptyFridge(id));
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
    public CharSequence addNeighbour(CharSequence neighbourId, CharSequence neighbourIp, CharSequence neighbourType, boolean isNextNeighbour) throws AvroRemoteException {
        if (isNextNeighbour) {
            nextNeighbour.clear();
            nextNeighbour.add(0, neighbourId.toString());
            nextNeighbour.add(1, neighbourIp.toString());
            nextNeighbour.add(2, neighbourType.toString());
        } else {
            previousNeighbour.clear();
            previousNeighbour.add(0, neighbourId.toString());
            previousNeighbour.add(1, neighbourIp.toString());
            previousNeighbour.add(2, neighbourType.toString());
        }
        return "Neighbour added to Fridge";
    }

    public Void setNewServer(CharSequence serverIp) throws AvroRemoteException {
        if (serverIp.toString().equalsIgnoreCase(ip + "," + port)) {
        	this.isServer = true;
           // new ServerImpl(repdata);
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                	 ServerImpl tempServer = new ServerImpl(repdata);
                     while(tempServer.isStayOpen()){
                     	try{
                     		Thread.sleep(1000);
                     	} catch(Exception e){
                     		
                     	}
                     }
                     isServer = false;
                     serverFound = false;
                     connectToServer();
                }
            });
        }
        return null;
    }

    @Override
    public Void sendElectionMessage(CharSequence previousId) throws AvroRemoteException {
        System.out.println("received electionMessage");
        this.heartbeat.setuserName("");
        if (nextNeighbour.size() > 0) {
        int ownId = Integer.parseInt(id);
        int incId = Integer.parseInt(previousId.toString());
        if (incId > ownId) {
            System.out.println("inc id is bigger than mine");
            inElection = true;
            String[] nextNeighbourIpValue = nextNeighbour.get(1).split(",");
            try {
                Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(nextNeighbourIpValue[0]), Integer.parseInt(nextNeighbourIpValue[1])));
                switch (nextNeighbour.get(2)) {
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
                String[] nextNeighbourIpValue = nextNeighbour.get(1).split(",");
                try {
                    Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(nextNeighbourIpValue[0]), Integer.parseInt(nextNeighbourIpValue[1])));
                    switch (nextNeighbour.get(2)) {
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
            String[] nextNeighbourIpValue = nextNeighbour.get(1).split(",");
            try {
                Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(nextNeighbourIpValue[0]), Integer.parseInt(nextNeighbourIpValue[1])));
                switch (nextNeighbour.get(2)) {
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
        }
        return null;
    }

    @Override
    public Void sendElectedMessage(CharSequence electedId, CharSequence electedIp) throws AvroRemoteException {
        System.out.println("received ELECTED id");
        //TODO if nakijken, nog nodig?
        if (!electedId.toString().equalsIgnoreCase(id.toString())) {
            //setNewServer(electedIp);
            inElection = false;
            String[] nextNeighbourIpValue = nextNeighbour.get(1).split(",");
            try {
                Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(nextNeighbourIpValue[0]), Integer.parseInt(nextNeighbourIpValue[1])));
                switch (nextNeighbour.get(2)) {
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
            //TODO eventueel een aantal keren laten nakijken of server beschikbaar is
            connectToServer();
            if(serverFound==false){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private void connectToServer() {
    	while(!serverFound){
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
	        }
    	}
        System.out.println("connectToServer done");
    }

    @Override
    public List<CharSequence> getNeighbours() throws AvroRemoteException {
        List<CharSequence> neighbours = new ArrayList<>();
        neighbours.addAll(previousNeighbour);
        neighbours.addAll(nextNeighbour);
        return neighbours;
    }

    @Override
    public Void clearNeighbours() throws AvroRemoteException {
        previousNeighbour.clear();
        nextNeighbour.clear();
        return null;
    }

    @Override
    public CharSequence updateRepDataNeighbours(List<CharSequence> firstNeighbour, List<CharSequence> lastNeighbour) throws AvroRemoteException {
        repdata.firstNeighbour = firstNeighbour;
        repdata.lastNeighbour = lastNeighbour;
        return "Neighbours updated";
    }

    @Override
    public CharSequence updateRepDataIdCounter(int idCounter) throws AvroRemoteException {
        repdata.idCounter = idCounter;
        return "IdCounter updated";
    }

    private void startElection() {
        if (nextNeighbour.size() > 0) {
            inElection = true;
            String[] nextNeighbourIpValue = nextNeighbour.get(1).split(",");
            try {
                Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(nextNeighbourIpValue[0]), Integer.parseInt(nextNeighbourIpValue[1])));
                switch (nextNeighbour.get(2)) {
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
