package classes.models;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.SaslSocketServer;
import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.avro.ipc.specific.SpecificResponder;

import classes.ServerExe;
import sourcefiles.LightProtocol;
import sourcefiles.ServerProtocol;

public class LightImpl implements LightProtocol{
    boolean status = false; 
    Transceiver client;
    ServerProtocol proxy;
    String name;
    String ip;
    Server server = null;
    int port;
    
    public LightImpl(int port){
    	try {
    	ip = InetAddress.getLocalHost().getHostAddress();
    	client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName("192.168.0.107"),6789));
    	this.port=port;
		proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
		server = new SaslSocketServer(new SpecificResponder(LightProtocol.class,this),new InetSocketAddress(InetAddress.getLocalHost(),port));
		server.start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	@Override
	public boolean changeState() throws AvroRemoteException {
		if(status){
			status=false;
		} else {
			status=true;
		}
		return status;
	}

	@Override
	public boolean getState() throws AvroRemoteException {
		return status;
	}
	
	public void join(){
		try {
			name = proxy.enter("light", ip+","+port).toString();
		} catch (AvroRemoteException e) {
			e.printStackTrace();
		}
	}
	
	public void leave(){
		try {
			proxy.leave(name);
		} catch (AvroRemoteException e) {
			e.printStackTrace();
		}
	}
	
	public void showName(){
		System.out.println(name);
	}
}
