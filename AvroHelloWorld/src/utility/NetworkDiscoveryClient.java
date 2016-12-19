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
	Object caller;
	String type;
	
	public NetworkDiscoveryClient(Object x, String y){
		caller = x;
		type = y;
	}
	
	public InetSocketAddress findServer() throws SocketTimeoutException, IOException{
		try{
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
          if (message.equals("DISCOVER_SERVER_RESPONSE")) {
            TempSensImpl sensor = (TempSensImpl)(caller);
            InetSocketAddress address = new InetSocketAddress(receivePacket.getAddress(), 6789);
            c.close();
            return address;
          }
          //Close the port!
          c.close();
		}catch(SocketException e){
			System.out.println();
		}
		
        return null;
	}

}
