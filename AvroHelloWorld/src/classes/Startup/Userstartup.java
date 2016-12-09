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
			System.out.println("Enter Exit to end");
			selectedType = keyboard.nextLine();
		} while(!selectedType.equalsIgnoreCase("exit"));
		keyboard.close();
	}

}
