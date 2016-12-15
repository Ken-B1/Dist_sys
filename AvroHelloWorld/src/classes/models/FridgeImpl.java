package classes.models;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.SaslSocketServer;
import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.avro.ipc.specific.SpecificResponder;

import sourcefiles.FridgeProtocol;
import sourcefiles.LightProtocol;
import sourcefiles.ServerProtocol;

public class FridgeImpl implements FridgeProtocol {
	List<CharSequence> inventory;
	Transceiver client;
    ServerProtocol proxy;
    String name;
    String ip;
    Server server = null;
    int port;
	
	public FridgeImpl(){
		inventory = new ArrayList<CharSequence>();
		inventory.add("Appel");
		try {
			ServerSocket s = new ServerSocket(0);
			port = s.getLocalPort();
			s.close();
    	    ip = InetAddress.getLocalHost().getHostAddress();
    	    client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getLocalHost(), 6789));
		    proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
		    server = new SaslSocketServer(new SpecificResponder(FridgeProtocol.class,this),new InetSocketAddress(InetAddress.getLocalHost(),port));
		    server.start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<CharSequence> getInventory() throws AvroRemoteException {		
		return inventory;
	}

	@Override
	public CharSequence addItem(CharSequence item) throws AvroRemoteException {
		inventory.add(item);
		return item+" has been added";
	}

	@Override
	public CharSequence removeItem(CharSequence item) throws AvroRemoteException {		
		boolean deletedItem =false;
		for(int i=0;i<inventory.size();i++){
			if(inventory.get(i).toString().equals(item.toString())){
				inventory.remove(i);
				if(inventory.size()==0){
					System.out.println(proxy.notifyUsersOfEmptyFridge(name));
				}
				deletedItem=true;
			}
		}
		
		if(deletedItem){
			return item + " has been removed";
		} else {
			return item + " was not in the fridge";
		}
	}
	
	public void join(){
		try {
			name = proxy.enter("fridge", ip+","+port).toString();
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
