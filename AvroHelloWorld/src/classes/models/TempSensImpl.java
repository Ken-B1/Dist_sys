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
	ServerProtocol proxy;
	private InetSocketAddress serverAddress;
	private boolean serverFound;
	private Vector<TemperatureRecord> temperatures;
	private Heartbeat heartbeat;
	private Thread heartbeatThread;
	private List<String> previousNeighbour;
	private List<String> nextNeighbour;
	private boolean inElection;

	//Exceptionhandler for heartbeat thread 
	Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
	    public void uncaughtException(Thread th, Throwable ex) {
	    	//Catches the exceptions thrown by the heartbeat thread(indicating server wasnt found)
	        System.out.println("Couldnt find server during heartbeat");
	        serverAddress = new InetSocketAddress("0.0.0.0", 0);
	        serverFound = false;
	    }
	};
	public TempSensImpl(double temperature) throws InterruptedException{
		previousNeighbour = new ArrayList<String>();
		nextNeighbour = new ArrayList<String>();
		inElection=false;
		serverFound = false;
		temperatures = new Vector<TemperatureRecord>();
		heartbeat=new Heartbeat();
		//Try to connect to server 
		searchServer();
		
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
			serverAddress = FindServer.findServer();
			serverFound = true;
			heartbeat.setServer(serverAddress);
			heartbeatThread = new Thread(heartbeat);
			heartbeatThread.setUncaughtExceptionHandler(h);
			heartbeatThread.start();
			Transceiver client = new SaslSocketTransceiver(serverAddress);
			ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
			CharSequence newUserName = proxy.enter("temperature sensor", ip + "," + portnumber);
			id = newUserName.toString();
			heartbeat.setuserName(newUserName.toString());
			client.close();
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
				Transceiver client = new SaslSocketTransceiver(serverAddress);
				ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
				proxy.updateTemperature(id, record);
				client.close();
			} catch (Exception e){
				e.printStackTrace();	
			}
		}
	}

    @Override
    public CharSequence setNewServer(CharSequence serverIp) throws AvroRemoteException {
        String[] serverIpSplit = serverIp.toString().split(",");
        try {
            if (client.isConnected()) {
                client.close();
            }
            serverAddress = new InetSocketAddress(serverIpSplit[0], Integer.parseInt(serverIpSplit[1]));
            client = new SaslSocketTransceiver(serverAddress);
            proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
