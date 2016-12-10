package classes;

import java.util.Scanner;

import classes.models.FridgeImpl;
import classes.models.LightImpl;

public class FridgeExe{
	public static void main(String[] args) {
		Scanner keyboard = new Scanner(System.in);
		int choice;
		FridgeImpl fridge = new FridgeImpl();
		
		do {
			System.out.println("================================================================================");
			System.out.println("Following are the commands you can use:");
			System.out.println("1) join");
			System.out.println("2) leave");
			System.out.println("3) show Name");
			System.out.println("0) exit");
			System.out.println("Type either join to join the server, or leave to leave the server, or exit to quit and shutdown.");

			choice= Integer.parseInt(keyboard.nextLine());
			
			switch(choice){
			case 1: 
				System.out.println("You chose: join");
				fridge.join();
				break;
			case 2: 
				System.out.println("You chose: leave");
				fridge.leave();
				break;
			case 3: 
				System.out.println("You chose: name");
				fridge.showName();
				break;	
			}
		} while (choice != 0);
	}
}
