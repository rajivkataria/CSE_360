package simpleDatabase;

import java.sql.SQLException;
import java.util.Scanner;

/*
 *  Main class to start the CSE360 application.
 * Handles user and admin flows with database setup and login options.
 */
public class StartCSE360 {

    // Database helper instance for db operations
    private static DatabaseHelper databaseHelper;
    
    // Scanner for reading user input
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        databaseHelper = new DatabaseHelper(); // Initialize DatabaseHelper
        
        try { 
        	// Connect to the database
            databaseHelper.connectToDatabase();

            // Check if the database is empty (no users registered)
            if (databaseHelper.isDatabaseEmpty()) {
                System.out.println("In-Memory Database is empty");
                setupAdministrator(); // Set up admin access if db is empty
            } else {
                System.out.println("If you are an administrator, select A\nIf you are a user, select U\nEnter your choice: ");
                String role = scanner.nextLine(); // get the user role

                // Execute the flow based on the role selection
                switch (role) {
                    case "U":
                        userFlow(); // start user flow
                        break;
                    case "A":
                        adminFlow(); // start admin flow
                        break;
                    default:
                        System.out.println("Invalid choice. Please select 'A' or 'U'");
                        databaseHelper.closeConnection(); // Close connection in case of invalid choice
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("Good Bye!!");
            databaseHelper.closeConnection(); // Ensures that the connection is closed on exiting
        }
    }

    // Sets up the initial administrator account when DB is empty
    private static void setupAdministrator() throws Exception {
        System.out.println("Setting up Administrator access.");
        System.out.print("Enter Admin Email: ");
        String email = scanner.nextLine();
        System.out.print("Enter Admin Password: ");
        String password = scanner.nextLine();
        
        databaseHelper.register(email, password, "admin"); // Register admin in the DB
        System.out.println("Administrator setup completed.");
    }

    // User flow to handle registration or login for users
    private static void userFlow() throws Exception {
        System.out.println("User Flow");
        System.out.print("What would you like to do? 1.Register 2.Login: ");
        String choice = scanner.nextLine(); // Get user's choice for registering or logging in

        switch(choice) {
            case "1": 
                System.out.print("Enter User Email: ");
                String email = scanner.nextLine();
                System.out.print("Enter User Password: ");
                String password = scanner.nextLine();
                
                // Check if user already exists in the DB
                if (!databaseHelper.doesUserExist(email)) {
                    databaseHelper.register(email, password, "user"); // Register a new user in DB
                    System.out.println("User setup completed.");
                } else {
                    System.out.println("User already exists.");
                }
                break;
            case "2":
                System.out.print("Enter User Email: ");
                email = scanner.nextLine();
                System.out.print("Enter User Password: ");
                password = scanner.nextLine();
                
                // Authenticate user login
                if (databaseHelper.login(email, password, "user")) {
                    System.out.println("User login successful.");
                } else {
                    System.out.println("Invalid user credentials. Try again!!");
                }
                break;
            default:
                System.out.println("Invalid choice. Please select '1' or '2'.");
        }
    }

    // Admin flow to handle admin login and display of user information
    private static void adminFlow() throws Exception {
        System.out.println("Admin Flow");
        System.out.print("Enter Admin Email: ");
        String email = scanner.nextLine();
        System.out.print("Enter Admin Password: ");
        String password = scanner.nextLine();
        
        // Authenticate login for admins
        if (databaseHelper.login(email, password, "admin")) {
            System.out.println("Admin login successful.");
            databaseHelper.displayUsersByAdmin(); // Display all users
        } else {
            System.out.println("Invalid admin credentials. Try again!!");
        }
    }
}
