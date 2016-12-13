package classes.models;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Scanner;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;

import sourcefiles.ServerProtocol;
import sourcefiles.UserProtocol;

public class UserImpl implements UserProtocol {
	private String userName;
	private int portnumber; 
	
	public UserImpl(){
		try {	
			ServerSocket s = new ServerSocket(0);
			portnumber = s.getLocalPort();
			s.close();
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getLocalHost(),6789));
			ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
			userName = proxy.enter("user",InetAddress.getLocalHost().getHostAddress() + "," + portnumber).toString();
			System.out.println(userName);
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
		//Method that will request the content of a certain fridge
		try {	
			Scanner keyboard = new Scanner(System.in);
			System.out.println("Give fridge name");		
			String selectedType = keyboard.nextLine();
			
			
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getLocalHost(),6789));
			ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
			proxy.showFridgeInventory("xx").toString();
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
		//Method that will open a fridge and connect to the fridge directly for a private connection
		try {	
			Scanner keyboard = new Scanner(System.in);
			System.out.println("Give fridge name");		
			String selectedType = keyboard.nextLine();
			
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getLocalHost(),6789));
			ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
			userName = proxy.connectUserToFridge(userName).toString();
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
	
}