package utility;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;

public class LANIp {
	public static InetAddress getAddress() throws UnknownHostException{
		try {
			Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
			for(NetworkInterface netint: Collections.list(nets)){
				Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
				for (InetAddress inetAddress : Collections.list(inetAddresses)){
					if(!inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress() ){
						return inetAddress;
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return InetAddress.getByName("0.0.0.0");
	}
}
