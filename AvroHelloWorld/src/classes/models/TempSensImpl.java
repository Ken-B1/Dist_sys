package classes.models;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.Vector;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.SaslSocketServer;
import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.avro.ipc.specific.SpecificResponder;

import sourcefiles.TemperatureRecord;
import sourcefiles.ServerProtocol;
import sourcefiles.TSProtocol;
import utility.NetworkDiscoveryClient;

public class TempSensImpl implements TSProtocol {
	private String userName;
	private int portnumber;
	private InetSocketAddress server;
	private boolean serverFound;
	private Vector<TemperatureRecord> temperatures;
	
	public TempSensImpl(double temperature){
		serverFound = false;
		temperatures = new Vector<TemperatureRecord>();
		//Try to connect to server 
		try {
			NetworkDiscoveryClient FindServer = new NetworkDiscoveryClient(this, "Ts");
			server = FindServer.findServer();
			serverFound = true;
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getLocalHost(),6789));
			ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
			
			//Use serversocket to find open socket
			ServerSocket s = new ServerSocket(0);
			portnumber = s.getLocalPort();
			s.close();
			String newuserName = proxy.enter("temperature sensor", InetAddress.getLocalHost().toString() + "," + portnumber).toString();
			if(newuserName != ""){
				userName = newuserName;
			}
			System.out.println(userName);
			client.close();
			//Start the procedure of updating temperature and sending it to the server
		} catch(AvroRemoteException e){
			System.err.println("Error joining");
			e.printStackTrace(System.err);
			System.exit(1);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketTimeoutException e){
			System.out.println("Server isnt found");
			serverFound = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
		LocalTime currenttime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
		TemperatureRecord newtemp = new TemperatureRecord(currenttime.toString(), temperature);
		temperatures.addElement(newtemp);
		sendToServer(newtemp);
		updateTemperature();
		System.out.println("TempSens created!");
	}
	
	public void setServer(InetSocketAddress address){
		server = address;
	}
	
	private void updateTemperature(){
		//X = time between updates = 1 min
		long x = 5000;
		while(true){	
			//Update temperature with random value between -1 and 1
			LocalTime currentTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
			if(!currentTime.equals(LocalTime.parse(temperatures.lastElement().time) ) ){
				//A minute has gone by so take new temperature measurement
				double currentTemp = temperatures.lastElement().temperature + (Math.random() * 2 -1);
				TemperatureRecord newrecord = new TemperatureRecord(currentTime.toString(),currentTemp);
				temperatures.addElement(newrecord);
				System.out.println("Current temperature of sensor:" + currentTemp);
			}
			
			try {
				Thread.sleep(x);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	}
	
	private void sendToServer(TemperatureRecord record){
		System.out.println("test");
		//First check if the server has been found yet
		if(!serverFound){
			try{
				NetworkDiscoveryClient FindServer = new NetworkDiscoveryClient(this, "Ts");
				server = FindServer.findServer();
				serverFound = true;
			} catch (IOException e) {
				System.out.println("Server isnt found");
				serverFound = false;
			}
		}
		
		//If server has been found, try to send new temperature
		if(serverFound){
			try {
				//Connect to server to send status update and join
				Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getLocalHost(),6789));
				ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
				String newuserName = proxy.enter("temperature sensor", InetAddress.getLocalHost().toString() + "," + portnumber).toString();
				if(newuserName != ""){
					userName = newuserName;
				}
				System.out.println(userName);
				proxy.updateTemperature(userName, record);
				client.close();
			} catch (Exception e){
				e.printStackTrace();	
			}
		}
	}
}
