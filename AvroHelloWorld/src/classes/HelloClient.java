package classes;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import avro.hello.proto.Hello;

public class HelloClient {
	public static void main(String[] args){
		try {
			//InetAddress moet worde aangepast naar de naam van wa ge wilt naar connecte
			//Mss later aanpasse zoda ge via een broadcast ofzo het ip van de server kunt krijge
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(InetAddress.getByName("kenpc"),6789));
			Hello proxy = (Hello) SpecificRequestor.getClient(Hello.class, client);
			CharSequence response = proxy.sayHello("Jordan");
			System.out.println(response);
			client.close();
		} catch(IOException e){
			System.err.println("Error connecting to server");
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
