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
    Map<CharSequence, NeighbourData> neighbourList;
    private String lastNeighbourId;
    private NeighbourData currentLastNeighbourInfo;
    Map<String, LinkedList<String>> fridgeAccessQueue;
    //Variable to save lightstates when everyone leaves the house
    private Map<CharSequence, Boolean> lightsave = new HashMap<CharSequence, Boolean>();

    public ServerImpl() {
        lastNeighbourId = "0";
        currentLastNeighbourInfo = new NeighbourData("0.0.0.0", "none");
        neighbourList = new HashMap<>();
        fridgeAccessQueue = new HashMap<String, LinkedList<String>>();
        //Check if there is already a server running
        try {
            NetworkDiscoveryClient NDC = new NetworkDiscoveryClient();
            InetSocketAddress serverAddress = NDC.findServer();
            //Server has been found, so close it, take over replication and start myself
            Transceiver client = new SaslSocketTransceiver(serverAddress);
            ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
            ReplicationData repdata = ReplicationGenerator.generateReplica(proxy.getReplication());
            this.setReplication(repdata, "");
            boolean success = proxy.closeServer();
            if (!success) {
                System.out.println("Something went wrong while trying to close the old server");
                client.close();
                return;
            }
            client.close();
            Thread.sleep(200);    //Sleep for a short period to make sure the old server has been closed
        } catch (IOException e) {
            System.out.println("No server has been found, so we are safe to start.");
        } catch (InterruptedException e) {

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

    public ServerImpl(ReplicationData data, String oldClientId) {
        this.setReplication(data, oldClientId);

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
        String name = idCounter + "";

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

        //Send update to all clients/fridges
        for (Entry<CharSequence, CharSequence> entry : connectedUsers.entrySet()) {
            /*if (entry.getKey() == name) {
                continue;
            }*/
            try {
                String[] userValue = entry.getValue().toString().split(",");
                Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(userValue[0]), Integer.parseInt(userValue[1])));
                UserProtocol proxy = (UserProtocol) SpecificRequestor.getClient(UserProtocol.class, client);
                proxy.enter(name, ip, type);
                proxy.updateRepDataIdCounter(idCounter);
                proxy.updateRepDataNeighbours(neighbourList, lastNeighbourId);
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
                Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(userValue[0]), Integer.parseInt(userValue[1])));
                FridgeProtocol proxy = (FridgeProtocol) SpecificRequestor.getClient(FridgeProtocol.class, client);
                proxy.enter(name, ip, type);
                proxy.updateRepDataIdCounter(idCounter);
                proxy.updateRepDataNeighbours(neighbourList, lastNeighbourId);
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
        CharSequence type = "";

        for (Entry<CharSequence, CharSequence> entry : connectedLights.entrySet()) {
            if (entry.getKey().toString().equalsIgnoreCase(userName.toString())) {
                connectedLights.remove(userName);
                type = "light";
                return "Light lost connection";
            }
        }

        for (Entry<CharSequence, CharSequence> entry : connectedTS.entrySet()) {
            if (entry.getKey().toString().equalsIgnoreCase(userName.toString())) {
                connectedTS.remove(userName);
                type = "temperature sensor";
                return "Temperature sensor lost connection";
            }
        }

        String removedClientIp = "";
        boolean removeFridge = false;

        for (Entry<CharSequence, CharSequence> entry : connectedFridges.entrySet()) {
            if (entry.getKey().toString().equalsIgnoreCase(userName.toString())) {
                removeFridge = true;
            }
        }

        if (removeFridge) {
            removedClientIp = connectedFridges.get(userName).toString();
            connectedFridges.remove(userName);
            for (String userIp : fridgeAccessQueue.get(userName.toString())) {
                try {
                    String[] userValue = userIp.split(",");
                    Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(userValue[0]), Integer.parseInt(userValue[1])));
                    UserProtocol proxy = (UserProtocol) SpecificRequestor.getClient(UserProtocol.class, client);
                    proxy.resetFridgeQueueStatus();
                    client.close();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    System.out.println("Couldn't find user. Moving on");
                } catch (IOException e) {
                    System.out.println("Couldn't find user. Moving on");
                }
            }
            fridgeAccessQueue.remove(userName.toString());

            type = "fridge";
        }


        boolean removeUser = false;
        for (Entry<CharSequence, CharSequence> entry : connectedUsers.entrySet()) {
            if (entry.getKey().toString().equalsIgnoreCase(userName.toString())) {
                removeUser = true;
            }
        }

        if (removeUser) {
            removedClientIp = connectedUsers.get(userName).toString();
            connectedUsers.remove(userName);
            type = "user";
        }

        NeighbourData removedNeighbourData = neighbourList.get(userName);
        String previousNeighbourId = "";
        NeighbourData oldData = new NeighbourData();

        for (Entry<CharSequence, NeighbourData> entry : neighbourList.entrySet()) {
            if (entry.getValue().getIp().toString().equalsIgnoreCase(removedClientIp)) {
                previousNeighbourId = entry.getKey().toString();
                oldData = entry.getValue();
            }
        }
        neighbourList.remove(userName);

        if (previousNeighbourId.length() > 0) {
            neighbourList.replace(previousNeighbourId, oldData, removedNeighbourData);
        }

        //Send update to all clients/fridges
        for (Entry<CharSequence, CharSequence> entry : connectedFridges.entrySet()) {
            try {
                String[] userValue = entry.getValue().toString().split(",");
                Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(userValue[0]), Integer.parseInt(userValue[1])));
                FridgeProtocol proxy = (FridgeProtocol) SpecificRequestor.getClient(FridgeProtocol.class, client);
                proxy.leave(userName, type);
                proxy.updateRepDataNeighbours(neighbourList, lastNeighbourId);
                if (entry.getKey().toString().equalsIgnoreCase(previousNeighbourId)) {
                    proxy.addNeighbour(removedNeighbourData.getIp(), removedNeighbourData.getType());
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
        for (Entry<CharSequence, CharSequence> entry : connectedUsers.entrySet()) {
            try {
                String[] userValue = entry.getValue().toString().split(",");
                Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(userValue[0]), Integer.parseInt(userValue[1])));
                UserProtocol proxy = (UserProtocol) SpecificRequestor.getClient(UserProtocol.class, client);
                proxy.leave(userName, type);
                proxy.updateRepDataNeighbours(neighbourList, lastNeighbourId);
                if (entry.getKey().toString().equalsIgnoreCase(previousNeighbourId)) {
                    proxy.addNeighbour(removedNeighbourData.getIp(), removedNeighbourData.getType());
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
        return userName + " has left";
    }

    @Override
    public List<CharSequence> getClients() throws AvroRemoteException {

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
        } catch (UnknownHostException e) {
            ;
        } catch (IOException e) {
        } catch (NullPointerException e) {
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
        for (Entry<CharSequence, Boolean> entry : userlocation.entrySet()) {
            if (!entry.getValue()) {
                checkIfFirst = false;
            }
        }
        if (checkIfFirst) {
            //First user to enter the house so lights should be restored
            for (Entry<CharSequence, CharSequence> entry : connectedLights.entrySet()) {
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
        for (Entry<CharSequence, Boolean> entry : userlocation.entrySet()) {
            if (!entry.getValue()) {
                checkIfLast = false;
            }
        }
        if (checkIfLast) {
            //Save status of lights and turn them all off
            for (Entry<CharSequence, CharSequence> entry : connectedLights.entrySet()) {
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

        return new ReplicationData(this.connectedUsers, this.connectedLights, this.connectedFridges, this.connectedTS, temp, this.userlocation, neighbourList, idCounter, lastNeighbourId);
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
            try {
                grantUserFridgeAccess(clientIp.toString(), fridgeIp, fridgeName);
            } catch (IOException e) {
                if (e.getMessage().contains("Connection refused")) {
                    System.out.println("unable to connect to the next user. Moving on");
                    fridgeAccessQueue.get(fridgeName.toString()).remove(clientIp.toString());
                }
            }
            return "You have been granted access, the queue was empty.";
        } else {
            fridgeAccessQueue.get(fridgeName.toString()).add(clientIp.toString());
            return "You are number" + fridgeAccessQueue.get(fridgeName.toString()).size() + "in queue. Please wait, we will contact you when ready.";
        }
    }

    @Override
    public Void closeFridge(final CharSequence fridgeName, CharSequence clientIp) throws AvroRemoteException {
        fridgeAccessQueue.get(fridgeName.toString()).remove();
        boolean failedToConnectUser = true;
        while (fridgeAccessQueue.get(fridgeName.toString()).size() > 0 && failedToConnectUser) {
            try {
                failedToConnectUser = false;
                grantUserFridgeAccess(fridgeAccessQueue.get(fridgeName.toString()).peekFirst(), connectedFridges.get(fridgeName.toString()).toString(), fridgeName);
            } catch (IOException e) {
                if (e.getMessage().contains("Connection refused")) {
                    failedToConnectUser = true;
                    System.out.println("unable to connect to the next user. Moving on");
                    fridgeAccessQueue.get(fridgeName.toString()).remove();
                }
            }
        }
        return null;
    }

    private void regulateNeighbours(String newNeighbourName, String newNeighbourIp, String newNeighbourType) {
        if (neighbourList.size() == 0) {
            lastNeighbourId = newNeighbourName;
            currentLastNeighbourInfo = new NeighbourData(newNeighbourIp, newNeighbourType);
            neighbourList.put(newNeighbourName, new NeighbourData(newNeighbourIp, newNeighbourType));
        } else {
            NeighbourData lastNeighbourData = new NeighbourData();
            for (Entry<CharSequence, NeighbourData> entry : neighbourList.entrySet()) {
                if (entry.getKey().toString().equalsIgnoreCase(lastNeighbourId)) {
                    lastNeighbourData = entry.getValue();
                }
            }
            addNeighbourToClient(currentLastNeighbourInfo.getIp().toString(), currentLastNeighbourInfo.getType().toString(), newNeighbourIp, newNeighbourType);
            addNeighbourToClient(newNeighbourIp, newNeighbourType, lastNeighbourData.getIp().toString(), lastNeighbourData.getType().toString());
            neighbourList.replace(lastNeighbourId, lastNeighbourData, new NeighbourData(newNeighbourIp, newNeighbourType));
            neighbourList.put(newNeighbourName, lastNeighbourData);
            lastNeighbourId = newNeighbourName;
            currentLastNeighbourInfo = new NeighbourData(newNeighbourIp, newNeighbourType);
        }
    }

    private void addNeighbourToClient(String clientIp, String clientType, String neighbourIp, String neighbourType) {
        try {
            String[] clientIpValue = clientIp.split(",");
            Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(clientIpValue[0]), Integer.parseInt(clientIpValue[1])));

            switch (clientType) {
                case "fridge":
                    FridgeProtocol fridgeProxy = (FridgeProtocol) SpecificRequestor.getClient(FridgeProtocol.class, client);
                    fridgeProxy.addNeighbour(neighbourIp, neighbourType);
                    break;
                case "user":
                    UserProtocol userProxy = (UserProtocol) SpecificRequestor.getClient(UserProtocol.class, client);
                    userProxy.addNeighbour(neighbourIp, neighbourType);
                    break;
            }
            client.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void grantUserFridgeAccess(String clientIp, String fridgeIp, CharSequence fridgeName) throws IOException {
        try {
            String[] userValue = clientIp.split(",");
            Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(userValue[0]), Integer.parseInt(userValue[1])));
            UserProtocol userProxy = (UserProtocol) SpecificRequestor.getClient(UserProtocol.class, client);
            userProxy.grantFridgeAccess(fridgeIp, fridgeName);
            client.close();
        } catch (UnknownHostException e) {
            System.out.println("I do not know this host");
            e.printStackTrace();
        }
    }

    @Override
    public boolean closeServer() throws AvroRemoteException {
        try {
            NDS.end();
            NDSThread.interrupt();
            heartbeatThread.interrupt();
            this.setStayOpen(false);
            this.server.interrupt();
        } catch (Exception e) {
            //Something went wrong, dont start the new server, system might be completely destroyed
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void setReplication(ReplicationData data, CharSequence oldClientId) {
        fridgeAccessQueue = new HashMap<String, LinkedList<String>>();
        this.connectedUsers = data.getConnectedUsers();
        this.connectedLights = data.getConnectedLights();
        this.connectedFridges = data.getConnectedFridges();
        this.connectedTS = data.getConnectedTS();
        this.temperatures = new ArrayList<TemperatureMeasurementRecord>();
        this.idCounter = data.getIdCounter();
        heartbeat.updateReplication(data, oldClientId.toString());

        neighbourList = data.getNeighbourList();
        lastNeighbourId = data.getLastNeighbourId().toString();

        String oldClientIp = "";
        for (Entry<CharSequence, CharSequence> entry : connectedUsers.entrySet()) {
            if (entry.getKey().toString().equalsIgnoreCase(oldClientId.toString())) {
                oldClientIp = entry.getValue().toString();
                connectedUsers.remove(entry.getKey());
            }
            if (entry.getKey().toString().equalsIgnoreCase(lastNeighbourId)) {
                currentLastNeighbourInfo = new NeighbourData(entry.getValue(), "user");
            }

            try {
                String[] userValue = entry.getValue().toString().split(",");
                Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(userValue[0]), Integer.parseInt(userValue[1])));
                UserProtocol proxy = (UserProtocol) SpecificRequestor.getClient(UserProtocol.class, client);
                proxy.resetFridgeQueueStatus();
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
            fridgeAccessQueue.put(entry.getKey().toString(), new LinkedList<String>());
            if (entry.getKey().toString().equalsIgnoreCase(oldClientId.toString())) {
                oldClientIp = entry.getValue().toString();
                connectedFridges.remove(entry.getKey());
            }
            if (entry.getKey().toString().equalsIgnoreCase(lastNeighbourId)) {
                currentLastNeighbourInfo = new NeighbourData(entry.getValue(), "fridge");
            }
        }
        if (!oldClientId.toString().equalsIgnoreCase("")) {
            if (neighbourList.size() > 1) {

                CharSequence previousNeighbourId = "";
                NeighbourData previousNeighbourData = new NeighbourData();
                NeighbourData oldClientNeighbourData = new NeighbourData();
                for (Entry<CharSequence, NeighbourData> entry : neighbourList.entrySet()) {
                    if (entry.getValue().getIp().toString().equalsIgnoreCase(oldClientIp)) {
                        previousNeighbourId = entry.getKey();
                        previousNeighbourData = entry.getValue();
                    }
                    if (entry.getKey().toString().equalsIgnoreCase(oldClientId.toString())) {
                        oldClientNeighbourData = entry.getValue();
                    }
                }

                CharSequence previousNeighbourIp = "";
                String previousNeighbourType = "";

                for (Entry<CharSequence, CharSequence> entry : connectedUsers.entrySet()) {
                    if (entry.getKey().toString().equalsIgnoreCase(previousNeighbourId.toString())) {
                        previousNeighbourIp = entry.getValue();
                        previousNeighbourType = "fridge";
                    }
                }

                for (Entry<CharSequence, CharSequence> entry : connectedFridges.entrySet()) {
                    if (entry.getKey().toString().equalsIgnoreCase(previousNeighbourId.toString())) {
                        previousNeighbourIp = entry.getValue();
                        previousNeighbourType = "fridge";
                    }
                }

                if (neighbourList.size() > 2) {
                    addNeighbourToClient(previousNeighbourIp.toString(), previousNeighbourType, oldClientNeighbourData.getIp().toString(), oldClientNeighbourData.getType().toString());
                    neighbourList.replace(previousNeighbourId, previousNeighbourData, oldClientNeighbourData);
                    Map<CharSequence, NeighbourData> tempNeighbourList = new HashMap<>();
                    for (Entry<CharSequence, NeighbourData> entry : neighbourList.entrySet()) {
                        if (!entry.getKey().toString().equalsIgnoreCase(oldClientId.toString())) {
                            tempNeighbourList.put(entry.getKey(), entry.getValue());
                        }
                    }
                    neighbourList.clear();
                    neighbourList.putAll(tempNeighbourList);
                }
                if (oldClientId.toString().equalsIgnoreCase(lastNeighbourId)) {
                    lastNeighbourId = previousNeighbourId.toString();
                    currentLastNeighbourInfo = new NeighbourData(previousNeighbourIp, previousNeighbourType);
                }
            } else {
                lastNeighbourId = "";
                currentLastNeighbourInfo = null;
                neighbourList = new HashMap<>();
            }
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
