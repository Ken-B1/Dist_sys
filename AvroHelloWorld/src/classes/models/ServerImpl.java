package classes.models;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.AvroRuntimeException;
import org.apache.avro.ipc.SaslSocketServer;
import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.avro.ipc.specific.SpecificResponder;

import classes.ServerExe;
import sourcefiles.FridgeProtocol;
import sourcefiles.LightProtocol;
import sourcefiles.ReplicationData;
import sourcefiles.ServerProtocol;
import sourcefiles.TemperatureAggregate;
import sourcefiles.TemperatureRecord;
import sourcefiles.UserProtocol;
import sourcefiles.replicationrecord;
import utility.NetworkDiscoveryServer;
import utility.ServerHeartbeatMaintainer;
import utility.TemperatureMeasurementRecord;

public class ServerImpl implements ServerProtocol {
	private Map<CharSequence, CharSequence> connectedUsers = new HashMap<CharSequence, CharSequence>();
	int userCount = 0; //Variable to give clients unique names
	private Map<CharSequence, CharSequence> connectedLights = new HashMap<CharSequence, CharSequence>();
	int lightCount = 0; //Variable to give clients unique names
	private Map<CharSequence, CharSequence> connectedFridges = new HashMap<CharSequence, CharSequence>();
	int fridgeCount = 0; //Variable to give clients unique names
	private Map<CharSequence, CharSequence> connectedTS = new HashMap<CharSequence, CharSequence>();
	int tsCount = 0; //Variable to give clients unique names
	private ArrayList<TemperatureMeasurementRecord> temperatures = new ArrayList<TemperatureMeasurementRecord>();
	private Map<CharSequence, Boolean> userlocation = new HashMap<CharSequence, Boolean>();	//Maps a user to a location (1 = outside, 0 = inside)
	ServerHeartbeatMaintainer heartbeat = new ServerHeartbeatMaintainer(this);
	Thread heartbeatThread = new Thread(heartbeat);
	private static boolean stayOpen=true;
	SaslSocketServer server;
	
	public ServerImpl(){
		try {
			Thread server1 = new Thread(new NetworkDiscoveryServer());
			server1.start();
			server = new SaslSocketServer(new SpecificResponder(ServerProtocol.class, this),new InetSocketAddress(InetAddress.getLocalHost(), 6789));
			server.start();
		} catch (IOException e) {
			System.err.println("[error]: Failed to start server");
			e.printStackTrace(System.err);
			System.exit(1);
		}
		
		while(stayOpen){
			
		}
		server.close();
	}
	
	public ServerImpl(ReplicationData data){
		this.connectedUsers = data.getConnectedUsers();
		this.connectedLights = data.getConnectedLights();
		this.connectedFridges = data.getConnectedFridges();
		this.connectedTS = data.getConnectedTS();
		this.temperatures = new ArrayList<TemperatureMeasurementRecord>();
		List<TemperatureAggregate> temperaturestemp = data.getTemperatures();
		for(TemperatureAggregate x : temperaturestemp){
			TemperatureMeasurementRecord newrecord = new TemperatureMeasurementRecord(x);
			this.temperatures.add(newrecord);
		}
		this.userlocation = data.getUserlocation();
		
		try {
			Thread server1 = new Thread(new NetworkDiscoveryServer());
			server1.start();
			server = new SaslSocketServer(new SpecificResponder(ServerProtocol.class, this),new InetSocketAddress(InetAddress.getLocalHost(), 6789));
			server.start();
			//Update heartbeat to have beats for each user
			
		} catch (IOException e) {
			System.err.println("[error]: Failed to start server");
			e.printStackTrace(System.err);
			System.exit(1);
		}
		
		while(stayOpen){
			
		}
		server.close();
	}
	
	@Override
	public CharSequence enter(CharSequence type, CharSequence ip) throws AvroRemoteException {
			if(heartbeatThread.getState() == Thread.State.NEW){
				heartbeatThread.start();
			}
			System.out.println("Client coming in");
			String name = "";
		switch (type.toString()) {
			case "light":
				if(connectedLights.containsValue(ip)){
					break;
				}
				name = "Light" + lightCount;
				lightCount++;
				connectedLights.put(name, ip);
				heartbeat.updateClient(name);
				break;
			case "temperature sensor":
				if(connectedTS.containsValue(ip)){
					break;
				}
				name = "TS" + tsCount;
				tsCount++;
				connectedTS.put(name, ip);
				heartbeat.updateClient(name);
				break;
			case "fridge":
				if(connectedFridges.containsValue(ip)){
					break;
				}
				name = "Fridge" + fridgeCount;
				fridgeCount++;
				connectedFridges.put(name, ip);
				heartbeat.updateClient(name);
				break;
			case "user":
				if(connectedUsers.containsValue(ip)){
					break;
				}
				name = "User" + userCount;
				userCount++;
				connectedUsers.put(name, ip);
				userlocation.put(name,  false);
				heartbeat.updateClient(name);
				break;
			}
		
		//Send update to all clients/fridges
		for (Entry<CharSequence, CharSequence> entry : connectedUsers.entrySet())
		{
			if(entry.getKey() == name){
				continue;
			}
			try {
				String[] userValue = entry.getValue().toString().split(",");
				Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(userValue[0]), Integer.parseInt(userValue[1])));
				UserProtocol proxy = (UserProtocol) SpecificRequestor.getClient(UserProtocol.class, client);
				proxy.enter(name, ip);
				client.close();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		for (Entry<CharSequence, CharSequence> entry : connectedFridges.entrySet())
		{
			if(entry.getKey() == name){
				continue;
			}
			try {
				String[] userValue = entry.getValue().toString().split(",");
				Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(userValue[0]), Integer.parseInt(userValue[1])));
				FridgeProtocol proxy = (FridgeProtocol) SpecificRequestor.getClient(FridgeProtocol.class, client);
				proxy.enter(name, ip);
				client.close();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		/*
		for (Entry<CharSequence, CharSequence> entry : connectedFridges.entrySet())
		{
			try {
				String[] userValue = entry.getValue().toString().split(",");
				Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(userValue[0]), Integer.parseInt(userValue[1])));
				UserProtocol proxy = (UserProtocol) SpecificRequestor.getClient(UserProtocol.class, client);
				System.out.println(proxy.notifyUsers(userName, state));
				client.close();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}*/
		return name;
	}

	@Override
	public CharSequence leave(CharSequence userName) throws AvroRemoteException {
		System.out.println(userName.toString().split("[0-9]")[0]);
		switch (userName.toString().split("[0-9]")[0]) {
		case "Light":
			connectedLights.remove(userName);
			break;
		case "TS":
			connectedTS.remove(userName);
			break;
		case "Fridge":
			connectedFridges.remove(userName);
			break;
		case "User":
			connectedUsers.remove(userName);
			break;
		}

		//Send update to all clients/fridges
		for (Entry<CharSequence, CharSequence> entry : connectedUsers.entrySet())
		{
			try {
				String[] userValue = entry.getValue().toString().split(",");
				Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(userValue[0]), Integer.parseInt(userValue[1])));
				FridgeProtocol proxy = (FridgeProtocol) SpecificRequestor.getClient(FridgeProtocol.class, client);
				proxy.leave(userName);
				client.close();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		for (Entry<CharSequence, CharSequence> entry : connectedFridges.entrySet())
		{
			try {
				String[] userValue = entry.getValue().toString().split(",");
				Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(userValue[0]), Integer.parseInt(userValue[1])));
				UserProtocol proxy = (UserProtocol) SpecificRequestor.getClient(UserProtocol.class, client);
				proxy.leave(userName);
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
		
		for (Entry<CharSequence, CharSequence> entry : connectedUsers.entrySet())
		{
			String name = entry.getKey() + ", Location: " + (userlocation.get(entry.getKey()) ? "Outside" : "Inside");
			clients.add(name);
		}
		
		for (Entry<CharSequence, CharSequence> entry : connectedLights.entrySet())
		{
			String name = (String) entry.getKey();
			clients.add(name);
		}
		
		for (Entry<CharSequence, CharSequence> entry : connectedFridges.entrySet())
		{
			String name = (String) entry.getKey();
			clients.add(name);
		}
		
		for (Entry<CharSequence, CharSequence> entry : connectedTS.entrySet())
		{
			String name = (String) entry.getKey();
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
				String[] lightValue =light.getValue().toString().split(",");
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
	public CharSequence changeLightState(CharSequence lightName)throws AvroRemoteException {
		boolean status = false;
		try {
			String[] lightValue =connectedLights.get(lightName.toString()).toString().split(",");
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(lightValue[0]), Integer.parseInt(lightValue[1])));
			LightProtocol proxy = (LightProtocol) SpecificRequestor.getClient(LightProtocol.class, client);

			status = proxy.changeState();

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
		if (status) {
			return lightName + " is now on";
		} else {
			return lightName + " is now ";
		}
	}

	@Override
	public int showCurrentHouseTemp() throws AvroRemoteException {
		if(temperatures.isEmpty()){
			throw new AvroRuntimeException("NoMeasurementsError");
		}
		return (int) temperatures.get(temperatures.size()-1).record.getTemperature().intValue();
	}

	@Override
	public Map<CharSequence, Integer> showTempHistory() throws AvroRemoteException {
		if(temperatures.isEmpty()){
			throw new AvroRuntimeException("NoMeasurementsError");
		}
		Map<CharSequence, Integer> returnmap = new HashMap<CharSequence, Integer>();
		for(TemperatureMeasurementRecord record: temperatures){
			returnmap.put(record.record.time, (int) record.record.temperature);
		}
		return returnmap;
	}

	@Override
	public CharSequence connectUserToFridge(CharSequence fridgeName)throws AvroRemoteException {
		//Add concurency on a later date
		System.out.println(connectedFridges.get(fridgeName.toString()));
		return connectedFridges.get(fridgeName.toString());
	}

	@Override
	public List<CharSequence> showConnectedFridges() throws AvroRemoteException {
		List<CharSequence> fridges = new ArrayList<CharSequence>();
		
		for (Entry<CharSequence, CharSequence> entry : connectedFridges.entrySet())
		{
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
				if(record.isTime(sensorValue.getTime().toString())){
					record.addTemperature(temperature, sensorValue.getTime().toString());
					TA = record.getAggregate();
					System.out.println(record.getCounter());
					Exists = true;
					break;
				}
		    }
			
			if(!Exists){
				//No measurements for time x have been made yet, so add it
				TemperatureMeasurementRecord temprecord = new TemperatureMeasurementRecord();
				temprecord.addTemperature(sensorValue.temperature, sensorValue.time.toString());
				TA = temprecord.getAggregate();
				temperatures.add(temprecord);
			}
			
			//Send update to all clients/fridges
			for (Entry<CharSequence, CharSequence> entry : connectedUsers.entrySet())
			{
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
			
			for (Entry<CharSequence, CharSequence> entry : connectedFridges.entrySet())
			{
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
	public List<CharSequence> getFridgeInventory(CharSequence fridgeName)throws AvroRemoteException {
		String[] fridgeValue = connectedFridges.get(fridgeName.toString()).toString().split(",");
		
		List<CharSequence> inventory=new ArrayList<CharSequence>();
		try {
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(fridgeValue[0]), Integer.parseInt(fridgeValue[1])));
			FridgeProtocol proxy = (FridgeProtocol) SpecificRequestor.getClient(FridgeProtocol.class, client);
			inventory=proxy.getInventory();
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
	public CharSequence notifyUsersOfEmptyFridge(CharSequence fridgeName)throws AvroRemoteException {
		for (Entry<CharSequence, CharSequence> entry : connectedUsers.entrySet())
		{
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
		if(!connectedUsers.containsKey(userName.toString())){
			throw new AvroRuntimeException("User hasnt joined the system yet");
		}
		userlocation.put(userName.toString(), false);
		notifyUsers(userName, " entered ");
		return true;
	}

	@Override
	public boolean leaveHouse(CharSequence userName) throws AvroRemoteException {
		if(!connectedUsers.containsKey(userName.toString())){
			throw new AvroRuntimeException("User hasnt joined the system yet");
		}
		userlocation.put(userName.toString(), true);
		notifyUsers(userName, " left ");
		return true;
	}
	
	//Method that will notify all users when someone leaves/enters the house
	//State = "entered" or "left" depending on what the user did
	private void notifyUsers(CharSequence userName, CharSequence state){
		for (Entry<CharSequence, CharSequence> entry : connectedUsers.entrySet())
		{
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
		
		for (Entry<CharSequence, CharSequence> entry : connectedFridges.entrySet())
		{
			try {
				String[] userValue = entry.getValue().toString().split(",");
				Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(userValue[0]), Integer.parseInt(userValue[1])));
				FridgeProtocol proxy = (FridgeProtocol) SpecificRequestor.getClient(FridgeProtocol.class, client);
				if(state == " entered "){
					proxy.enterHouse(userName);
				}else{
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
		for(TemperatureMeasurementRecord x : this.temperatures){
			temp.add(x.getAggregate());
		}
		System.out.println(temp.size());
		return new ReplicationData(this.connectedUsers, this.connectedLights, this.connectedFridges, this.connectedTS, temp, this.userlocation);
	}
}
