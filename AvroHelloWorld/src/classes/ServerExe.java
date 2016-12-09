package classes;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;

import org.apache.avro.AvroRemoteException;
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

public class ServerExe implements ServerProtocol{
		private static Map<String,CharSequence> connectedUsers = new HashMap<String,CharSequence>();
		private static Map<String,CharSequence> connectedLights = new HashMap<String,CharSequence>();
		private static Map<String,CharSequence> connectedFridges = new HashMap<String,CharSequence>();
		private static Map<String,CharSequence> connectedTS = new HashMap<String,CharSequence>();
		
		private Map<String, Integer> temperatures = new HashMap<String, Integer>();
		
		private static Scanner keyboard = new Scanner(System.in);
		public static void main(String[] args){
		
			Server server = null;
			try {
				System.out.println(InetAddress.getLocalHost());
				server = new SaslSocketServer(new SpecificResponder(ServerProtocol.class,new ServerExe()),new InetSocketAddress(InetAddress.getLocalHost(),6789));
			} catch (IOException e){
				System.err.println("[error]: Failed to start server");
				e.printStackTrace(System.err);
				System.exit(1);
			}
			
			server.start();
			
			do {
				System.out.println("'exit' to quit");
			} while (keyboard.nextLine().equals("exit"));
			
			server.close();
		/*boolean getList = true;
		Server server = null;
		try {
			System.out.println(InetAddress.getLocalHost());
			server = new SaslSocketServer(new SpecificResponder(ServerProtocol.class,new ServerExe()),new InetSocketAddress(InetAddress.getLocalHost(),6789));
		} catch (IOException e){
			System.err.println("[error]: Failed to start server");
			e.printStackTrace(System.err);
			System.exit(1);
		}
		server.start();
		
		System.out.println("If you want to get a list of all the connected devices, type: 'list'");
		do{
			switch (keyboard.nextLine()){
			case "list":
				System.out.println("List incoming:");
				System.out.println(connectedClients.values().size());
				break;
			case "getstate":
				try {
					if(connectedClients.size()>0){
						String selectedClient = selectConnectedClient();
						if(!selectedClient.equals("")){
						System.out.println("Getting the state for: " + selectedClient);
						Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(connectedClients.get(selectedClient)),6798));
						ClientHello proxy = (ClientHello) SpecificRequestor.getClient(ClientHello.class, client);
						CharSequence response = proxy.state("Hello there");
						System.out.println("Response:");
						System.out.println(response);
						client.close();
					}else {
						System.out.println("There are no connected clients! Try again later");
					}
					break;
					}
				}catch (UnknownHostException e) {
					e.printStackTrace();
				}catch (IOException e) {
					e.printStackTrace();
				}
			case "state":
				try {
					if(connectedClients.size()>0){
						String selectedClient = selectConnectedClient();
						System.out.println("Changing the state for: " + selectedClient);
						Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(connectedClients.get(selectedClient)),6798));
						ClientHello proxy = (ClientHello) SpecificRequestor.getClient(ClientHello.class, client);
						CharSequence response = proxy.change("Hello there");
						System.out.println("Response:");
						System.out.println(response);
						client.close();
					}else {
						System.out.println("There are no connected clients! Try again later");
					}
					break;
				}catch (UnknownHostException e) {
					e.printStackTrace();
				}catch (IOException e) {
					e.printStackTrace();
				}
			default: 
				getList=false;
				break;
			}
		} while (getList);
			try {
				server.join();
			} catch (InterruptedException e){
			}*/
	}

	@Override
	public CharSequence enter(CharSequence type, CharSequence ip) throws AvroRemoteException {
		System.out.println("Client coming in");
		String name="";
		switch(type.toString()){
		case "light":
			name = "Light"+connectedLights.size();
			connectedLights.put(name, ip);
			break;
		case "temperature sensor":
			name = "TS"+connectedTS.size();
			connectedTS.put(name, ip);
			break;
		case "fridge": 
			name = "Fridge"+connectedFridges.size();
			connectedFridges.put(name, ip);
			break;
		case "user": 
			name = "User"+connectedUsers.size();
			connectedUsers.put(name, ip);
			break;
		}
		return name;
	}

	@Override
	public CharSequence leave(CharSequence userName) throws AvroRemoteException {
		System.out.println(userName.toString().split("[0-9]")[0]);
		switch(userName.toString().split("[0-9]")[0]){
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
	public List<CharSequence> getLightStatuses() throws AvroRemoteException {	
		System.out.print("portnumber:");
		int port = keyboard.nextInt();
		System.out.println(keyboard.nextLine());
		
		List<CharSequence> lightStatuses = new ArrayList<CharSequence>();
		
		Set<Map.Entry<String, CharSequence>> set = connectedLights.entrySet();

		for (Map.Entry<String, CharSequence> light : set) {
			try {	
				Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(light.getValue().toString()),port));
				LightProtocol proxy = (LightProtocol) SpecificRequestor.getClient(LightProtocol.class, client);
				boolean status = proxy.getState();
				if(status){
					lightStatuses.add(light.getKey() + " is on");
				} else {
					lightStatuses.add(light.getKey() + " is off");
				}
				System.out.println("Received status: " + status + ", from light: " + light.getKey());
				
				client.close();
				
			} catch(AvroRemoteException e){
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
		boolean status=false;
		System.out.print("portnumber:");
		int port = keyboard.nextInt();
		System.out.println(keyboard.nextLine());
		
		
		try {	
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(connectedLights.get(lightName.toString()).toString()),port));
			LightProtocol proxy = (LightProtocol) SpecificRequestor.getClient(LightProtocol.class, client);
			
			status = proxy.changeState();
			
			client.close();
			
		} catch(AvroRemoteException e){
			System.err.println("Error joining");
			e.printStackTrace(System.err);
			System.exit(1);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(status){
			return lightName + " is now on";
		} else {
			return lightName + " is now ";
		}
	}

	@Override
	public CharSequence showFridgeInventory(CharSequence fridgeName) throws AvroRemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CharSequence showCurrentHouseTemp() throws AvroRemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<CharSequence, Integer> showTempHistory() throws AvroRemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CharSequence connectUserToFridge(CharSequence fridgeName) throws AvroRemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CharSequence> showConnectedFridges() throws AvroRemoteException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Void updateTemperature(CharSequence sensorName, int sensorValue) throws AvroRemoteException {
		if(temperatures.containsKey(sensorName)){
			temperatures.put((String) sensorName, sensorValue);
		}
		return null;
	}

	@Override
	public CharSequence requestShowEmptyFridge(CharSequence fridgeName) throws AvroRemoteException {
		// TODO Auto-generated method stub
		return null;
	}
	
}

