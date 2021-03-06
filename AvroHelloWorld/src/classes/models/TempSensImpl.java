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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.SaslSocketServer;
import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.avro.ipc.specific.SpecificResponder;

import sourcefiles.*;
import utility.Heartbeat;
import utility.NetworkDiscoveryClient;

public class TempSensImpl implements TSProtocol {
	private String id;
	private String ip;
	private int portnumber;
	private Transceiver client;
	private InetSocketAddress serverAddress;
	private boolean serverFound;
	private Vector<TemperatureRecord> temperatures;
	private Heartbeat heartbeat;
	private Thread heartbeatThread;

	//Exceptionhandler for heartbeat thread 
	Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
	    public void uncaughtException(Thread th, Throwable ex) {
	    	//Catches the exceptions thrown by the heartbeat thread(indicating server wasnt found)
	        serverAddress = new InetSocketAddress("0.0.0.0", 0);
	        serverFound = false;
	        connectToServer();
	    }
	};
	public TempSensImpl(double temperature) throws InterruptedException{
		serverFound = false;
		temperatures = new Vector<TemperatureRecord>();
		heartbeat=new Heartbeat();
		//Try to connect to server 
		connectToServer();
		id = "";
		
		LocalTime currenttime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
		TemperatureRecord newtemp = new TemperatureRecord(currenttime.toString(), temperature);
		temperatures.addElement(newtemp);
		sendToServer(newtemp);
		updateTemperature();
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
				sendToServer(newrecord);
			}
			
			try {
				Thread.sleep(x);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
		}
	}
	
	private void sendToServer(TemperatureRecord record){
		//First check if the server has been found yet
		if(!serverFound){
			connectToServer();
		}
		
		//If server has been found, try to send new temperature
		if(serverFound){
			try {
				//Connect to server to send status update and join
				Transceiver client = new SaslSocketTransceiver(serverAddress);
				ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
				proxy.updateTemperature(id, record);
				client.close();
			} catch (Exception e){
				e.printStackTrace();	
			}
		}
	}

    private void connectToServer() {
    	while(!serverFound){
	        try {
	            NetworkDiscoveryClient findServer = new NetworkDiscoveryClient();
	            serverAddress = findServer.findServer();
	            serverFound = true;	        
	            if(id == ""){
					client = new SaslSocketTransceiver(serverAddress);
			        ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
			        id = proxy.enter("temperature sensor", ip + "," + portnumber).toString();
			        client.close();
	            }
	            heartbeat.setServer(serverAddress);
	            heartbeat.setuserName(id);
	            heartbeatThread = new Thread(heartbeat);
	            heartbeatThread.setUncaughtExceptionHandler(h);
	            heartbeatThread.start();
	        } catch (IOException e) {
	            //Server can't be found
	            serverFound = false;
	            heartbeat.setServer(new InetSocketAddress("0.0.0.0", 0));
	
	        }
    	}
    }
}
