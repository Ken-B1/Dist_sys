package utility;

import java.time.LocalTime;

import sourcefiles.TemperatureRecord;

public class TemperatureMeasurementRecord {
	//Class that contains the average temperature at a certain timepoint
	public int counter;
	public TemperatureRecord record;
	
	public TemperatureMeasurementRecord(){
		counter = 0;
		record = new TemperatureRecord(LocalTime.now().toString(), 0.0);
		
	}
	
	
	public void addTemperature(double temperature, String time){
		//calculate the new average of this time
		if(counter == 0){
			//No temperatures added yet, so add first temperature
			record.temperature = temperature;
			record.time = time;
			counter = 1;
		}else{
		//Extra check to see if correct time has been added
			if(isTime(time)){
				record.temperature = ((record.temperature*counter) + temperature)/(counter+1);
				counter++;
			}
		}
	}
	
	public boolean isTime(String x){
		LocalTime time = LocalTime.parse(record.time);
		return time.equals(LocalTime.parse(x));
	}
}
