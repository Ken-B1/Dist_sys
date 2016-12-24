package utility;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.avro.AvroRemoteException;

import static java.time.temporal.ChronoUnit.SECONDS;

import classes.ServerExe;
import classes.models.ServerImpl;

//The maintainer will hold all the heartbeats of the devices and notify the server if a device hasnt answered in a while
public class ServerHeartbeatMaintainer implements Runnable{
	//Server linked to this maintainer
	ServerImpl server;
	private Map<String, String> heartbeats = new HashMap<String, String>();  //Maps a client to a time since last heartbeat was received
	
	public ServerHeartbeatMaintainer(ServerImpl server){
		this.server = server;
	}
	
	public void updateClient(String userName){
		String x = LocalDateTime.now().toString();
		heartbeats.put(userName, x);
	}
	
	public void leaveClient(String userName){
		heartbeats.remove(userName);
	}

	@Override
	public void run() {
		// This object will periodically check all heartbeats to see if devices havent notified in a while
		while(true){
			System.out.println("Checking for aliveness");
			LocalDateTime now = LocalDateTime.now();
			Vector<String> removeValues = new Vector<String>();
			for (Entry<String, String> entry : heartbeats.entrySet())
			{
				LocalDateTime lastTime = LocalDateTime.parse(entry.getValue());
				long delay = SECONDS.between(lastTime, now);
				if(delay > 30){
					//Havent heard from the device in over 30 seconds, so notify server
					try {
						server.leave(entry.getKey());
					} catch (AvroRemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					removeValues.add(entry.getKey());
				}
			}
			
			for (String x : removeValues){
				this.leaveClient(x);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
