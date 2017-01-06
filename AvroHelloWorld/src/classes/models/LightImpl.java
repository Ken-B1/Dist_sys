package classes.models;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
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

import classes.ServerExe;
import sourcefiles.*;
import utility.Heartbeat;
import utility.NetworkDiscoveryClient;

public class LightImpl implements LightProtocol {
    boolean status = false;
    Transceiver client;
    ServerProtocol proxy;
    String id;
    String ip;
    Server server = null;
    int port;

    InetSocketAddress serverAddress;
    private boolean serverFound;
    private Heartbeat heartbeat;
    private Thread heartbeatThread;

    Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
        public void uncaughtException(Thread th, Throwable ex) {
            //Catches the exceptions thrown by the heartbeat thread(indicating server wasnt found)
            System.out.println("Couldnt find server during heartbeat");
            serverAddress = new InetSocketAddress("0.0.0.0", 0);
            serverFound = false;
            connectToServer();
        }
    };

    public LightImpl() {
        heartbeat = new Heartbeat();
        connectToServer();
        try {
            ServerSocket s = new ServerSocket(0);
            port = s.getLocalPort();
            s.close();
            ip = InetAddress.getLocalHost().getHostAddress();
            client = new SaslSocketTransceiver(serverAddress);
            proxy = (ServerProtocol) SpecificRequestor.getClient(ServerProtocol.class, client);
            server = new SaslSocketServer(new SpecificResponder(LightProtocol.class, this), new InetSocketAddress(ip, port));
            server.start();
            id = proxy.enter("light",ip+","+port).toString();
            heartbeat.setuserName(id);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean changeState() throws AvroRemoteException {
        if (status) {
            status = false;
        } else {
            status = true;
        }
        return status;
    }

    @Override
    public Void setState(boolean state) throws AvroRemoteException {
        status = state;
        return null;
    }

    @Override
    public boolean getState() throws AvroRemoteException {
        return status;
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

   /* public void join() {
        connectToServer();
        try {
            id = proxy.enter("light", ip + "," + port).toString();
            heartbeat.setuserName(id);
        } catch (AvroRemoteException e) {
            e.printStackTrace();
        }
    }

    public void leave() {
        try {
            proxy.leave(id);
            heartbeat.setuserName("");
        } catch (AvroRemoteException e) {
            e.printStackTrace();
        }
    }*/

    public void showName() {
        System.out.println(id);
    }

    private void connectToServer() {
    	while(!serverFound){
	        try {
	            NetworkDiscoveryClient FindServer = new NetworkDiscoveryClient();
	            serverAddress = FindServer.findServer();
	            System.out.println("server found on: " + serverAddress);
	            serverFound = true;
	            heartbeat.setServer(serverAddress);
	            heartbeatThread = new Thread(heartbeat);
	            heartbeatThread.setUncaughtExceptionHandler(h);
	            heartbeatThread.start();
	        } catch (IOException e) {
	            //Server can't be found
	            serverFound = false;
	        }
    	}
    }
}
