package utility;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class NetworkDiscoveryServer implements Runnable{

	public NetworkDiscoveryServer(){
	}
	
	
	@Override
	public void run() {
		try{
			DatagramSocket socket = new DatagramSocket(8888);
		    socket.setBroadcast(true);

		    while (true) {
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
		        byte[] sendData = "DISCOVER_SERVER_RESPONSE".getBytes();

		        //Send a response
		        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
		        socket.send(sendPacket);

		        System.out.println(">>>Sent packet to: " + sendPacket.getAddress().getHostAddress());
		      }
		    }
		}catch(Exception e){
			
		}
	}

}
