package classes.models;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.AvroRuntimeException;
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
import utility.TemperatureMeasurementRecord;

public class UserImpl implements UserProtocol {
    private Scanner keyboard = new Scanner(System.in);
    private String id;
    private String ip;
    private int portNumber;
    private Transceiver client;
    ServerProtocol proxy;
    private boolean serverFound;
    private Heartbeat heartbeat;
    private Thread heartbeatThread;
    private NeighbourData electionNeighbour;
    private boolean inElection;
    InetSocketAddress serverAddress;
    private Server server = null;
    public boolean isServer = false;
    public boolean inFridgeQueue = false;
    public boolean connectedToFridge = false;

    Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
        public void uncaughtException(Thread th, Throwable ex) {
            //Catches the exceptions thrown by the heartbeat thread(indicating server wasnt found)
            serverAddress = new InetSocketAddress("0.0.0.0", 0);
            serverFound = false;
      
            //Search for server before starting election to check if original server came back online
            try {
                NetworkDiscoveryClient FindServer = new NetworkDiscoveryClient();
                serverAddress = FindServer.findServer();
                serverFound = true;
                if(!isServer){
	                heartbeat.setServer(serverAddress);
	                heartbeat.setuserName(id);
	                heartbeatThread = new Thread(heartbeat);
	                heartbeatThread.setUncaughtExceptionHandler(h);
	                heartbeatThread.start();
                }
            } catch (IOException e) {
                System.out.println("IOException happened, didnt find server");
                serverFound = false;
	            heartbeat.setServer(new InetSocketAddress("0.0.0.0", 0));
	            startElection();
            }
        }
    };

    private ReplicationData repdata;

    public UserImpl() {
        electionNeighbour = null;
        inElection = false;
        heartbeat = new Heartbeat();
        try {
            InetAddress localaddress = LANIp.getAddress();
            ip = localaddress.toString().split("/")[1];
            ServerSocket s = new ServerSocket(0);
            portNumber = s.getLocalPort();
            s.close();
            server = new SaslSocketServer(new SpecificResponder(UserProtocol.class, this), new InetSocketAddress(ip, portNumber));
            server.start();
            connectToServer(true);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestClients() {
        //Method that will request the other clients from the server and display it to this user
        if (!serverFound) {
            connectToServer(false);
        }
        try {
            Transceiver client = new SaslSocketTransceiver(serverAddress);
            ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
            List<CharSequence> clients = proxy.getClients();
            for (CharSequence x : clients) {
                String type = x.toString().split("[0-9]")[0];
                System.out.println(type + ": " + x.toString());
            }
            client.close();
            //Start the procedure of updating temperature and sending it to the server
        } catch (AvroRemoteException e) {
            System.err.println("Error joining");
            e.printStackTrace(System.err);
            System.exit(1);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestLights() {
        //Method that requests all lights from the server
        if (!serverFound) {
            connectToServer(false);
        }
        try {
            Transceiver client = new SaslSocketTransceiver(serverAddress);
            ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
            System.out.println(proxy.getLightStatuses().toString());
            client.close();
            //Start the procedure of updating temperature and sending it to the server
        } catch (AvroRemoteException e) {
            System.err.println("Error joining");
            e.printStackTrace(System.err);
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void switchLight() {
        //Method that will request to switch the status of a light
        if (!serverFound) {
            connectToServer(false);
        }
        try {
            Scanner keyboard = new Scanner(System.in);
            System.out.println("Give light name");
            String selectedType = keyboard.nextLine();

            Transceiver client = new SaslSocketTransceiver(serverAddress);
            ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
            String answer = proxy.changeLightState(selectedType).toString();
            System.out.println(answer);
            client.close();
            //Start the procedure of updating temperature and sending it to the server
        } catch (AvroRuntimeException e) {
            System.err.println("That light doesnt exist");
        } catch (AvroRemoteException e) {
            System.err.println("Error joining");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Something went wrong");
        }
    }

    public void getFridgeContent() {
        //Method that will request the content of a certain fridge
        if (!serverFound) {
            connectToServer(false);
        }
        try {
            Scanner keyboard = new Scanner(System.in);

            Transceiver client = new SaslSocketTransceiver(serverAddress);
            ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
            List<String> fridges = new ArrayList<String>();

            for (CharSequence fridge : proxy.showConnectedFridges()) {
                fridges.add(fridge.toString());
            }

            //Check if the list contains any fridges
            if (fridges.isEmpty()) {
                System.out.println("You dont have any smartfridges connected to the server.");
                return;
            }
            String fridgeName;
            do {
                System.out.println("Chose one of the following fridges:");

                for (String fridge : fridges) {
                    System.out.println(fridge);
                }
                fridgeName = keyboard.nextLine();
            } while (!fridges.contains(fridgeName));

            System.out.println("Inventory of fridge: " + fridgeName);
            for (CharSequence item : proxy.getFridgeInventory(fridgeName)) {
                System.out.println("*) " + item);
            }
            client.close();
            //Start the procedure of updating temperature and sending it to the server
        } catch (AvroRemoteException e) {
            System.err.println("Error joining");
            e.printStackTrace(System.err);
            System.exit(1);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openFridge() {
        //Method that will try to connect to a fridge
        if (!serverFound) {
            connectToServer(false);
        }
        try {
            String fridgeName = "";
            Transceiver client = new SaslSocketTransceiver(serverAddress);
            ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
            List<String> fridges = new ArrayList<String>();

            for (CharSequence fridge : proxy.showConnectedFridges()) {
                fridges.add(fridge.toString());
            }

            //Check if the list contains any fridges
            if (fridges.isEmpty()) {
                System.out.println("You dont have any smartfridges connected to the server.");
                return;
            }

            do {
                System.out.println("Chose one of the following fridges:");

                for (String fridge : fridges) {
                    System.out.println("*) "+ fridge);
                }
                System.out.println("*) exit");
                fridgeName = keyboard.nextLine();
            } while (!fridges.contains(fridgeName) && !fridgeName.equalsIgnoreCase("exit"));
            System.out.println("Checking if we can open the fridge, please wait.");
            inFridgeQueue = true;
            proxy.requestFridgeAddress(fridgeName, ip + "," + portNumber);
            client.close();
        } catch (AvroRemoteException e) {
            System.err.println("Error joining");
            //e.printStackTrace();
            if (e.getMessage().contains("An existing")) {
                inFridgeQueue = false;
                startElection();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getTemperature() {
        //Method that will request the current temperature of the house
        if (!serverFound) {
            connectToServer(false);
        }
        try {
            Transceiver client = new SaslSocketTransceiver(serverAddress);
            ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
            System.out.println(proxy.showCurrentHouseTemp());
            client.close();
        } catch (IOException e) {
            System.out.println("Could not connect to server");
        } catch (AvroRuntimeException e) {
            System.out.println("There are no temperaturemeasurements.");
        }
    }

    public void getTemperatureHistory() {
        //Method that will request the history of temperatures in the house
        if (!serverFound) {
            connectToServer(false);
        }
        try {
            Transceiver client = new SaslSocketTransceiver(serverAddress);
            ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
            Map<CharSequence, Integer> temperatures = proxy.showTempHistory();
            client.close();

            for (Entry<CharSequence, Integer> entry : temperatures.entrySet()) {
                System.out.println(entry.getKey().toString() + ": " + entry.getValue());
            }
            //Start the procedure of updating temperature and sending it to the server
        } catch (AvroRuntimeException e) {
            //No temperature sensors are added to the system
            System.out.println("Maybe you need to buy some temperature sensors.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enterHouse() {
        //Method to enter the house
        if (!serverFound) {
            connectToServer(false);
        }
        try {
            Transceiver client = new SaslSocketTransceiver(serverAddress);
            ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
            proxy.enterHouse(id);
            client.close();
        } catch (AvroRemoteException e) {
            //User hasnt joined the system yet
            try {
                Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getLocalHost(), 6789));
                ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
                id = proxy.enter("user", InetAddress.getLocalHost().getHostAddress() + "," + portNumber).toString();
                client.close();
            } catch (Exception e1) {
                System.out.println("Something went wrong while trying to join");
            }
        } catch (IOException e) {

        }
    }

    public void leaveHouse() {
        //Method to leave the house
        if (!serverFound) {
            connectToServer(false);
        }
        try {
            Transceiver client = new SaslSocketTransceiver(serverAddress);
            ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
            proxy.leaveHouse(id);
            client.close();
        } catch (AvroRemoteException e) {
            //User hasnt joined the system yet
            try {
                Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getLocalHost(), 6789));
                ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
                id = proxy.enter("user", InetAddress.getLocalHost().getHostAddress() + "," + portNumber).toString();
                client.close();
            } catch (Exception e1) {
                System.out.println("Something went wrong while trying to join");
            }
        } catch (IOException e) {

        }
    }

    @Override
    public CharSequence notifyOfEmptyFridge(CharSequence fridgeName) throws AvroRemoteException {
        System.out.println(fridgeName + " is empty!!");
        return id + " received empty fridge";
    }

    @Override
    public Void notifyUsers(CharSequence userName, CharSequence state) throws AvroRemoteException {
        System.out.println("User" + userName.toString() + state + "the house.");
        if (state.toString().contentEquals(" entered ")) {
            this.enterHouse(userName);
        } else {
            this.leaveHouse(userName);
        }
        return null;
    }

    private void connectToServer(boolean setId) {
        while (!serverFound) {
            try {
                NetworkDiscoveryClient FindServer = new NetworkDiscoveryClient();
                serverAddress = FindServer.findServer();
                serverFound = true;
           	    client = new SaslSocketTransceiver(serverAddress);
                proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
                repdata = ReplicationGenerator.generateReplica(proxy.getReplication());
                if(setId){
                	//Need a new id(first join or after original server came back online)
                    id = proxy.enter("user", ip + "," + portNumber).toString();              	
                }
                client.close();
                if(!this.isServer){
	                heartbeat.setServer(serverAddress);
	                heartbeat.setuserName(id);
	                heartbeatThread = new Thread(heartbeat);
	                heartbeatThread.setUncaughtExceptionHandler(h);
	                heartbeatThread.start();
                }
            } catch (IOException e) {
                //Server can't be found
                serverFound = false;
                heartbeat.setServer(new InetSocketAddress("0.0.0.0", 0));
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {

            }
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
        repdata.getUserlocation().put(userName.toString(), false);
        return null;
    }

    @Override
    public Void leaveHouse(CharSequence userName) throws AvroRemoteException {
        // TODO Auto-generated method stub
        repdata.getUserlocation().put(userName.toString(), true);
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

    public void printAggregate() {
        System.out.println("Users:");
        for (Entry<CharSequence, CharSequence> entry : repdata.connectedUsers.entrySet()) {
            System.out.println("    " + entry.getKey() + ": " + entry.getValue() + ", Location: " + (repdata.userlocation.get(entry.getKey()) ? "Outside" : "Inside"));
        }
        System.out.println("Lights:");
        for (Entry<CharSequence, CharSequence> entry : repdata.connectedLights.entrySet()) {
            System.out.println("    " + entry.getKey());
        }
        System.out.println("Fridges:");
        for (Entry<CharSequence, CharSequence> entry : repdata.connectedFridges.entrySet()) {
            System.out.println("    " + entry.getKey());
        }
        System.out.println("Ts:");
        for (Entry<CharSequence, CharSequence> entry : repdata.connectedTS.entrySet()) {
            System.out.println("    " + entry.getKey());
        }
        System.out.println("temperatures:");
        for (TemperatureAggregate x : repdata.temperatures) {
            System.out.println("    " + x.counter + ", (" + x.record.time.toString() + ":" + x.record.temperature + ")");
        }
        System.out.println("end________________________________________");
    }

    @Override
    public CharSequence addNeighbour(CharSequence neighbourIp, CharSequence neighbourType) throws AvroRemoteException {
    	InetSocketAddress test = new InetSocketAddress(this.ip, this.portNumber);
    	String[] xxx = neighbourIp.toString().split(",");
    	InetSocketAddress newneighbour = new InetSocketAddress(xxx[0], Integer.parseInt(xxx[1]));
    	if(newneighbour.equals(test)){
    		electionNeighbour = null;
    	}else{
	        electionNeighbour = new NeighbourData(neighbourIp, neighbourType);
    	}	
        return "Neighbour added to Fridge";
    }

    public void setNewServer(CharSequence serverIp) throws AvroRemoteException {
        if (!this.isServer) {
             this.isServer = true;
            inFridgeQueue=false;
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
        int ownId = Integer.parseInt(id);
        int incId = Integer.parseInt(previousId.toString());
        String[] electionNeighbourIpValue = electionNeighbour.getIp().toString().split(",");
        if (incId > ownId) {
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
            setNewServer(ip + "," + portNumber);
            inElection = false;
            try {
                Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(electionNeighbourIpValue[0]), Integer.parseInt(electionNeighbourIpValue[1])));
                switch (electionNeighbour.getType().toString()) {
                    case "fridge":
                        FridgeProtocol fridgeProxy = (FridgeProtocol) SpecificRequestor.getClient(FridgeProtocol.class, client);
                        fridgeProxy.sendElectedMessage(id, ip + "," + portNumber);
                        break;
                    case "user":
                        UserProtocol userProxy = (UserProtocol) SpecificRequestor.getClient(UserProtocol.class, client);
                        userProxy.sendElectedMessage(id, ip + "," + portNumber);
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
            serverFound = false;
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

    @Override
    public NeighbourData getNeighbour() throws AvroRemoteException {
        return electionNeighbour;
    }

    @Override
    public Void clearNeighbour() throws AvroRemoteException {
        electionNeighbour = null;
        return null;
    }

    private void startElection() {
    	if(inElection || serverFound || isServer){
    		return;
    	}
        if (electionNeighbour != null) {
        	System.out.println("Starting election");
            inElection = true;
            try {
                String[] electionNeighbourIpValue = electionNeighbour.getIp().toString().split(",");
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
                setNewServer(ip + "," + portNumber);
            } catch (AvroRemoteException e) {
                e.printStackTrace();
            }
        }
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

    @Override
    public CharSequence grantFridgeAccess(CharSequence fridgeIp, CharSequence fridgeName) throws AvroRemoteException {
        connectedToFridge=true;
        System.out.println("We have been granted access to the fridge.");
        String[] fridgeValue = fridgeIp.toString().split(",");
        try {
            Transceiver fridgeclient = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(fridgeValue[0]), Integer.parseInt(fridgeValue[1])));
            FridgeProtocol fridgeProxy = (FridgeProtocol) SpecificRequestor.getClient(FridgeProtocol.class, fridgeclient);
            String choice;
            do {
                System.out.println("Type one of the following commands: ");
                System.out.println("*) add (to put item in fridge)");
                System.out.println("*) take (to take item ouf of the fridge)");
                System.out.println("*) exit");
                choice = keyboard.nextLine();
                switch (choice) {
                    case "add":
                        CharSequence item;
                        System.out.print("Type the item you are putting in the fridge:");
                        item = keyboard.nextLine();
                        System.out.println(fridgeProxy.addItem(item));
                        break;
                    case "take":
                        List<CharSequence> items = new ArrayList<CharSequence>();
                        for (CharSequence inventoryItem : fridgeProxy.getInventory()) {
                            items.add(inventoryItem.toString());
                        }
                        if (items.size() > 0) {
                            String selectedItem;
                            System.out.println("Chose one of the following items to take out of the fridge:");
                            do {
                                for (CharSequence i : items) {
                                    System.out.println("*) " + i);
                                }
                                System.out.println("*) exit");
                                selectedItem = keyboard.nextLine();
                            } while (!items.contains(selectedItem) && !selectedItem.equalsIgnoreCase("exit"));
                            if (!selectedItem.equalsIgnoreCase("exit")) {
                                System.out.println(fridgeProxy.removeItem((CharSequence) selectedItem));
                            }
                        } else {
                            System.out.println("This fridge is empty.");
                        }
                        break;
                }
            } while (!choice.equalsIgnoreCase("exit"));
            fridgeclient.close();
            connectedToFridge = false;
        } catch (AvroRemoteException e) {
            connectedToFridge = false;
            inFridgeQueue=false;
            System.out.println("We seemed to have lost the fridge. Moving on.");
            return "Fridge crashed";
           } catch (IOException e) {
            e.printStackTrace();
        }


            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        inFridgeQueue = false;
                        Transceiver serverClient = new SaslSocketTransceiver(serverAddress);
                        ServerProtocol serverProxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, serverClient);
                        serverProxy.closeFridge(fridgeName, ip + "," + portNumber);
                        serverClient.close();
                    } catch (AvroRemoteException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        if (e.getMessage().contains("Connection refused")) {
                            System.out.println("unable to connect to the next user. Moving on");
                        }
                        if(e.getMessage().contains("An existing")){
                            System.out.println("cannot find server at this time");
                        }
                    }

                }
            });

        return "Closed fridge";
    }

    @Override
    public Void resetFridgeQueueStatus() throws AvroRemoteException {
        if(inFridgeQueue){
            System.out.println("Please try to reconnect to the fridge.");
        }
        inFridgeQueue = false;
        return null;
    }
}
