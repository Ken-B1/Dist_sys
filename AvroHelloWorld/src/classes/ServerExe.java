package classes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.Map.Entry;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.AvroRuntimeException;
import org.apache.avro.ipc.SaslSocketServer;
import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.avro.ipc.specific.SpecificResponder;

import classes.models.FridgeImpl;
import classes.models.LightImpl;
import classes.models.TempSensImpl;
import classes.models.UserImpl;
import sourcefiles.FridgeProtocol;
import sourcefiles.LightProtocol;
import sourcefiles.ServerProtocol;
import sourcefiles.TSProtocol;
import sourcefiles.UserProtocol;
import sourcefiles.TemperatureRecord;
import utility.NetworkDiscoveryServer;
import utility.TemperatureMeasurementRecord;


public class ServerExe implements ServerProtocol {
	private static Map<String, CharSequence> connectedUsers = new HashMap<String, CharSequence>();
	private static Map<String, CharSequence> connectedLights = new HashMap<String, CharSequence>();
	private static Map<String, CharSequence> connectedFridges = new HashMap<String, CharSequence>();
	private static Map<String, CharSequence> connectedTS = new HashMap<String, CharSequence>();
	private Vector<TemperatureMeasurementRecord> temperatures = new Vector<TemperatureMeasurementRecord>();
	private Map<String, Boolean> userlocation = new HashMap<String, Boolean>();	//Maps a user to a location (1 = outside, 0 = inside)
	private static Scanner keyboard = new Scanner(System.in);
	private static boolean stayOpen=true;
	
	public static void main(String[] args) {
		Server server = null;		
		try {
			Thread server1 = new Thread(new NetworkDiscoveryServer());
			server1.start();
			server = new SaslSocketServer(new SpecificResponder(ServerProtocol.class, new ServerExe()),new InetSocketAddress(InetAddress.getLocalHost(), 6789));
		} catch (IOException e) {
			System.err.println("[error]: Failed to start server");
			e.printStackTrace(System.err);
			System.exit(1);
		}
		server.start();
		int choice = 0;
		/*do {
			System.out.println("1) get light states");
			System.out.println("0) quit");
			choice = Integer.parseInt(keyboard.nextLine());
			switch (choice) {
			case 1:
				Set<Map.Entry<String, CharSequence>> set = connectedLights.entrySet();
				String lightChoice;
				do {
					for (Map.Entry<String, CharSequence> light : set) {
						System.out.println(light.getKey());
					}
					System.out.print("type the name of a light you want to connect to:");
					lightChoice = keyboard.nextLine();
				} while (!connectedLights.containsKey(lightChoice));
				Transceiver client;
				try {
					System.out.println(connectedLights.get(lightChoice).toString());
					client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(connectedLights.get(lightChoice).toString()), 6798));
					LightProtocol proxy = (LightProtocol) SpecificRequestor.getClient(LightProtocol.class, client);
					System.out.println(proxy.getState());
					client.close();
					break;
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} while (choice != 0);*/
		
		while(stayOpen){
			
		}
		server.close();
	}

	@Override
	public CharSequence enter(CharSequence type, CharSequence ip) throws AvroRemoteException {
			System.out.println("Client coming in");
			String name = "";
			switch (type.toString()) {
			case "light":
				if(connectedLights.containsValue(ip)){
					break;
				}
				name = "Light" + connectedLights.size();
				connectedLights.put(name, ip);
				break;
			case "temperature sensor":
				if(connectedTS.containsValue(ip)){
					break;
				}
			name = "TS" + connectedTS.size();
			connectedTS.put(name, ip);
			break;
		case "fridge":
			if(connectedFridges.containsValue(ip)){
				break;
			}
			name = "Fridge" + connectedFridges.size();
			connectedFridges.put(name, ip);
			break;
		case "user":
			if(connectedUsers.containsValue(ip)){
				break;
			}
			name = "User" + connectedUsers.size();
			connectedUsers.put(name, ip);
			userlocation.put(name,  false);
			break;
		}
		return name;
	}

	@Override
	public CharSequence leave(CharSequence userName) throws AvroRemoteException {
		System.out.println(userName.toString().split("[0-9]")[0]);
		switch (userName.toString().split("[0-9]")[0]) {
		case "Light":
			connectedLights.remove(userName.toString());
			break;
		case "TS":
			connectedTS.remove(userName.toString());
			break;
		case "Fridge":
			connectedFridges.remove(userName.toString());
			break;
		case "User":
			connectedUsers.remove(userName.toString());
			break;
		}

		return userName + " has left";
	}

	@Override
	public List<CharSequence> getClients() throws AvroRemoteException {
		// TODO Auto-generated method stub
		
		List<CharSequence> clients = new ArrayList<CharSequence>();
		
		for (Entry<String, CharSequence> entry : connectedUsers.entrySet())
		{
			String name = entry.getKey() + ", Location: " + (userlocation.get(entry.getKey()) ? "Outside" : "Inside");
			clients.add(name);
		}
		
		for (Entry<String, CharSequence> entry : connectedLights.entrySet())
		{
			String name = entry.getKey();
			clients.add(name);
		}
		
		for (Entry<String, CharSequence> entry : connectedFridges.entrySet())
		{
			String name = entry.getKey();
			clients.add(name);
		}
		
		for (Entry<String, CharSequence> entry : connectedTS.entrySet())
		{
			String name = entry.getKey();
			clients.add(name);
		}
		
		return clients;
	}

	@Override
	public List<CharSequence> getLightStatuses() throws AvroRemoteException {	
		List<CharSequence> lightStatuses = new ArrayList<CharSequence>();

		Set<Map.Entry<String, CharSequence>> set = connectedLights.entrySet();

		for (Map.Entry<String, CharSequence> light : set) {
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
		int currenttemperature = 0;
		int counter = 0;
		if(temperatures.size() == 0){
			throw new AvroRuntimeException("NoMeasurementsError");
		}
		return (int) temperatures.lastElement().record.temperature;
	}

	@Override
	public Map<CharSequence, Integer> showTempHistory() throws AvroRemoteException {
		if(temperatures.size() == 0){
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
		
		for (Entry<String, CharSequence> entry : connectedFridges.entrySet())
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
		double temperature = sensorValue.temperature;
		
		if (connectedTS.containsKey(sensorName.toString())) {
			boolean Exists = false;
			for (TemperatureMeasurementRecord record : temperatures) {
				if(record.isTime(sensorValue.time.toString())){
					record.addTemperature(sensorValue.temperature, sensorValue.time.toString());
					Exists = true;
					break;
				}
		    }
			
			if(!Exists){
				System.out.println("test");
				//No measurements for time x have been made yet, so add it
				TemperatureMeasurementRecord temprecord = new TemperatureMeasurementRecord();
				temprecord.addTemperature(sensorValue.temperature, sensorValue.time.toString());
				temperatures.add(temprecord);
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
		for (Entry<String, CharSequence> entry : connectedUsers.entrySet())
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
		return true;
	}

	@Override
	public boolean leaveHouse(CharSequence userName) throws AvroRemoteException {
		if(!connectedUsers.containsKey(userName.toString())){
			throw new AvroRuntimeException("User hasnt joined the system yet");
		}
		userlocation.put(userName.toString(), true);
		return true;
	}
}
