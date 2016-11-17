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

import avro.hello.proto.ClientAvro;
import avro.hello.proto.ClientHello;
import avro.hello.proto.Hello;

public class HelloServer implements Hello{
		private static Map<String,String> connectedClients = new HashMap<String,String>();
		private static Scanner keyboard = new Scanner(System.in);
		public static void main(String[] args){
		
		boolean getList = true;
		Server server = null;
		try {
			System.out.println(InetAddress.getLocalHost());
			server = new SaslSocketServer(new SpecificResponder(Hello.class,new HelloServer()),new InetSocketAddress(InetAddress.getLocalHost(),6789));
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
			}
	}	

	@Override
	public CharSequence leave(CharSequence username) throws AvroRemoteException {
		System.out.println( username + " has left");
		connectedClients.remove(username.toString());
		return "Goodbye " + username+ ". Hope to see you back soon!";
	}

	@Override
	public CharSequence enter(CharSequence username,CharSequence ip){ 
		System.out.println( username + " has connected");
		//try {
			System.out.println("connecting to ip: "+ip.toString());
			/*Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName(ip.toString()),6798));
			System.out.println("We made client: " + username);
			ClientAvro proxy = (ClientAvro) SpecificRequestor.getClient(ClientAvro.class, client);*/
			connectedClients.put(username.toString(),ip.toString());
		/*} catch(IOException e){
			System.err.println("Error connecting to server");
			e.printStackTrace(System.err);
			System.exit(1);
		}*/
		return "Welcome " + username;
	}
	
	private static String selectConnectedClient(){
		String selectedClient;
		do {
			selectedClient="";
			System.out.println("Choose from following clients: ");
			Set<Map.Entry<String,String>> connectedClientSet = connectedClients.entrySet();
			for(Map.Entry<String, String> c : connectedClientSet){
				System.out.println(c.getKey());
			}
			System.out.print("Typ the name of the client: ");
			selectedClient=keyboard.nextLine();
		} while (!connectedClients.containsKey(selectedClient));
		return selectedClient;
	}
}