package classes.models;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.AvroRemoteException;

import sourcefiles.FridgeProtocol;

public class FridgeImpl implements FridgeProtocol {
	List<CharSequence> inventory;
	
	public FridgeImpl(){
		inventory = new ArrayList<CharSequence>();
		System.out.println("Fridge created!");
	}

	@Override
	public List<CharSequence> getInventory() throws AvroRemoteException {
		return inventory;
	}

	@Override
	public CharSequence addItem(CharSequence item) throws AvroRemoteException {
		inventory.add(item);
		return item+" has been added";
	}

	@Override
	public CharSequence removeItem(CharSequence item) throws AvroRemoteException {
		if(inventory.contains(item)){
			inventory.remove(item);
			return item + " has been removed";
		}
		else {
			return item + " was not in the fridge";
		}
	}
}
