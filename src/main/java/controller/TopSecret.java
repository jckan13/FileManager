package controller;

import model.DatabaseManager;
import view.UserInterface;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Commmand Line Utility
 */
public class TopSecret {
    public static void main(String[] args) {
        try {
            DatabaseManager databaseManager = new DatabaseManager();
            databaseManager.initializeDatabase();
            ProgramControl.setRepositories(databaseManager);

            if (args.length == 1 && args[0].equals("--change-password")) {
                UserInterface.changePassword();
            } else if (args.length == 1 && args[0].equals("--web")) {
                WebServer.start();
            } else if (args.length == 0) {
                UserInterface.start();
            } else {
                System.out.println("Invalid command.");
                System.out.println("Usage:");
                System.out.println("./gradlew run");
                System.out.println("./gradlew run --args=\"--change-password\"");
                System.out.println("./gradlew run --args=\"--web\"");
            }

        } catch (SQLException e) {
            System.out.println("Error: Could not connect to mission database.");
            System.out.println("Details: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error: Could not import mission brief data.");
        }

    }//end of main

}//end of TopSecret
