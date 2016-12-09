package classes.Startup;

import java.util.Scanner;

import classes.models.UserImpl;


public class Userstartup {

	public static void main(String[] args) {
		//Create an object of the userimpl class
		UserImpl ts = new UserImpl();
		Scanner keyboard = new Scanner(System.in);
		String selectedType;
		do {
			System.out.println("Usage:");
			System.out.println("a: requestlights");
			System.out.println("b: switchlight");
			System.out.println("c: getfridgecontent");
			System.out.println("d: openfridge");
			System.out.println("e: gettemperature");
			System.out.println("f: gettemperaturehistory");
			System.out.println("g: requestClients");
			System.out.println("h: exit");
			selectedType = keyboard.nextLine();
			
			switch(selectedType){
			case "a":
				ts.requestLights();
				break;
			case "b":
				ts.switchLight("xx");
				break;
			case "c":
				ts.getFridgeContent("xx");
				break;
			case "d":
				ts.openFridge("x");
				break;
			case "e":
				ts.getTemperature();
				break;
			case "f":
				ts.getTemperatureHistory();
				break;
			case "g":
				ts.requestClients();
				break;
			}
		} while(!selectedType.equalsIgnoreCase("h") && !selectedType.equalsIgnoreCase("exit"));
		keyboard.close();
	}
}
