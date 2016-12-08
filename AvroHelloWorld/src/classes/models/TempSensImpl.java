package classes.models;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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

public class TempSensImpl implements TSProtocol {
	Random random = new Random();
	double currentTemp;
	String userName;
	
	public TempSensImpl(double temperature){
		//Try to connect to server 
		try {	
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getLocalHost(),6789));
			ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
			userName = proxy.enter("temperature sensor",InetAddress.getLocalHost().getHostAddress()).toString();
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
			} catch (Exception e){
				e.printStackTrace();				
			}
		}
		
	}
}
