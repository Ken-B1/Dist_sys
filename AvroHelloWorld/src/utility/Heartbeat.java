package utility;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;

import sourcefiles.ServerProtocol;

public class Heartbeat implements Runnable{
	//Method that will send periodical heartbeats to establish liveliness to the server
	//As soon as a server has been found heartbeats should be sent periodically
	//As soon as the server is lost, heartbeats should stop, because they are useless at that point
	boolean isRunning;
	//SocketAddress for server
	InetSocketAddress server;
	//Name for device that is attached to this heartbeat
	String userName;
	
	public Heartbeat(){
		this.isRunning = false;
		this.server = new InetSocketAddress("0.0.0.0", 0);
		this.userName = "";
	}
	
	public Heartbeat(InetSocketAddress server, String userName){
		this.isRunning = false;
		this.server=server;
		this.userName = userName;
	}
	
	@Override
	public void run(){
		while(true){
			if(this.server.toString() != "/0.0.0.0:0" && userName != ""){		
				try{
					Transceiver client = new SaslSocketTransceiver(server);
					ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
					System.out.println(userName);
					proxy.showHeartbeat(userName);
					client.close();
				} catch(IOException e){
					//Something went wrong while trying to send heartbeats
					throw new RuntimeException("Couldn't find server during heartbeat");
				}
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	//Method to set the server
	public void setServer(InetSocketAddress server){
		this.server = server;
	}
	
	//Method to update username
	public void setuserName(String userName){
		this.userName = userName;
	}
	
	//Method to check if heartbeat is valid
	public boolean isValid(InetSocketAddress server, String userName){
		if(server == this.server && userName == this.userName){
			return true;
		}else{
			return false;
		}
	}
}
