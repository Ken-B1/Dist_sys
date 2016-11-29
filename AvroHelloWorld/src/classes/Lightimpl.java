package classes;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.SaslSocketServer;
import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.avro.ipc.specific.SpecificResponder;

import sourcefiles.Light;
import sourcefiles.lightprotocol;
import avro.hello.proto.Hello;

public class Lightimpl implements lightprotocol{
    boolean status = false; 
	
    public static void main(String[] args){
		Server server = null;
    	try {
			System.out.println(InetAddress.getLocalHost());
			server = new SaslSocketServer(new SpecificResponder(lightprotocol.class,new Lightimpl()),new InetSocketAddress(InetAddress.getLocalHost(),6798));
		} catch (IOException e){
			System.err.println("[error]: Failed to start server");
			e.printStackTrace(System.err);
			System.exit(1);
		}
		server.start();
    	try {	
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(6789));
			Hello proxy = (Hello) SpecificRequestor.getClient(Hello.class, client);
			proxy.enter("ken", "1");
    	} catch(Exception e){
    		
    	}
    	try {
			server.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    
	@Override
	public boolean changestate() throws AvroRemoteException {
		// TODO Auto-generated method stub
		status = !status;
		return false;
	}

}
