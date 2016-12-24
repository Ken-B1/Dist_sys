package classes.models;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.AvroRuntimeException;
import org.apache.avro.ipc.SaslSocketServer;
import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.avro.ipc.specific.SpecificResponder;

import sourcefiles.FridgeProtocol;
import sourcefiles.LightProtocol;
import sourcefiles.ServerProtocol;
import sourcefiles.TemperatureAggregate;
import sourcefiles.TemperatureRecord;
import sourcefiles.UserProtocol;
import sourcefiles.ReplicationData;
import utility.Heartbeat;
import utility.NetworkDiscoveryClient;
import utility.ReplicationGenerator;
import utility.TemperatureMeasurementRecord;

public class UserImpl implements UserProtocol{
	private String userName;
	private int portNumber; 
	private InetSocketAddress server;
	private boolean serverFound;
	private Heartbeat heartbeat;
	private Thread heartbeatThread;
	Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
	    public void uncaughtException(Thread th, Throwable ex) {
	    	//Catches the exceptions thrown by the heartbeat thread(indicating server wasnt found)
	        System.out.println("Couldnt find server during heartbeat");
	        server = new InetSocketAddress("0.0.0.0", 0);
	        serverFound = false;
	        connectToServer();
	    }
	};
	
	private ReplicationData repdata;
	
	public UserImpl(){
		userName = "";
		heartbeat = new Heartbeat();
		//Use the networkdiscoveryutility to find the server
		connectToServer();
		//Start the procedure of updating temperature and sending it to the server
		
		//Create a server for this client
		Server server;
		try {
			server = new SaslSocketServer(new SpecificResponder(UserProtocol.class,this),new InetSocketAddress(InetAddress.getLocalHost(),portNumber));
		    server.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("User created!");
	}
	
	public void requestClients(){
		//Method that will request the other clients from the server and display it to this user
		if(!serverFound){
			connectToServer();
		}
	
		
		try {	
			Transceiver client = new SaslSocketTransceiver(server);
			ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
			List<CharSequence> clients = proxy.getClients();
			for(CharSequence x : clients){
				String type = x.toString().split("[0-9]")[0];
				System.out.println(type + ": " + x.toString());
			}
			client.close();
			//Start the procedure of updating temperature and sending it to the server
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
	
	public void requestLights(){
		//Method that requests all lights from the server
		if(!serverFound){
			connectToServer();
		}
	
		
		try {	
			Transceiver client = new SaslSocketTransceiver(server);
			ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
			System.out.println(proxy.getLightStatuses().toString());
			client.close();
			//Start the procedure of updating temperature and sending it to the server
		} catch(AvroRemoteException e){
			System.err.println("Error joining");
			e.printStackTrace(System.err);
			System.exit(1);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void switchLight(){
		//Method that will request to switch the status of a light
		if(!serverFound){
			connectToServer();
		}
	
		
		try {	
			Scanner keyboard = new Scanner(System.in);
			System.out.println("Give light name");		
			String selectedType = keyboard.nextLine();
			
			Transceiver client = new SaslSocketTransceiver(server);
			ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
			proxy.changeLightState(selectedType).toString();
			client.close();
			//Start the procedure of updating temperature and sending it to the server
		} catch(AvroRemoteException e){
			System.err.println("Error joining");
			e.printStackTrace(System.err);
			System.exit(1);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void getFridgeContent(){
		//Method that will request the content of a certain fridge
		if(!serverFound){
			connectToServer();
		}
	
		
		try {	
			Scanner keyboard = new Scanner(System.in);
					
			Transceiver client = new SaslSocketTransceiver(server);
			ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
			List<String> fridges= new ArrayList<String>();
			
			for(CharSequence fridge:proxy.showConnectedFridges()){
				fridges.add(fridge.toString());
			}
			
			//Check if the list contains any fridges
			if(fridges.isEmpty()){
				System.out.println("You dont have any smartfridges connected to the server.");
				return;
			}
			String fridgeName;
			do {
				System.out.println("Chose one of the following fridges:");
				
				for(String fridge:fridges){
					System.out.println(fridge);
				}
				fridgeName = keyboard.nextLine();
			} while (!fridges.contains(fridgeName));
			
			System.out.println("Inventory of fridge: " + fridgeName);
			for(CharSequence item: proxy.getFridgeInventory(fridgeName)){
				System.out.println("*) " + item);
			}
			client.close();
			//Start the procedure of updating temperature and sending it to the server
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
	
	public void openFridge(){
		Scanner keyboard = new Scanner(System.in);
		String[] fridgeValue= {};
		//Method that will try to connect to a fridge
		if(!serverFound){
			connectToServer();
		}
	
		
		try {	
			String fridgeName="";
			Transceiver client = new SaslSocketTransceiver(server);
			ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
			List<String> fridges= new ArrayList<String>();
			
			for(CharSequence fridge:proxy.showConnectedFridges()){
				fridges.add(fridge.toString());
			}
			
			//Check if the list contains any fridges
			if(fridges.isEmpty()){
				System.out.println("You dont have any smartfridges connected to the server.");
				return;
			}
			
			do {
				System.out.println("Chose one of the following fridges:");
				
				for(String fridge:fridges){
					System.out.println(fridge);
				}
				fridgeName = keyboard.nextLine();
			} while (!fridges.contains(fridgeName));
			fridgeValue = proxy.connectUserToFridge(fridgeName).toString().split(",");
			
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
		//Method that will open a fridge and connect to the fridge directly for a private connection
		try {	
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(fridgeValue[0]), Integer.parseInt(fridgeValue[1])));
			FridgeProtocol proxy = (FridgeProtocol) SpecificRequestor.getClient(FridgeProtocol.class, client);
			String choice;
			do {
				
				System.out.println("Type one of the following commands: ");
				System.out.println("*) add (put item in fridge)");
				System.out.println("*) take (take item ouf of the fridge)");
				System.out.println("*) exit");
				
				choice=keyboard.nextLine();
				
				switch(choice){
				case "add":
					CharSequence item;
					System.out.print("Type the item you are putting in the fridge:");
					item = keyboard.nextLine();
					System.out.println(proxy.addItem(item));
					break;
				case "take":
					List<CharSequence> items = new ArrayList<CharSequence>();
					for(CharSequence inventoryItem: proxy.getInventory()){
						items.add(inventoryItem.toString());
					}
					if(items.size()>0){
						String selectedItem; 
						System.out.println("Chose one of the following items to take out of the fridge:");
						do{
							for(CharSequence i : items){
								System.out.println("*) " + i); 
							}
							System.out.println("*) exit");
							selectedItem = keyboard.nextLine();
						}while(!items.contains(selectedItem) && !selectedItem.equalsIgnoreCase("exit"));
						if(!selectedItem.equalsIgnoreCase("exit")){
							System.out.println(proxy.removeItem((CharSequence)selectedItem));	
						}
					} else {
						System.out.println("This fridge is empty.");
					}
					break;
				}
				
			} while (!choice.equalsIgnoreCase("exit"));
			client.close();
			//Start the procedure of updating temperature and sending it to the server
		} catch(AvroRemoteException e){
			System.err.println("Error joining");
			e.printStackTrace(System.err);
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void getTemperature(){
		//Method that will request the current temperature of the house
		if(!serverFound){
			connectToServer();
		}
	
		
		try {	
			Transceiver client = new SaslSocketTransceiver(server);
			ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
			System.out.println(proxy.showCurrentHouseTemp());
			client.close();
			//Start the procedure of updating temperature and sending it to the server
		} catch(AvroRuntimeException e){
			//No temperature sensors are added to the system
			System.err.println(e.getMessage());
			System.out.println("Maybe you need to buy some temperature sensors.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void getTemperatureHistory(){
		//Method that will request the history of temperatures in the house
		if(!serverFound){
			connectToServer();
		}
	
		
		
		try {	
			Transceiver client = new SaslSocketTransceiver(server);
			ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
			Map<CharSequence, Integer> temperatures = proxy.showTempHistory();
			client.close();
			
			for(Entry<CharSequence, Integer> entry : temperatures.entrySet()){
				System.out.println("test");
				System.out.println(entry.getKey().toString() + ": " + entry.getValue());
			}
			//Start the procedure of updating temperature and sending it to the server
		} catch(AvroRuntimeException e){
			//No temperature sensors are added to the system
			System.err.println(e.getMessage());
			System.out.println("Maybe you need to buy some temperature sensors.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void enterHouse(){
		//Method to enter the house
		if(!serverFound){
			connectToServer();
		}
	
		
		
		try{
			Transceiver client = new SaslSocketTransceiver(server);
			ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
			proxy.enterHouse(userName);
			client.close();			
		} catch(AvroRemoteException e){
			//User hasnt joined the system yet
			try{
				Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getLocalHost(),6789));
				ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
				userName = proxy.enter("user",InetAddress.getLocalHost().getHostAddress() + "," + portNumber).toString();
				System.out.println(userName);
				client.close();
			} catch(Exception e1){
				System.out.println("Something went wrong while trying to join");
			}
		} catch(IOException e){
			
		}		
	}
	
	public void leaveHouse(){
		//Method to leave the house
		if(!serverFound){
			connectToServer();
		}
	
		
		
		try{
			Transceiver client = new SaslSocketTransceiver(server);
			ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
			proxy.leaveHouse(userName);
			client.close();			
		} catch(AvroRemoteException e){
			//User hasnt joined the system yet
			try{
				Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getLocalHost(),6789));
				ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
				userName = proxy.enter("user",InetAddress.getLocalHost().getHostAddress() + "," + portNumber).toString();
				System.out.println(userName);
				client.close();
			} catch(Exception e1){
				System.out.println("Something went wrong while trying to join");
			}
		} catch(IOException e){
			
		}
	}

	@Override
	public CharSequence notifyOfEmptyFridge(CharSequence fridgeName)throws AvroRemoteException {
		System.out.println(fridgeName +" is empty!!");
		return userName + " received empty fridge";
	}

	@Override
	public Void notifyUsers(CharSequence userName, CharSequence state) throws AvroRemoteException {
		System.out.println(userName.toString() + state + "the house.");
		System.out.println(state);
		System.out.println(state.toString().contentEquals(" entered "));
		if(state.toString().contentEquals(" entered ")){
			this.enterHouse(userName);
		}else{
			this.leaveHouse(userName);
		}
		return null;
	}

	private void connectToServer(){
		//Method that will connect to the server
		while(!serverFound){
			//Make sure the server hasnt been found yet
			try{
				NetworkDiscoveryClient FindServer = new NetworkDiscoveryClient();
				server = FindServer.findServer();
				//Server has been found, so enter it
				ServerSocket s = new ServerSocket(0);
				portNumber = s.getLocalPort();
				s.close();
				
				Transceiver client = new SaslSocketTransceiver(server);
				ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
				userName = proxy.enter("user",InetAddress.getLocalHost().getHostAddress() + "," + portNumber).toString();
				System.out.println(userName);
				
				//Make sure the replicationdata is up to date
				repdata = ReplicationGenerator.generateReplica(proxy.getReplication());
				client.close();
				
				
				serverFound = true;
				heartbeat.setServer(server);
				heartbeat.setuserName(userName);
				heartbeatThread = new Thread(heartbeat);
				heartbeatThread.setUncaughtExceptionHandler(h);
				heartbeatThread.start();
				
			} catch(IOException e){
				System.out.println("Searching for server.");
			}
			//Try again in 1 second
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public Void enter(CharSequence userName, CharSequence ip) throws AvroRemoteException {
		switch (userName.toString().split("[0-9]")[0]) {
		case "Light":
			repdata.getConnectedLights().put(userName.toString(), ip);
			break;
		case "TS":
			repdata.getConnectedTS().put(userName.toString(), ip);
			break;
		case "Fridge":
			repdata.getConnectedFridges().put(userName.toString(), ip);
			break;
		case "User":
			repdata.getConnectedUsers().put(userName.toString(), ip);
			enterHouse(userName);
			break;
		}
		return null;
	}

	@Override
	public Void leave(CharSequence userName) throws AvroRemoteException {
		switch (userName.toString().split("[0-9]")[0]) {
		case "Light":
			repdata.getConnectedLights().remove(userName.toString());
			break;
		case "TS":
			repdata.getConnectedTS().remove(userName.toString());
			break;
		case "Fridge":
			repdata.getConnectedFridges().remove(userName.toString());
			break;
		case "User":
			repdata.getConnectedUsers().remove(userName.toString());
			repdata.getUserlocation().remove(userName.toString());
			break;
		}
		return null;
	}

	@Override
	public Void enterHouse(CharSequence userName) throws AvroRemoteException {
		// TODO Auto-generated method stub
		System.out.println(userName);
		System.out.println(userName.toString());
		System.out.println(repdata.userlocation.containsKey(userName));
		System.out.println(repdata.userlocation.containsKey(userName.toString()));

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
		for(TemperatureAggregate x : repdata.getTemperatures()){
			if(toCompare.contentEquals(x.getRecord().getTime().toString())){
				break;
			}
			index++;
		}
		if(index == repdata.getTemperatures().size()){
			//temperatureaggregate doestn exist yet
			repdata.getTemperatures().add(temperature);
		}else{
			repdata.getTemperatures().set(index, temperature);
		}
		return null; 
	}
	
	public void printAggregate(){
		System.out.println("Users:");
		for (Entry<CharSequence, CharSequence> entry : repdata.connectedUsers.entrySet())
		{
			System.out.println("    " + entry.getKey() + ": " + entry.getValue() +", Location: " +  (repdata.userlocation.get(entry.getKey()) ? "Outside" : "Inside"));
		}
		System.out.println("Lights:");		
		for (Entry<CharSequence, CharSequence> entry : repdata.connectedLights.entrySet())
		{
			System.out.println("    " + entry.getKey());
		}
		System.out.println("Fridges:");		
		for (Entry<CharSequence, CharSequence> entry : repdata.connectedFridges.entrySet())
		{
			System.out.println("    " + entry.getKey());
		}
		System.out.println("Ts:");		
		for (Entry<CharSequence, CharSequence> entry : repdata.connectedTS.entrySet())
		{
			System.out.println("    " + entry.getKey());
		}
		System.out.println("temperatures:");		
		for(TemperatureAggregate x : repdata.temperatures){
			System.out.println("    " + x.counter + ", (" + x.record.time.toString() + ":" + x.record.temperature + ")");
		}
		System.out.println("end________________________________________");
	}
	
}
