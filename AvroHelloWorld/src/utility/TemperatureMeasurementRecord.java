package utility;

import java.time.LocalTime;

import sourcefiles.TemperatureAggregate;
import sourcefiles.TemperatureRecord;

public class TemperatureMeasurementRecord extends TemperatureAggregate{
	//Class that contains the average temperature at a certain timepoint
	
	public TemperatureMeasurementRecord(){
		counter = 0;
		record = new TemperatureRecord(LocalTime.now().toString(), 0.0);
		
	}
	
	//Constructor that extracts values from a temperatureaggregate
	public TemperatureMeasurementRecord(TemperatureAggregate extract){
		counter = extract.getCounter();
		record = extract.getRecord();
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
	
	public TemperatureAggregate getAggregate(){
		return new TemperatureAggregate(this.getCounter(), this.getRecord());
	}
}
