package utility;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import static java.time.temporal.ChronoUnit.SECONDS;

import classes.ServerExe;

//The maintainer will hold all the heartbeats of the devices and notify the server if a device hasnt answered in a while
public class ServerHeartbeatMaintainer implements Runnable{
	//Server linked to this maintainer
	ServerExe server;
	private Map<String, String> heartbeats = new HashMap<String, String>();  //Maps a client to a time since last heartbeat was received
	
	public ServerHeartbeatMaintainer(ServerExe server){
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
			for (Entry<String, String> entry : heartbeats.entrySet())
			{
				LocalDateTime lastTime = LocalDateTime.parse(entry.getValue());
				long delay = SECONDS.between(lastTime, now);
				if(delay > 30){
					//Havent heard from the device in over 30 seconds, so notify server
					server.removeClient(entry.getKey());
					this.leaveClient(entry.getKey());
				}
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
