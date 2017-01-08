package utility;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class NetworkDiscoveryServer implements Runnable{

	private int portnumber;
	DatagramSocket socket;
	boolean isRunning;

	public NetworkDiscoveryServer(int portnumber){
		this.portnumber = portnumber;
		try {
			this.socket = new DatagramSocket(8888);
			this.socket.setBroadcast(true);
			this.isRunning = true;
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void run() {
		try{
		    while (isRunning) {
		      System.out.println(">>>Ready to receive broadcast packets!");

		      //Receive a packet
		      byte[] recvBuf = new byte[15000];
		      DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
		      socket.receive(packet);
		      
		      System.out.println(">>>Discovery packet received from: " + packet.getAddress().getHostAddress());
		      System.out.println(">>>Packet received; data: " + new String(packet.getData()));
		      
		      
		      //See if the packet holds the right command (message)
		      String message = new String(packet.getData()).trim();
		      if (message.equals("DISCOVER_SERVER_REQUEST")) {
		    	String response = "DISCOVER_SERVER_RESPONSE," + LANIp.getAddress().toString().split("/")[1].toString() + "," + this.portnumber;
		    	byte[] sendData = (response).getBytes();

		        //Send a response
		        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
		        socket.send(sendPacket);

		        System.out.println(">>>Sent packet to: " + sendPacket.getAddress().getHostAddress());
		      }
		    }
		}catch(Exception e){
			//Socket closed because server is shutting down
			System.out.println("Closing networkdiscoveryserver because server is closing");
		}
	}
	
	public void end(){
		this.isRunning = false;
		this.socket.close();
	}

}
