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
	Transceiver client;
	int currentTemp;
	
	
	public static void main(String[] args){
		new TempSensImpl();
	}
	
	public TempSensImpl(){
		//Try to connect to server 
		try {	
			client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getLocalHost(),6789));
			ServerProtocol proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
			String userName = proxy.enter("light",InetAddress.getLocalHost().getHostAddress()).toString();
			System.out.println(userName);
		} catch(AvroRemoteException e){
			System.err.println("Error joining");
			e.printStackTrace(System.err);
			System.exit(1);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		currentTemp=20;
		System.out.println("TempSens created!");
	}
}
