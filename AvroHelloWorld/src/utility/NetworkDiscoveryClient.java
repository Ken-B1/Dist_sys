package utility;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import classes.models.TempSensImpl;

public class NetworkDiscoveryClient{
	InetAddress serveraddress;
	int portnumber;
	
	public NetworkDiscoveryClient(){
	}
	
	public InetSocketAddress findServer() throws SocketTimeoutException, IOException{
	  DatagramSocket c = new DatagramSocket();
	  c.setSoTimeout(1000);
      c.setBroadcast(true);

      byte[] sendData = "DISCOVER_SERVER_REQUEST".getBytes();

      //Try the 255.255.255.255 first
      try {
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 8888);
        c.send(sendPacket);
        System.out.println(getClass().getName() + ">>> Request packet sent to: 255.255.255.255 (DEFAULT)");
      } catch (Exception e) {
      }
      
      //Wait for a response
      byte[] recvBuf = new byte[15000];
      DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
      c.receive(receivePacket);
      //We have a response
      System.out.println(getClass().getName() + ">>> Broadcast response from server: " + receivePacket.getAddress().getHostAddress());

      //Check if the message is correct
      String message = new String(receivePacket.getData()).trim();
      String[] message2 = message.split(",");
      if (message2[0].equals("DISCOVER_SERVER_RESPONSE")) {
        InetSocketAddress address = new InetSocketAddress(message2[1], Integer.parseInt(message2[2]));
        c.close();
        return address;
      }
      //Close the port!
      c.close();
	return null;
		
	}

}
