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

import sourcefiles.FridgeProtocol;
import sourcefiles.LightProtocol;
import sourcefiles.ServerProtocol;
import sourcefiles.TSProtocol;
import sourcefiles.UserProtocol;
import classes.models.*;

public class ClientExe {
	static Server server;
	
	public static void main(String[] args) {
		Scanner keyboard = new Scanner(System.in);
		boolean connect = true;
		String userName="";
		String selectedType;
		int port=0;
		
		System.out.println("Welcome!");
		
		System.out.print("portnumber:");
		port = keyboard.nextInt();
		System.out.println(keyboard.nextLine());
		do {
			System.out.println("===============================================");
			System.out.println("Please select one of the following types:");
			System.out.println("");
			System.out.println("*) Light");
			System.out.println("*) Temperature Sensor");
			System.out.println("*) Fridge");
			System.out.println("*) User");
			System.out.println("");
			System.out.print("Type the name of the type you want to select: ");
			
			selectedType = keyboard.nextLine();
		} while(!selectedType.equalsIgnoreCase("light") && !selectedType.equalsIgnoreCase("temperature sensor") && !selectedType.equalsIgnoreCase("fridge") && !selectedType.equalsIgnoreCase("user"));
		
		selectedType = selectedType.toLowerCase();
		
		switch(selectedType){
			case "light":
				System.out.println("Selected the Light");
			try {
				server = new SaslSocketServer(new SpecificResponder(LightProtocol.class,new LightImpl()),new InetSocketAddress(InetAddress.getLocalHost(),port));
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
				break;
			case "temperature sensor":
				System.out.println("Selected the Temperature Sensor");
				try {
					server = new SaslSocketServer(new SpecificResponder(TSProtocol.class,new TempSensImpl(2)),new InetSocketAddress(InetAddress.getLocalHost(),port));
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case "fridge": 
				System.out.println("Selected the Fridge");
				try {
					server = new SaslSocketServer(new SpecificResponder(FridgeProtocol.class,new FridgeImpl()),new InetSocketAddress(InetAddress.getLocalHost(),port));
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case "user": 
				System.out.println("Selected the User");
				try {
					server = new SaslSocketServer(new SpecificResponder(UserProtocol.class,new UserImpl()),new InetSocketAddress(InetAddress.getLocalHost(),port));
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
		}
		server.start();
		System.out.println("Type join, leave or exit");
		do{
			switch (keyboard.nextLine()){
				case "join":
					try {	
						Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName("143.169.195.85"),6789));
						ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
						System.out.println("Server made");
						userName = proxy.enter(selectedType,InetAddress.getLocalHost().getHostAddress(), 0).toString();
						System.out.println("Your userName is: " + userName);
						System.out.println("If you want to leave, type: 'leave'");
						client.close();
						break;
						
					} catch(AvroRemoteException e){
						System.err.println("Error joining");
						e.printStackTrace(System.err);
						System.exit(1);
					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				case "leave":
					try {
						Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName("143.169.195.85"),6789));
						ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
						CharSequence leaveResponse = proxy.leave(userName);
						System.out.println(leaveResponse);
						System.out.println("If you want to connect, type: 'join'");
						client.close();
						break;	
						
					} catch(AvroRemoteException e){
						System.err.println("Error leaving");
						e.printStackTrace(System.err);
						System.exit(1);
					}catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				case "exit":
					connect=false;	
					break;
					
				case "lightStates":
					try {
						Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName("143.169.195.85"),6789));
						ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
						List<CharSequence> lightStatuses = proxy.getLightStatuses();
						client.close();
						
						for(CharSequence lightStatus:lightStatuses){
							System.out.println(lightStatus);
						}
						break;	
						
					} catch(AvroRemoteException e){
						System.err.println("Error leaving");
						e.printStackTrace(System.err);
						System.exit(1);
					}catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}	
				
				default:
					System.out.println("Type: 'join','leave','exit'");
					break;
			}
		} while (connect);	
		
		server.close();
	}
}
