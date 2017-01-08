package classes;

import java.util.Scanner;

import classes.models.UserImpl;


public class UserExe {

    public static void main(String[] args) {
        //Create an object of the userimpl class
        UserImpl user = new UserImpl();
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
            System.out.println("h: enterHouse");
            System.out.println("i: leaveHouse");
            System.out.println("j: exit");
            selectedType = keyboard.nextLine();
            System.out.println(selectedType.equalsIgnoreCase("j"));
            if (user.isServer) {
                selectedType = "";
                System.out.println("This user is currently a server, please wait until the server comes back online");
            }
            switch (selectedType) {
                case "a":
                    user.requestLights();
                    break;
                case "b":
                    user.switchLight();
                    break;
                case "c":
                    user.getFridgeContent();
                    break;
                case "d":
                    user.openFridge();
                    try {
                        do {
                           Thread.sleep(1000);
                        } while (user.inFridgeQueue || user.connectedToFridge);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case "e":
                    user.getTemperature();
                    break;
                case "f":
                    user.getTemperatureHistory();
                    break;
                case "g":
                    user.requestClients();
                    break;
                case "h":
                    user.enterHouse();
                    break;
                case "i":
                    user.leaveHouse();
                    break;
                case "x":
                    user.printAggregate();
                    break;
                default:
                    break;
            }
        } while (!selectedType.equalsIgnoreCase("j"));
        keyboard.close();
    }
}
