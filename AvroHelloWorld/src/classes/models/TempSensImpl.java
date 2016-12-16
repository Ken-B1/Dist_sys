package classes.models;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Random;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.SaslSocketServer;
import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.avro.ipc.specific.SpecificResponder;

import sourcefiles.ServerProtocol;
import sourcefiles.TSProtocol;
import utility.NetworkDiscoveryClient;

public class TempSensImpl implements TSProtocol {
	private double currentTemp;
	private String userName;
	private int portnumber;
	private InetSocketAddress server;
	
	public TempSensImpl(double temperature){
		//Try to connect to server 
		try {
			NetworkDiscoveryClient FindServer = new NetworkDiscoveryClient(this, "Ts");
			server = FindServer.findServer();
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getLocalHost(),6789));
			ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
			
			//Use serversocket to find open socket
			ServerSocket s = new ServerSocket(0);
			portnumber = s.getLocalPort();
			s.close();
			InetSocketAddress socketaddress = new InetSocketAddress(InetAddress.getLocalHost(), portnumber);
			
			userName = proxy.enter("temperature sensor", server.toString().replace(":", ",")).toString();
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
		currentTemp=temperature;
		updateTemperature();
		System.out.println("TempSens created!");
	}
	
	public void setServer(InetSocketAddress address){
		server = address;
	}
	
	private void updateTemperature(){
		//X = time between updates
		long x = 30000;
		while(true){
			//Update temperature with random value between -1 and 1
			currentTemp += Math.random() * 2 -1;
			System.out.println("Current temperature of sensor:" + currentTemp);
			try {
				//Connect to server to send status update
				Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getLocalHost(),6789));
				ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
				proxy.updateTemperature(userName, (int)currentTemp);
				client.close();
				
				Thread.sleep(x);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					Thread.sleep(x);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} catch (Exception e){
				e.printStackTrace();	
				try {
					Thread.sleep(x);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		
	}
}
