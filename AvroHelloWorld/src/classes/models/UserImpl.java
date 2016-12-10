package classes.models;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.SaslSocketServer;
import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.avro.ipc.specific.SpecificResponder;

import sourcefiles.FridgeProtocol;
import sourcefiles.LightProtocol;
import sourcefiles.ServerProtocol;
import sourcefiles.UserProtocol;

public class UserImpl implements UserProtocol {
	private String userName;
	private int portNumber; 
	
	public UserImpl(){
		try {	
			ServerSocket s = new ServerSocket(0);
			portNumber = s.getLocalPort();
			s.close();
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getLocalHost(),6789));
			ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
			userName = proxy.enter("user",InetAddress.getLocalHost().getHostAddress() + "," + portNumber).toString();
			System.out.println(userName);
			client.close();
			Server server = new SaslSocketServer(new SpecificResponder(UserProtocol.class,this),new InetSocketAddress(InetAddress.getLocalHost(),portNumber));
		    server.start();
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
		System.out.println("User created!");
	}
	
	public void requestClients(){
		//Method that will request the other clients from the server and display it to this user
		try {	
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getLocalHost(),6789));
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
		try {	
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getLocalHost(),6789));
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
		try {	
			Scanner keyboard = new Scanner(System.in);
			System.out.println("Give light name");		
			String selectedType = keyboard.nextLine();
			
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getLocalHost(),6789));
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
		Scanner keyboard = new Scanner(System.in);
		
		//Method that will request the content of a certain fridge
		try {	
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getLocalHost(),6789));
			ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
			List<String> fridges= new ArrayList<String>();
			
			for(CharSequence fridge:proxy.showConnectedFridges()){
				fridges.add(fridge.toString());
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
		//Method that will request the content of a certain fridge
		try {	
			String fridgeName="";
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getLocalHost(),6789));
			ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
			List<String> fridges= new ArrayList<String>();
			
			for(CharSequence fridge:proxy.showConnectedFridges()){
				fridges.add(fridge.toString());
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
		try {	
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getLocalHost(),6789));
			ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
			System.out.println(proxy.showCurrentHouseTemp());
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
	
	public void getTemperatureHistory(){
		//Method that will request the history of temperatures in the house
		try {	
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getLocalHost(),6789));
			ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
			userName = proxy.showTempHistory().toString();
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

	@Override
	public CharSequence notifyOfEmptyFridge(CharSequence fridgeName)throws AvroRemoteException {
		System.out.println(fridgeName +" is empty!!");
		return userName + " received empty fridge";
	}
	
}
