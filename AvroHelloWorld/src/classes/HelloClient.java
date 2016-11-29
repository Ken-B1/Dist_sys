package classes;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Scanner;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.SaslSocketServer;
import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.avro.ipc.specific.SpecificResponder;

import avro.hello.proto.ClientHello;
import avro.hello.proto.Hello;

public class HelloClient implements ClientHello {
	private boolean state = false;
	private static String name="";
	public static void main(String[] args){
		
		Scanner keyboard = new Scanner(System.in);
		boolean connect = true;
		Server server = null;
		try {
			server = new SaslSocketServer(new SpecificResponder(ClientHello.class,new HelloClient()),new InetSocketAddress(InetAddress.getLocalHost(),6798));
		} catch (IOException e){
			System.err.println("[error]: Failed to start server");
			e.printStackTrace(System.err);
			System.exit(1);
		}
		server.start();
		System.out.println("If you want to connect, type: 'join'");
		
		try {		
			do{
				switch (keyboard.nextLine()){
					case "join":
						try {	
							Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName("143.169.195.146"),6789));
							Hello proxy = (Hello) SpecificRequestor.getClient(Hello.class, client);
							System.out.println("Please give me your name: ");
							name = keyboard.nextLine();
							CharSequence joinResponse = proxy.enter(name,InetAddress.getLocalHost().getHostAddress());
							System.out.println(joinResponse);
							System.out.println("If you want to leave, type: 'leave'");
							client.close();
							break;
							
							} catch(AvroRemoteException e){
								System.err.println("Error joining");
								e.printStackTrace(System.err);
								System.exit(1);
							}
						
					case "leave":
						try {
							Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName("143.169.195.146"),6789));
							Hello proxy = (Hello) SpecificRequestor.getClient(Hello.class, client);
							CharSequence leaveResponse = proxy.leave(name);
							System.out.println(leaveResponse);
							System.out.println("If you want to connect, type: 'join'");
							client.close();
							break;	
							
						} catch(AvroRemoteException e){
							System.err.println("Error leaving");
							e.printStackTrace(System.err);
							System.exit(1);
						}
						
					case "exit":
						connect=false;	
						//client.close();
						break;
					
					default:
						System.out.println("Type: 'join','leave','exit'");
						break;
				}
			} while (connect);	
			
			try {
				server.join();
			} catch (InterruptedException e){
			}
			
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public CharSequence state(CharSequence username) throws AvroRemoteException {
		System.out.println(username);
		return state+"";
	}

	@Override
	public CharSequence change(CharSequence username)
			throws AvroRemoteException {
		if(state){
			state=false;
			return "Changed state from: true to: false"; 
		} else {
			state=true;
			return "Changed state from: false to: true"; 
		}
	}
}