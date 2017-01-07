package classes.models;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.AvroRuntimeException;
import org.apache.avro.ipc.SaslSocketServer;
import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.avro.ipc.specific.SpecificResponder;

import classes.ServerExe;
import sourcefiles.*;
import utility.LANIp;
import utility.NetworkDiscoveryClient;
import utility.NetworkDiscoveryServer;
import utility.ReplicationGenerator;
import utility.ServerHeartbeatMaintainer;
import utility.TemperatureMeasurementRecord;

public class ServerImpl implements ServerProtocol {
    private Map<CharSequence, CharSequence> connectedUsers = new HashMap<CharSequence, CharSequence>();
    private Map<CharSequence, CharSequence> connectedLights = new HashMap<CharSequence, CharSequence>();
    private Map<CharSequence, CharSequence> connectedFridges = new HashMap<CharSequence, CharSequence>();
    private Map<CharSequence, CharSequence> connectedTS = new HashMap<CharSequence, CharSequence>();
    private ArrayList<TemperatureMeasurementRecord> temperatures = new ArrayList<TemperatureMeasurementRecord>();
    private Map<CharSequence, Boolean> userlocation = new HashMap<CharSequence, Boolean>();    //Maps a user to a location (1 = outside, 0 = inside)
   
    ServerHeartbeatMaintainer heartbeat = new ServerHeartbeatMaintainer(this);
    Thread heartbeatThread = new Thread(heartbeat);
    NetworkDiscoveryServer NDS;
    Thread NDSThread;
    
    private boolean stayOpen = true;
    SaslSocketServer server;
    private int idCounter;
    private List<String> firstNeighbour;
    private List<String> lastNeighbour;
    Map<String, LinkedList<String>> fridgeAccessQueue;
    //Variable to save lightstates when everyone leaves the house
  	private Map<CharSequence, Boolean> lightsave = new HashMap<CharSequence, Boolean>();

    public ServerImpl() {
        firstNeighbour = new ArrayList<String>();
        lastNeighbour = new ArrayList<String>();
        fridgeAccessQueue = new HashMap<String, LinkedList<String>>();
    	//Check if there is already a server running 
    	try{
    		NetworkDiscoveryClient NDC = new NetworkDiscoveryClient();
    		InetSocketAddress serverAddress = NDC.findServer();
    		//Server has been found, so close it, take over replication and start myself
    		Transceiver client = new SaslSocketTransceiver(serverAddress);
			ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
			ReplicationData repdata = ReplicationGenerator.generateReplica(proxy.getReplication());
			this.setReplication(repdata);
			boolean success = proxy.closeServer();
			if(!success){
				System.out.println("Something went wrong while trying to close the old server");
				client.close();
				return;
			}
			client.close();
			Thread.sleep(200);	//Sleep for a short period to make sure the old server has been closed
    	} catch(IOException e){
    		System.out.println("No server has been found, so we are safe to start.");
    	} catch (InterruptedException e) {
			// TODO Auto-generated catch block
		}
        try {
        	InetAddress localaddress = LANIp.getAddress();
        	ServerSocket s1 = new ServerSocket(0);
			int portnumber = s1.getLocalPort();
			s1.close();
            NDS = new NetworkDiscoveryServer(portnumber);
            NDSThread = new Thread(NDS);
            NDSThread.start();
            heartbeatThread.start();
            server = new SaslSocketServer(new SpecificResponder(ServerProtocol.class, this), new InetSocketAddress(localaddress, portnumber));
            server.start();

        } catch (IOException e) {
            System.err.println("[error]: Failed to start server");
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    public ServerImpl(ReplicationData data) {
        System.out.println("Booting server");
        this.setReplication(data);

        try {
        	InetAddress localaddress = LANIp.getAddress();
        	ServerSocket s1 = new ServerSocket(0);
			int portnumber = s1.getLocalPort();
			s1.close();
			NDS = new NetworkDiscoveryServer(portnumber);
            NDSThread = new Thread(NDS);
            NDSThread.start();
            heartbeatThread.start();
            server = new SaslSocketServer(new SpecificResponder(ServerProtocol.class, this), new InetSocketAddress(localaddress, portnumber));
            server.start();
        } catch (IOException e) {
            System.err.println("[error]: Failed to start server");
            e.printStackTrace(System.err);
            System.exit(1);
        }

    }

    @Override
    public CharSequence enter(CharSequence type, CharSequence ip) throws AvroRemoteException {
        System.out.println("Client coming in");
        String name = idCounter + "";

        //TODO ENUM maken met de verschillende types in
        switch (type.toString()) {
            case "light":
                if (connectedLights.containsValue(ip)) {
                    break;
                }
                connectedLights.put(name, ip);
                heartbeat.updateClient(name);
                break;
            case "temperature sensor":
                if (connectedTS.containsValue(ip)) {
                    break;
                }
                connectedTS.put(name, ip);
                heartbeat.updateClient(name);
                break;
            case "fridge":
                if (connectedFridges.containsValue(ip)) {
                    break;
                }
                regulateNeighbours(name, ip.toString(), type.toString());
                connectedFridges.put(name, ip);
                fridgeAccessQueue.put(name, new LinkedList<String>());
                heartbeat.updateClient(name);
                break;
            case "user":
                if (connectedUsers.containsValue(ip)) {
                    break;
                }
                regulateNeighbours(name, ip.toString(), type.toString());
                userlocation.put(name, false);
                connectedUsers.put(name, ip);
                heartbeat.updateClient(name);
                break;
        }
        idCounter++;

        List<CharSequence> firstNeighbourCharSequence = new ArrayList<CharSequence>();
        List<CharSequence> lastNeighbourCharSequence = new ArrayList<CharSequence>();

        firstNeighbourCharSequence.addAll(firstNeighbour);
        lastNeighbourCharSequence.addAll(lastNeighbour);

        //Send update to all clients/fridges
        for (Entry<CharSequence, CharSequence> entry : connectedUsers.entrySet()) {
            /*if (entry.getKey() == name) {
                continue;
            }*/
            try {
                String[] userValue = entry.getValue().toString().split(",");
                Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(userValue[0]), Integer.parseInt(userValue[1])));
                UserProtocol proxy = (UserProtocol) SpecificRequestor.getClient(UserProtocol.class, client);
                proxy.enter(name, ip,type);
                proxy.updateRepDataIdCounter(idCounter);
                System.out.println(proxy.updateRepDataNeighbours(firstNeighbourCharSequence, lastNeighbourCharSequence));
                client.close();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (Entry<CharSequence, CharSequence> entry : connectedFridges.entrySet()) {
            /*if (entry.getKey() == name) {
                continue;
            }*/
            try {
                String[] userValue = entry.getValue().toString().split(",");
                System.out.println("connecting to fridge at ip: " + userValue[0] + " and at port: " + userValue[1]);
                Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(userValue[0]), Integer.parseInt(userValue[1])));
                FridgeProtocol proxy = (FridgeProtocol) SpecificRequestor.getClient(FridgeProtocol.class, client);
                proxy.enter(name, ip,type);
                proxy.updateRepDataIdCounter(idCounter);
                System.out.println(proxy.updateRepDataNeighbours(firstNeighbourCharSequence, lastNeighbourCharSequence));
                client.close();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return name;
    }

    @Override
    public CharSequence leave(CharSequence userName) throws AvroRemoteException {
        System.out.println("Removing: " + userName);
        List<CharSequence> neighbours = new ArrayList<CharSequence>();
        CharSequence type = "";

        for (Entry<CharSequence, CharSequence> entry : connectedLights.entrySet()) {
            if (entry.getKey().toString().equalsIgnoreCase(userName.toString())) {
                connectedLights.remove(userName);
                type="light";
                return "Light lost connection";
            }
        }

        for (Entry<CharSequence, CharSequence> entry : connectedTS.entrySet()) {
            if (entry.getKey().toString().equalsIgnoreCase(userName.toString())) {
                connectedTS.remove(userName);
                type="temperature sensor";
                return "Temperature sensor lost connection";
            }
        }

        for (Entry<CharSequence, CharSequence> entry : connectedFridges.entrySet()) {
            if (entry.getKey().toString().equalsIgnoreCase(userName.toString())) {
                String[] fridgeValue = entry.getValue().toString().split(",");
                try {
                    Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(fridgeValue[0]), Integer.parseInt(fridgeValue[1])));
                    FridgeProtocol fridgeProxy = (FridgeProtocol) SpecificRequestor.getClient(FridgeProtocol.class, client);
                    neighbours = fridgeProxy.getNeighbours();
                    client.close();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                connectedFridges.remove(userName);
                fridgeAccessQueue.remove(userName.toString());
                type="fridge";
            }
        }

        for (Entry<CharSequence, CharSequence> entry : connectedUsers.entrySet()) {
            if (entry.getKey().toString().equalsIgnoreCase(userName.toString())) {
                String[] userValue = entry.getValue().toString().split(",");
                try {
                    Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(userValue[0]), Integer.parseInt(userValue[1])));
                    UserProtocol userProxy = (UserProtocol) SpecificRequestor.getClient(UserProtocol.class, client);
                    neighbours = userProxy.getNeighbours();
                    client.close();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                connectedUsers.remove(userName);
                type="user";
            }
        }

        if (lastNeighbour.size() == 0) {
            firstNeighbour.clear();
        } else {
            if (firstNeighbour.get(0).equalsIgnoreCase(userName.toString())) {
                if (lastNeighbour.get(0).equalsIgnoreCase(neighbours.get(0).toString()) && lastNeighbour.get(0).equalsIgnoreCase(neighbours.get(3).toString())) {
                    firstNeighbour.clear();
                    try {
                        String[] firstNeighbourIpValue = firstNeighbour.get(1).split(",");
                        Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(firstNeighbourIpValue[0]), Integer.parseInt(firstNeighbourIpValue[1])));

                        switch (firstNeighbour.get(2)) {
                            case "fridge":
                                FridgeProtocol fridgeProxy = (FridgeProtocol) SpecificRequestor.getClient(FridgeProtocol.class, client);
                                fridgeProxy.clearNeighbours();
                                break;
                            case "user":
                                UserProtocol userProxy = (UserProtocol) SpecificRequestor.getClient(UserProtocol.class, client);
                                userProxy.clearNeighbours();
                                break;
                        }
                        client.close();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    firstNeighbour.addAll(lastNeighbour);
                    lastNeighbour.clear();
                } else {
                    firstNeighbour.clear();
                    firstNeighbour.add(0, neighbours.get(3).toString());
                    firstNeighbour.add(1, neighbours.get(4).toString());
                    firstNeighbour.add(2, neighbours.get(5).toString());
                }
            }

            if (lastNeighbour.get(0).equalsIgnoreCase(userName.toString())) {
                if (firstNeighbour.get(0).equalsIgnoreCase(neighbours.get(0).toString()) && firstNeighbour.get(0).equalsIgnoreCase(neighbours.get(3).toString())) {
                    try {
                        String[] firstNeighbourIpValie = firstNeighbour.get(1).split(",");
                        Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(firstNeighbourIpValie[0]), Integer.parseInt(firstNeighbourIpValie[1])));

                        switch (firstNeighbour.get(2)) {
                            case "fridge":
                                FridgeProtocol fridgeProxy = (FridgeProtocol) SpecificRequestor.getClient(FridgeProtocol.class, client);
                                fridgeProxy.clearNeighbours();
                                break;
                            case "user":
                                UserProtocol userProxy = (UserProtocol) SpecificRequestor.getClient(UserProtocol.class, client);
                                userProxy.clearNeighbours();
                                break;
                        }
                        client.close();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    lastNeighbour.clear();
                } else {
                    lastNeighbour.clear();
                    lastNeighbour.add(0, neighbours.get(0).toString());
                    lastNeighbour.add(1, neighbours.get(1).toString());
                    lastNeighbour.add(2, neighbours.get(2).toString());
                }
            }

            //TODO eerste buur van te verwijderen client aan de laatste hangen
            try {
                String[] firstNeighbourValue = neighbours.get(1).toString().split(",");
                Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(firstNeighbourValue[0]), Integer.parseInt(firstNeighbourValue[1])));

                switch (neighbours.get(2).toString()) {
                    case "fridge":
                        FridgeProtocol fridgeProxy = (FridgeProtocol) SpecificRequestor.getClient(FridgeProtocol.class, client);
                        fridgeProxy.addNeighbour(neighbours.get(3), neighbours.get(4), neighbours.get(5), true);
                        break;
                    case "user":
                        UserProtocol userProxy = (UserProtocol) SpecificRequestor.getClient(UserProtocol.class, client);
                        userProxy.addNeighbour(neighbours.get(3), neighbours.get(4), neighbours.get(5), true);
                        break;
                }
                client.close();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //TODO laatste buur van te verwijderen client aan de eerste hangen
            try {
                String[] lastNeighbourValue = neighbours.get(4).toString().split(",");
                Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(lastNeighbourValue[0]), Integer.parseInt(lastNeighbourValue[1])));

                switch (neighbours.get(5).toString()) {
                    case "fridge":
                        FridgeProtocol fridgeProxy = (FridgeProtocol) SpecificRequestor.getClient(FridgeProtocol.class, client);
                        fridgeProxy.addNeighbour(neighbours.get(0), neighbours.get(1), neighbours.get(2), false);
                        break;
                    case "user":
                        UserProtocol userProxy = (UserProtocol) SpecificRequestor.getClient(UserProtocol.class, client);
                        userProxy.addNeighbour(neighbours.get(0), neighbours.get(1), neighbours.get(2), false);
                        break;
                }
                client.close();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        List<CharSequence> firstNeighbourCharSequence = new ArrayList<CharSequence>();
        List<CharSequence> lastNeighbourCharSequence = new ArrayList<CharSequence>();

        firstNeighbourCharSequence.addAll(firstNeighbour);
        lastNeighbourCharSequence.addAll(lastNeighbour);

        //TODO zien om dit te mergen met for loops hierboven
        //Send update to all clients/fridges
        for (Entry<CharSequence, CharSequence> entry : connectedFridges.entrySet()) {
            try {
                String[] userValue = entry.getValue().toString().split(",");
                Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(userValue[0]), Integer.parseInt(userValue[1])));
                FridgeProtocol proxy = (FridgeProtocol) SpecificRequestor.getClient(FridgeProtocol.class, client);
                proxy.leave(userName,type);
                proxy.updateRepDataNeighbours(firstNeighbourCharSequence, lastNeighbourCharSequence);
                client.close();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (Entry<CharSequence, CharSequence> entry : connectedUsers.entrySet()) {
            try {
                String[] userValue = entry.getValue().toString().split(",");
                Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(userValue[0]), Integer.parseInt(userValue[1])));
                UserProtocol proxy = (UserProtocol) SpecificRequestor.getClient(UserProtocol.class, client);
                proxy.leave(userName,type);
                proxy.updateRepDataNeighbours(firstNeighbourCharSequence, lastNeighbourCharSequence);
                client.close();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return userName + " has left";
    }

    @Override
    public List<CharSequence> getClients() throws AvroRemoteException {
        // TODO Auto-generated method stub

        List<CharSequence> clients = new ArrayList<CharSequence>();

        for (Entry<CharSequence, CharSequence> entry : connectedUsers.entrySet()) {
            String name = "User" + entry.getKey() + ", Location: " + (userlocation.get(entry.getKey()) ? "Outside" : "Inside");
            clients.add(name);
        }

        for (Entry<CharSequence, CharSequence> entry : connectedLights.entrySet()) {
            String name = "Light" + entry.getKey().toString();
            clients.add(name);
        }

        for (Entry<CharSequence, CharSequence> entry : connectedFridges.entrySet()) {
            String name = "Fridge" + (String) entry.getKey();
            clients.add(name);
        }

        for (Entry<CharSequence, CharSequence> entry : connectedTS.entrySet()) {
            String name = "Ts" + (String) entry.getKey();
            clients.add(name);
        }

        return clients;
    }

    @Override
    public List<CharSequence> getLightStatuses() throws AvroRemoteException {
        List<CharSequence> lightStatuses = new ArrayList<CharSequence>();

        Set<Map.Entry<CharSequence, CharSequence>> set = connectedLights.entrySet();

        for (Map.Entry<CharSequence, CharSequence> light : set) {
            try {
                String[] lightValue = light.getValue().toString().split(",");
                Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(lightValue[0]), Integer.parseInt(lightValue[1])));
                LightProtocol proxy = (LightProtocol) SpecificRequestor.getClient(LightProtocol.class, client);
                boolean status = proxy.getState();
                if (status) {
                    lightStatuses.add(light.getKey() + " is on");
                } else {
                    lightStatuses.add(light.getKey() + " is off");
                }
                client.close();

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
        return lightStatuses;
    }

    @Override
    public CharSequence changeLightState(CharSequence lightName) throws AvroRemoteException {
        boolean status = false;
        try {
            String[] lightValue = connectedLights.get(lightName.toString()).toString().split(",");
            Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(lightValue[0]), Integer.parseInt(lightValue[1])));
            LightProtocol proxy = (LightProtocol) SpecificRequestor.getClient(LightProtocol.class, client);

            status = proxy.changeState();

            client.close();

        } catch (AvroRemoteException e) {
            System.err.println("Error joining");
        } catch (UnknownHostException e) {;
        } catch (IOException e) {
        } catch (NullPointerException e){
        	//The requested light doesnt exist
        	return "That light doesn't exist";
        }
        if (status) {
            return lightName + " is now on";
        } else {
            return lightName + " is now ";
        }
    }

    @Override
    public int showCurrentHouseTemp() throws AvroRemoteException {
        if (temperatures.isEmpty()) {
       		throw new AvroRemoteException("No temperatures found");
        }
        return (int) temperatures.get(temperatures.size() - 1).record.getTemperature().intValue();
    }

    @Override
    public Map<CharSequence, Integer> showTempHistory() throws AvroRemoteException {
        if (temperatures.isEmpty()) {
            throw new AvroRuntimeException("NoMeasurementsError");
        }
        Map<CharSequence, Integer> returnmap = new HashMap<CharSequence, Integer>();
        for (TemperatureMeasurementRecord record : temperatures) {
            returnmap.put(record.record.time, (int) record.record.temperature);
        }
        return returnmap;
    }

    @Override
    public CharSequence connectUserToFridge(CharSequence fridgeName) throws AvroRemoteException {
        //Add concurency on a later date
        System.out.println(connectedFridges.get(fridgeName.toString()));
        return connectedFridges.get(fridgeName.toString());
    }

    @Override
    public List<CharSequence> showConnectedFridges() throws AvroRemoteException {
        List<CharSequence> fridges = new ArrayList<CharSequence>();

        for (Entry<CharSequence, CharSequence> entry : connectedFridges.entrySet()) {
            CharSequence name = entry.getKey();
            fridges.add(name);
        }

        return fridges;
    }

    @Override
    public CharSequence requestShowEmptyFridge(CharSequence fridgeName) throws AvroRemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void updateTemperature(CharSequence sensorName, TemperatureRecord sensorValue) throws AvroRemoteException {
        double temperature = sensorValue.getTemperature();

        if (connectedTS.containsKey(sensorName.toString())) {
            boolean Exists = false;
            TemperatureAggregate TA = new TemperatureAggregate();
            for (TemperatureMeasurementRecord record : temperatures) {
                if (record.isTime(sensorValue.getTime().toString())) {
                    record.addTemperature(temperature, sensorValue.getTime().toString());
                    TA = record.getAggregate();
                    System.out.println(record.getCounter());
                    Exists = true;
                    break;
                }
            }

            if (!Exists) {
                //No measurements for time x have been made yet, so add it
                TemperatureMeasurementRecord temprecord = new TemperatureMeasurementRecord();
                temprecord.addTemperature(sensorValue.temperature, sensorValue.time.toString());
                TA = temprecord.getAggregate();
                temperatures.add(temprecord);
            }

            //Send update to all clients/fridges
            for (Entry<CharSequence, CharSequence> entry : connectedUsers.entrySet()) {
                try {
                    String[] userValue = entry.getValue().toString().split(",");
                    Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(userValue[0]), Integer.parseInt(userValue[1])));
                    UserProtocol proxy = (UserProtocol) SpecificRequestor.getClient(UserProtocol.class, client);
                    proxy.updateTemperature(TA);
                    client.close();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            for (Entry<CharSequence, CharSequence> entry : connectedFridges.entrySet()) {
                try {
                    String[] userValue = entry.getValue().toString().split(",");
                    Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(userValue[0]), Integer.parseInt(userValue[1])));
                    FridgeProtocol proxy = (FridgeProtocol) SpecificRequestor.getClient(FridgeProtocol.class, client);
                    proxy.updateTemperature(TA);
                    client.close();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        return null;
    }

    @Override
    public List<CharSequence> getFridgeInventory(CharSequence fridgeName) throws AvroRemoteException {
        String[] fridgeValue = connectedFridges.get(fridgeName.toString()).toString().split(",");

        List<CharSequence> inventory = new ArrayList<CharSequence>();
        try {
            Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(fridgeValue[0]), Integer.parseInt(fridgeValue[1])));
            FridgeProtocol proxy = (FridgeProtocol) SpecificRequestor.getClient(FridgeProtocol.class, client);
            inventory = proxy.getInventory();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return inventory;
    }

    @Override
    public CharSequence notifyUsersOfEmptyFridge(CharSequence fridgeName) throws AvroRemoteException {
        for (Entry<CharSequence, CharSequence> entry : connectedUsers.entrySet()) {
            try {
                String[] userValue = entry.getValue().toString().split(",");
                Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(userValue[0]), Integer.parseInt(userValue[1])));
                UserProtocol proxy = (UserProtocol) SpecificRequestor.getClient(UserProtocol.class, client);
                System.out.println(proxy.notifyOfEmptyFridge(fridgeName));
                client.close();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return "All users have been notified";
    }

    @Override
    public boolean enterHouse(CharSequence userName) throws AvroRemoteException {
        if (!connectedUsers.containsKey(userName.toString())) {
            throw new AvroRuntimeException("User hasnt joined the system yet");
        }
        boolean checkIfFirst = true;
		for(Entry<CharSequence, Boolean> entry : userlocation.entrySet()){
			if(!entry.getValue()){
				checkIfFirst = false;
			}
		}
		if(checkIfFirst){
			//First user to enter the house so lights should be restored
			for (Entry<CharSequence, CharSequence> entry : connectedLights.entrySet()){
				try {
					String[] lightValue = entry.getValue().toString().split(",");
					Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(lightValue[0]), Integer.parseInt(lightValue[1])));
					LightProtocol proxy = (LightProtocol) SpecificRequestor.getClient(LightProtocol.class, client);
					proxy.setState(lightsave.get(entry.getKey()));
					client.close();
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
		}
        userlocation.put(userName.toString(), false);
        notifyUsers(userName, " entered ");
        return true;
    }

    @Override
    public boolean leaveHouse(CharSequence userName) throws AvroRemoteException {
        if (!connectedUsers.containsKey(userName.toString())) {
            throw new AvroRuntimeException("User hasnt joined the system yet");
        }
        userlocation.put(userName.toString(), true);
        boolean checkIfLast = true;
		for(Entry<CharSequence, Boolean> entry : userlocation.entrySet()){
			if(!entry.getValue()){
				checkIfLast = false;
			}
		}
		if(checkIfLast){
			System.out.println("test");
			//Save status of lights and turn them all off
			for (Entry<CharSequence, CharSequence> entry : connectedLights.entrySet()){
				try {
					String[] lightValue = entry.getValue().toString().split(",");
					Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(lightValue[0]), Integer.parseInt(lightValue[1])));
					LightProtocol proxy = (LightProtocol) SpecificRequestor.getClient(LightProtocol.class, client);
					boolean status = proxy.getState();
					proxy.setState(false);
					lightsave.put(entry.getKey(), status);
					client.close();
					
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
		}
        notifyUsers(userName, " left ");
        return true;
    }

    //Method that will notify all users when someone leaves/enters the house
    //State = "entered" or "left" depending on what the user did
    private void notifyUsers(CharSequence userName, CharSequence state) {
        for (Entry<CharSequence, CharSequence> entry : connectedUsers.entrySet()) {
            try {
                String[] userValue = entry.getValue().toString().split(",");
                Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(userValue[0]), Integer.parseInt(userValue[1])));
                UserProtocol proxy = (UserProtocol) SpecificRequestor.getClient(UserProtocol.class, client);
                proxy.notifyUsers(userName, state);
                client.close();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        for (Entry<CharSequence, CharSequence> entry : connectedFridges.entrySet()) {
            try {
                String[] userValue = entry.getValue().toString().split(",");
                Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(userValue[0]), Integer.parseInt(userValue[1])));
                FridgeProtocol proxy = (FridgeProtocol) SpecificRequestor.getClient(FridgeProtocol.class, client);
                if (state == " entered ") {
                    proxy.enterHouse(userName);
                } else {
                    proxy.leaveHouse(userName);
                }
                client.close();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public Void showHeartbeat(CharSequence userName) throws AvroRemoteException {
        heartbeat.updateClient(userName.toString());
        return null;
    }

    @Override
    public ReplicationData getReplication() throws AvroRemoteException {
        ArrayList<TemperatureAggregate> temp = new ArrayList<TemperatureAggregate>();
        for (TemperatureMeasurementRecord x : this.temperatures) {
            temp.add(x.getAggregate());
        }
        System.out.println(temp.size());

        List<CharSequence> firstNeighbourCharSequence = new ArrayList<CharSequence>();
        List<CharSequence> lastNeighbourCharSequence = new ArrayList<CharSequence>();

        firstNeighbourCharSequence.addAll(firstNeighbour);
        lastNeighbourCharSequence.addAll(lastNeighbour);

        return new ReplicationData(this.connectedUsers, this.connectedLights, this.connectedFridges, this.connectedTS, temp, this.userlocation, firstNeighbourCharSequence, lastNeighbourCharSequence, idCounter);
    }

    @Override
    public CharSequence requestFridgeAddress(CharSequence fridgeName, CharSequence clientIp) throws AvroRemoteException {
        if (fridgeAccessQueue.get(fridgeName.toString()).size() == 0) {
            String fridgeIp = "";
            for (Entry<CharSequence, CharSequence> entry : connectedFridges.entrySet()) {
                if (entry.getKey().toString().equalsIgnoreCase(fridgeName.toString())) {
                    fridgeIp = entry.getValue().toString();
                }
            }
            fridgeAccessQueue.get(fridgeName.toString()).add(clientIp.toString());
            grantUserFridgeAccess(clientIp.toString(), fridgeIp, fridgeName);
            return "You have been granted access, the queue was empty.";
        } else {
            fridgeAccessQueue.get(fridgeName.toString()).add(clientIp.toString());
            return "You are number" + fridgeAccessQueue.get(fridgeName.toString()).size() + "in queue. Please wait, we will contact you when ready.";
        }
    }

    @Override
    public Void closeFridge(final CharSequence fridgeName, CharSequence clientIp) throws AvroRemoteException {
        if (fridgeAccessQueue.get(fridgeName.toString()).peekFirst().equalsIgnoreCase(clientIp.toString())) {
            fridgeAccessQueue.get(fridgeName.toString()).remove();
        }
        if (fridgeAccessQueue.get(fridgeName.toString()).size() > 0) {
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    grantUserFridgeAccess(fridgeAccessQueue.get(fridgeName.toString()).remove(), connectedFridges.get(fridgeName.toString()).toString(), fridgeName);
                }
            });
        }
        return null;
    }

    private void addNeighbourToClient(String clientIp, String clientType, String neighbourName, String neighbourIp, String neighbourType, boolean isNextNeighbour) {
        try {
            String[] clientIpValue = clientIp.split(",");
            Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(clientIpValue[0]), Integer.parseInt(clientIpValue[1])));

            switch (clientType) {
                case "fridge":
                    FridgeProtocol fridgeProxy = (FridgeProtocol) SpecificRequestor.getClient(FridgeProtocol.class, client);
                    fridgeProxy.addNeighbour(neighbourName, neighbourIp, neighbourType, isNextNeighbour);
                    break;
                case "user":
                    UserProtocol userProxy = (UserProtocol) SpecificRequestor.getClient(UserProtocol.class, client);
                    userProxy.addNeighbour(neighbourName, neighbourIp, neighbourType, isNextNeighbour);
                    break;
            }
            client.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void regulateNeighbours(String clientName, String clientIp, String clientType) {
        if (firstNeighbour.size() == 0) {
            firstNeighbour.add(0, clientName);
            firstNeighbour.add(1, clientIp);
            firstNeighbour.add(2, clientType);
        } else {
            if (lastNeighbour.size() == 0) {
                addNeighbourToClient(firstNeighbour.get(1), firstNeighbour.get(2), clientName, clientIp, clientType, false);
                addNeighbourToClient(firstNeighbour.get(1), firstNeighbour.get(2), clientName, clientIp, clientType, true);
                addNeighbourToClient(clientIp, clientType, firstNeighbour.get(0), firstNeighbour.get(1), firstNeighbour.get(2), true);
                addNeighbourToClient(clientIp, clientType, firstNeighbour.get(0), firstNeighbour.get(1), firstNeighbour.get(2), false);
            } else {
                addNeighbourToClient(clientIp, clientType, lastNeighbour.get(0), lastNeighbour.get(1), lastNeighbour.get(2), false);
                addNeighbourToClient(clientIp, clientType, firstNeighbour.get(0), firstNeighbour.get(1), firstNeighbour.get(2), true);
                addNeighbourToClient(lastNeighbour.get(1), lastNeighbour.get(2), clientName, clientIp, clientType, true);
                addNeighbourToClient(firstNeighbour.get(1), firstNeighbour.get(2), clientName, clientIp, clientType, false);
            }
            lastNeighbour.clear();
            lastNeighbour.add(0, clientName);
            lastNeighbour.add(1, clientIp);
            lastNeighbour.add(2, clientType);
        }
    }

    private void grantUserFridgeAccess(String clientIp, String fridgeIp, CharSequence fridgeName) {
        try {
            String[] userValue = clientIp.split(",");
            Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(userValue[0]), Integer.parseInt(userValue[1])));
            UserProtocol userProxy = (UserProtocol) SpecificRequestor.getClient(UserProtocol.class, client);
            userProxy.grantFridgeAccess(fridgeIp, fridgeName);
            client.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	@Override
	public boolean closeServer() throws AvroRemoteException {
		try{
	        NDS.end();
	        NDSThread.interrupt();
	        heartbeatThread.interrupt();
	        this.setStayOpen(false);
	        this.server.interrupt();
		} catch(Exception e){
			//Something went wrong, dont start the new server, system might be completely destroyed
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private void setReplication(ReplicationData data){
		    fridgeAccessQueue = new HashMap<String, LinkedList<String>>();
	        this.connectedUsers = data.getConnectedUsers();
	        this.connectedLights = data.getConnectedLights();
	        this.connectedFridges = data.getConnectedFridges();
	        this.connectedTS = data.getConnectedTS();
	        this.temperatures = new ArrayList<TemperatureMeasurementRecord>();
	        this.idCounter = data.getIdCounter();
	        heartbeat.updateReplication(data);
	        
	        firstNeighbour = new ArrayList<String>();
	        lastNeighbour = new ArrayList<String>();
	        for (Entry<CharSequence, CharSequence> entry : connectedFridges.entrySet()){
	        	fridgeAccessQueue.put(entry.getKey().toString(), new LinkedList<String>());
	        }
	        
	        for (CharSequence firstValue : data.getFirstNeighbour()) {
	            this.firstNeighbour.add(firstValue.toString());
	        }

	        for (CharSequence lastValue : data.getLastNeighbour()) {
	            this.lastNeighbour.add(lastValue.toString());
	        }

	        List<TemperatureAggregate> temperaturestemp = data.getTemperatures();
	        for (TemperatureAggregate x : temperaturestemp) {
	            TemperatureMeasurementRecord newrecord = new TemperatureMeasurementRecord(x);
	            this.temperatures.add(newrecord);
	        }
	        this.userlocation = data.getUserlocation();
	}

	public boolean isStayOpen() {
		return this.stayOpen;
	}

	public void setStayOpen(boolean stayOpen) {
		this.stayOpen = stayOpen;
	}
}
