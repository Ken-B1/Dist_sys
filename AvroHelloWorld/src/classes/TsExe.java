package classes;

import java.util.Scanner;

import classes.models.TempSensImpl;

public class TsExe {
	public static void main(String[] args) {
		Scanner reader = new Scanner(System.in);  // Reading from System.in
		System.out.println("Enter a number: ");
		int n = reader.nextInt();
		TempSensImpl ts = new TempSensImpl(n);
		Scanner keyboard = new Scanner(System.in);
		String selectedType;
		do {
			System.out.println("Enter Exit to end");
			selectedType = keyboard.nextLine();
		} while(!selectedType.equalsIgnoreCase("exit"));
		reader.close();
		keyboard.close();
	}
}
