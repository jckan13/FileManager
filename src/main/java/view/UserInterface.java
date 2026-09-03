package view;

import model.CredentialManager;
import model.LoginService;

import java.io.IOException;
import java.util.Scanner;
import controller.ProgramControl;


public class UserInterface {
    //prompt the user for their username and password
    //lists the choices the inputter can select, doesn't exit till choice is picked
    //reads input from the terminal with a scanner
    public static void start(){
        LoginService loginService = new CredentialManager();
        Scanner scanner = new Scanner(System.in);
        start(loginService, scanner);
    }//end of start
    //testable start method that takes in two test params
    public static void start(LoginService loginService, Scanner scanner) {
        try {
            if (!loginService.credentialsExist()) {
                createNewCredentials(loginService, scanner);
                return;
            }

            loginUser(loginService, scanner);

        } catch (IOException e) {
            System.out.println("Error with credential file.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }//end of testable start method

    private static void createNewCredentials(LoginService loginService, Scanner scanner) throws IOException {
        System.out.println("No credentials found. Create a new one.");
        //prompt to set a username and password
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        loginService.createCredentials(username, password);
        System.out.println("Credentials created. Go back to homepage to sign in.");
    }

    private static void loginUser(LoginService loginService, Scanner scanner) throws IOException {
        System.out.print("Username: ");
        String username = scanner.nextLine();

        if (!loginService.usernameExists(username)) {
            System.out.println("Invalid credentials.");
            return;
        }

        if (loginService.hasNoPassword(username)) {
            createPasswordForNewUser(loginService, scanner, username);
            return;
        }

        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (loginService.login(username, password)) {
            System.out.println("Logged in successfully.");
            ProgramControl.setCurrentUsername(username);
            runMainMenu(scanner);
        } else {
            System.out.println("Invalid credentials.");
        }
    }

    private static void createPasswordForNewUser(LoginService loginService, Scanner scanner, String username) throws IOException {
        System.out.println("This user does not have a password yet.");
        System.out.println("Create a password to activate the account.");

        System.out.print("New password: ");
        String newPassword = scanner.nextLine();

        System.out.print("Confirm new password: ");
        String confirmPassword = scanner.nextLine();

        if (!newPassword.equals(confirmPassword)) {
            System.out.println("Error: New passwords do not match.");
            return;
        }

        if (!loginService.isValidPassword(newPassword)) {
            System.out.println("Error: Password must be at least 5 characters long and contain uppercase and lowercase letters.");
            return;
        }

        loginService.createCredentials(username, newPassword);

        System.out.println("Password created successfully. Please restart the program and log in.");
    }

    // -------------------------
    // Main Menu
    // -------------------------

    static void runMainMenu(Scanner scanner) {
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("Top Secret System");
            System.out.println("1. Briefs");
            System.out.println("2. Agents");
            System.out.println("3. Facilities");
            System.out.println("4. Missions");
            System.out.println("5. Review Audit Log");
            System.out.println("6. Add User");
            System.out.println("7. Exit");
            System.out.print("Select an option: ");

            String option = scanner.nextLine();

            if (option.equals("1")) {
                runBriefsMenu(scanner);
            } else if (option.equals("2")) {
                runAgentsMenu(scanner);
            } else if (option.equals("3")) {
                runFacilitiesMenu(scanner);
            } else if(option.equals("4")) {
                runMissionsMenu(scanner);
            }else if (option.equals("5")) {
                showAuditLog();
            } else if (option.equals("6")) {
                addUserPrompt(scanner);
            } else if (option.equals("7")) {
                System.out.println("Exiting Top Secret System.");
                running = false;
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    // -------------------------
    // Briefs Menu
    // -------------------------

    private static void runBriefsMenu(Scanner scanner) {
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("Briefs Menu");
            System.out.println("1. List briefs");
            System.out.println("2. Read a brief");
            System.out.println("3. Add a brief");
            System.out.println("4. Back");
            System.out.print("Select an option: ");

            String option = scanner.nextLine();

            if (option.equals("1")) {
                System.out.println();
                System.out.println("Available Briefs:");
                System.out.println(ProgramControl.listBriefs());

                System.out.println();
                System.out.print("Enter a brief number to read, or 0 to return: ");
                String number = scanner.nextLine();

                if (!number.equals("0")) {
                    System.out.println();
                    System.out.println(ProgramControl.displayBrief(number));
                }

            } else if (option.equals("2")) {
                System.out.print("Enter brief number: ");
                String number = scanner.nextLine();
                System.out.println();
                System.out.println(ProgramControl.displayBrief(number));

            } else if (option.equals("3")) {
                addBriefPrompt(scanner);

            } else if (option.equals("4")) {
                running = false;

            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    private static void addBriefPrompt(Scanner scanner) {
        System.out.print("Title: ");
        String title = scanner.nextLine();

        System.out.print("Date (YYYY-MM-DD): ");
        String date = scanner.nextLine();

        System.out.print("Text: ");
        String text = scanner.nextLine();

        System.out.print("Mission ID: ");
        String missionId = scanner.nextLine();

        System.out.println(ProgramControl.addBrief(title, date, text, missionId));
    }

    // -------------------------
    // Agents Menu
    // -------------------------

    private static void runAgentsMenu(Scanner scanner) {
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("Agents Menu");
            System.out.println("1. List agents");
            System.out.println("2. Read an agent");
            System.out.println("3. Add an agent");
            System.out.println("4. Back");
            System.out.print("Select an option: ");

            String option = scanner.nextLine();

            if (option.equals("1")) {
                System.out.println();
                System.out.println("Available Agents:");
                System.out.println(ProgramControl.listAgents());

                System.out.println();
                System.out.print("Enter an agent number to read, or 0 to return: ");
                String number = scanner.nextLine();

                if (!number.equals("0")) {
                    System.out.println();
                    System.out.println(ProgramControl.displayAgent(number));
                }

            } else if (option.equals("2")) {
                System.out.print("Enter agent number: ");
                String number = scanner.nextLine();
                System.out.println();
                System.out.println(ProgramControl.displayAgent(number));

            } else if (option.equals("3")) {
                addAgentPrompt(scanner);

            } else if (option.equals("4")) {
                running = false;

            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    private static void addAgentPrompt(Scanner scanner) {
        System.out.print("Name: ");
        String name = scanner.nextLine();

        System.out.print("Date of birth (YYYY-MM-DD): ");
        String dateOfBirth = scanner.nextLine();

        System.out.print("Date of death, or leave blank: ");
        String dateOfDeath = scanner.nextLine();

        System.out.print("Notes: ");
        String notes = scanner.nextLine();

        System.out.print("Facility ID: ");
        String facilityId = scanner.nextLine();

        System.out.println(ProgramControl.addAgent(name, dateOfBirth, dateOfDeath, notes, facilityId));
    }


    // -------------------------
    // Facilities Menu
    // -------------------------

    private static void runFacilitiesMenu(Scanner scanner) {
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("Facilities Menu");
            System.out.println("1. List facilities");
            System.out.println("2. Read a facility");
            System.out.println("3. Add a facility");
            System.out.println("4. Back");
            System.out.print("Select an option: ");

            String option = scanner.nextLine();

            if (option.equals("1")) {
                System.out.println();
                System.out.println("Available Facilities:");
                System.out.println(ProgramControl.listFacilities());

                System.out.println();
                System.out.print("Enter a facility number to read, or 0 to return: ");
                String number = scanner.nextLine();

                if (!number.equals("0")) {
                    System.out.println();
                    System.out.println(ProgramControl.displayFacility(number));
                }

            } else if (option.equals("2")) {
                System.out.print("Enter facility number: ");
                String number = scanner.nextLine();
                System.out.println();
                System.out.println(ProgramControl.displayFacility(number));

            } else if (option.equals("3")) {
                addFacilityPrompt(scanner);

            } else if (option.equals("4")) {
                running = false;

            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    private static void addFacilityPrompt(Scanner scanner) {
        System.out.print("Facility name: ");
        String name = scanner.nextLine();

        System.out.print("Abbreviation: ");
        String abbreviation = scanner.nextLine();

        System.out.println(ProgramControl.addFacility(name, abbreviation));
    }
    //--------------------------
    //Mission Menu
    //--------------------------
    private static void runMissionsMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("Missions Menu");
            System.out.println("1. List missions");
            System.out.println("2. Read a mission");
            System.out.println("3. Add a mission");
            System.out.println("4. Back");
            System.out.print("Select an option: ");

            String option = scanner.nextLine();

            if (option.equals("1")) {
                System.out.println();
                System.out.println("Available Missions:");
                System.out.println(ProgramControl.listMissions());

                System.out.println();
                System.out.print("Enter a mission number to read, or 0 to return: ");
                String number = scanner.nextLine();

                if (!number.equals("0")) {
                    System.out.println();
                    System.out.println(ProgramControl.displayMission(number));
                }

            } else if (option.equals("2")) {
                System.out.print("Enter mission number: ");
                String number = scanner.nextLine();
                System.out.println();
                System.out.println(ProgramControl.displayMission(number));

            } else if (option.equals("3")) {
                addMissionPrompt(scanner);

            } else if (option.equals("4")) {
                running = false;

            } else {
                System.out.println("Invalid option.");
            }
        }
    }
    private static void addMissionPrompt(Scanner scanner) {
        System.out.print("Mission title: ");
        String title = scanner.nextLine();
        System.out.print("Date (YYYY-MM-DD): ");
        String date = scanner.nextLine();
        System.out.print("Description: ");
        String description = scanner.nextLine();
        System.out.print("Facility ID: ");
        String facilityId = scanner.nextLine();

        System.out.println(ProgramControl.addMission(title, date, description, facilityId));
    }

    // -------------------------
    // Audit Log
    // -------------------------

    private static void showAuditLog() {
        System.out.println();
        System.out.println("Audit Log:");
        System.out.println(ProgramControl.listAuditLogs());
    }

    // -------------------------
    // Change Password
    // -------------------------

    //calls CredentialManager's changePassword
    public static void changePassword() {
        LoginService loginService = new CredentialManager();
        Scanner scanner = new Scanner(System.in);
        changePassword(loginService, scanner);
    }

    //testable changePassword method with two params
    static void changePassword(LoginService loginService, Scanner scanner) {
        try {
            if (!loginService.credentialsExist()) {
                System.out.println("No credentials found. Please create credentials first.");
                return;
            }
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Current password: ");
            String currentPassword = scanner.nextLine();
            System.out.print("New password: ");
            String newPassword = scanner.nextLine();
            System.out.print("Confirm new password: ");
            String confirmPassword = scanner.nextLine();
            loginService.changePassword(username, currentPassword, newPassword, confirmPassword);
            System.out.println("Password changed successfully.");
        } catch (IOException e) {
            System.out.println("Error with credential file.");
        } catch (IllegalArgumentException | SecurityException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }//end of testable changePassword method

    private static void addUserPrompt(Scanner scanner) {
        LoginService loginService = new CredentialManager();

        System.out.print("Enter new username: ");
        String username = scanner.nextLine();

        try {
            loginService.addUser(username);
            System.out.println("User added successfully. The new user must create a password when they first log in.");
        } catch (IOException e) {
            System.out.println("Error with credential file.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

}//end of UserInterface