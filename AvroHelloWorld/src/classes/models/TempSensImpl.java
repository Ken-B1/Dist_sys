package classes.models;

import java.util.Random;

import sourcefiles.TSProtocol;

public class TempSensImpl implements TSProtocol {
	Random random = new Random();
	int currentTemp;
	
	public TempSensImpl(int startTemp){
		currentTemp=startTemp;
		System.out.println("TempSens created!");
	}
}
