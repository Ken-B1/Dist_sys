package classes.models;

import org.apache.avro.AvroRemoteException;

import sourcefiles.LightProtocol;

public class LightImpl implements LightProtocol{
    boolean status = false; 
    
    public LightImpl(){
		System.out.println("Light created!");
	}

	@Override
	public boolean changeState() throws AvroRemoteException {
		if(status){
			status=false;
		} else {
			status=true;
		}
		return status;
	}

	@Override
	public boolean getState() throws AvroRemoteException {
		return status;
	}
}
