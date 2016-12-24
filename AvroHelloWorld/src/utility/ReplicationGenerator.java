package utility;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import sourcefiles.ReplicationData;
import sourcefiles.TemperatureAggregate;

public class ReplicationGenerator {
	//Class that takes a replication record and creates a copy from it
	//This is used because apache avro does weird things with Strings and charsequences when serializing/deserializing during protocol calls
	public ReplicationGenerator(){
		
	}
	
	public static ReplicationData generateReplica(ReplicationData original){
		ReplicationData newData = new ReplicationData();
		Map<CharSequence, CharSequence> connectedUsers = new HashMap<CharSequence, CharSequence>();
		Map<CharSequence, CharSequence> connectedLights = new HashMap<CharSequence, CharSequence>();
		Map<CharSequence, CharSequence> connectedFridges = new HashMap<CharSequence, CharSequence>();
		Map<CharSequence, CharSequence> connectedTS = new HashMap<CharSequence, CharSequence>();
		Map<CharSequence, Boolean> userlocation = new HashMap<CharSequence, Boolean>();	//Maps a user to a location (1 = outside, 0 = inside)
		
		
		for (Entry<CharSequence, CharSequence> entry : original.getConnectedUsers().entrySet())
		{
			connectedUsers.put(entry.getKey().toString(), entry.getValue().toString());
		}

		for (Entry<CharSequence, CharSequence> entry : original.getConnectedLights().entrySet())
		{
			connectedLights.put(entry.getKey().toString(), entry.getValue().toString());
		}
		
		for (Entry<CharSequence, CharSequence> entry : original.getConnectedFridges().entrySet())
		{
			connectedFridges.put(entry.getKey().toString(), entry.getValue().toString());
		}
	
		for (Entry<CharSequence, CharSequence> entry : original.getConnectedTS().entrySet())
		{
			connectedTS.put(entry.getKey().toString(), entry.getValue().toString());
		}
		
		for(Entry<CharSequence, Boolean> entry : original.getUserlocation().entrySet()){
			userlocation.put(entry.getKey().toString(), entry.getValue());
		}
		
		newData.setConnectedUsers(connectedUsers);
		newData.setConnectedLights(connectedLights);
		newData.setConnectedFridges(connectedFridges);
		newData.setConnectedTS(connectedTS);
		newData.setTemperatures(original.getTemperatures());
		newData.setUserlocation(userlocation);
		return newData;
	}
}
