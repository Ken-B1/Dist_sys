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
import utility.Heartbeat;
import utility.NetworkDiscoveryClient;

public class TempSensImpl implements TSProtocol {
	private String userName;
	private int portnumber;
	private InetSocketAddress server;
	private boolean serverFound;
	private Vector<TemperatureRecord> temperatures;
	private Heartbeat heartbeat;
	private Thread heartbeatThread;
	
	public TempSensImpl(double temperature) throws InterruptedException{
		heartbeat = new Heartbeat();
		Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
		    public void uncaughtException(Thread th, Throwable ex) {
		    	//Catches the exceptions thrown by the heartbeat thread(indicating server wasnt found)
		        System.out.println("Couldnt find server during heartbeat");
		        server = new InetSocketAddress("0.0.0.0", 0);
		        serverFound = false;
		    }
		};
		heartbeatThread = new Thread(heartbeat);
		heartbeatThread.setUncaughtExceptionHandler(h);
		heartbeatThread.start();
		
		serverFound = false;
		temperatures = new Vector<TemperatureRecord>();
		//Try to connect to server 
		try {
			searchServer();
			//Use serversocket to find open socket
			ServerSocket s = new ServerSocket(0);
			portnumber = s.getLocalPort();
			s.close();
			
			if(serverFound){
				Transceiver client = new SaslSocketTransceiver(server);
				ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
				String newuserName = proxy.enter("temperature sensor", InetAddress.getLocalHost().toString() + "," + portnumber).toString();
				if(newuserName != ""){
					userName = newuserName;
					heartbeat.setuserName(newuserName);
				}
				client.close();
			}
			
			System.out.println(userName);
			//Start the procedure of updating temperature and sending it to the server
		} catch(AvroRemoteException e){
			System.err.println("Something went wrong while trying to join the server");
			e.printStackTrace(System.err);
			System.exit(1);
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
	

	public void searchServer(){
		try{
			NetworkDiscoveryClient FindServer = new NetworkDiscoveryClient();
			server = FindServer.findServer();
			serverFound = true;
			heartbeat.setServer(server);
		} catch(IOException e){
			//Server can't be found
			serverFound = false;
			heartbeat.setServer(new InetSocketAddress("0.0.0.0", 0));
			
		}
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
				sendToServer(newrecord);
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
		//First check if the server has been found yet
		if(!serverFound){
			searchServer();
		}
		
		//If server has been found, try to send new temperature
		if(serverFound){
			try {
				//Connect to server to send status update and join
				Transceiver client = new SaslSocketTransceiver(server);
				ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
				String newuserName = proxy.enter("temperature sensor", InetAddress.getLocalHost().toString() + "," + portnumber).toString();
				if(newuserName != ""){
					userName = newuserName;
					heartbeat.setuserName(newuserName);
				}
				proxy.updateTemperature(userName, record);
				client.close();
			} catch (Exception e){
				e.printStackTrace();	
			}
		}
	}
}
