package passwordEvaluationTestbed;

// JavaFX imports needed to support the Graphical User Interface

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

/*
 *  The following class manages the user interface for an account and role management application, 
 *  provides functionality to invite new users, modify user roles, register new users,
 *  and handles login/logout processes. 
 *  Different actions are available based on the user's role.
 */
public class MainApp extends Application {

    // Simulated databases
    private static Map<String, UserAccount> userDB = new HashMap<>();
    private static Map<String, Invite> inviteDB = new HashMap<>();
    private static Map<String, TempPass> tempPassDB = new HashMap<>();

    // Role constants
    private static final String ROLE_ADMINISTRATOR = "Administrator";
    private static final String ROLE_LEARNER = "Student";
    private static final String ROLE_TEACHER = "Teacher";

    // a variable to keep track of the currently logged-in Admin's username
    private String loggedInAdmin;

    // main method launches the JavaFX application
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage mainWindow) {
        // Remove the initial window title here
        // mainWindow.setTitle("Help System - Phase 1");
        // If no users already exist in the database, choose the new login scene
        if (userDB.isEmpty()) {
            mainWindow.setScene(initAdminRegistration(mainWindow));
        } 
        // If Users exist, show login screen
        else {
            mainWindow.setScene(new Scene(buildLoginScreen(mainWindow, mainWindow), 400, 350));
        }
        mainWindow.show();
    }
    // This method creates the first user registration page for setting up the first admin
    private Scene initAdminRegistration(Stage window) {
        GridPane regGrid = new GridPane();  // Layout for the registration form
        regGrid.setPadding(new Insets(10));
        regGrid.setVgap(8);     // Vertical gap between form elements
        regGrid.setHgap(10);    // Horizontal gap between form elements

        Label titleLbl = new Label("Admin Registration");
        titleLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Required labels and text fields for registration

        Label userLbl = new Label("Username:");
        TextField userTxt = new TextField();
        Label passLbl = new Label("Password:");
        PasswordField passTxt = new PasswordField();
        Label confirmPassLbl = new Label("Confirm Password:");
        PasswordField confirmPassTxt = new PasswordField();
        Button regBtn = new Button("Register Admin");       // Submit button for the registration form
        Label statusLbl = new Label();              
        
        // New form elements added
        regGrid.add(titleLbl, 0, 0, 2, 1);
        regGrid.add(userLbl, 0, 1);
        regGrid.add(userTxt, 1, 1);
        regGrid.add(passLbl, 0, 2);
        regGrid.add(passTxt, 1, 2);
        regGrid.add(confirmPassLbl, 0, 3);
        regGrid.add(confirmPassTxt, 1, 3);
        regGrid.add(regBtn, 1, 4);
        regGrid.add(statusLbl, 1, 5);

        Scene regScene = new Scene(regGrid, 400, 300);

        // When the button is pressed, this event is triggered which validates the given username and password from the user
        regBtn.setOnAction(e -> {
            String uname = userTxt.getText();       // Text from username input
            String pwd = passTxt.getText();         //Text from password input
            String confirmPwd = confirmPassTxt.getText();   //Text from confirm password input

            // If password is not valid, the event is brought to halt and no further changes are made to the layout
            if (!validatePassword(pwd)) {
                statusLbl.setText("Password must be 8+ chars, with uppercase, digit, special char.");
                return;
            }
            // Or, if passwords don't match, no further changes are made to the layout and the event is stopped
            if (!pwd.equals(confirmPwd)) {
                statusLbl.setText("Passwords do not match.");
                return;
            }
            // Check if there are any users already present in the database, and, if not, then add the first as an admin
            if (userDB.isEmpty()) {
                Set<String> roles = new HashSet<>();
                roles.add(ROLE_ADMINISTRATOR);
                UserAccount adminUser = new UserAccount(uname, pwd.toCharArray(), roles);   // created a user account with the given username, password, and the role of admin
                userDB.put(uname, adminUser);       //user is also added to the database for future needs

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Registration Successful");          //Successfull message is shown afterwards
                alert.setHeaderText(null);
                alert.setContentText("Admin account created. Please log in.");      //User is prompted again to login with the new credentials
                alert.showAndWait();

                window.setScene(new Scene(buildLoginScreen(window, window), 400, 350));
            } else {
                statusLbl.setText("An admin already exists. Please log in.");       // If database is not empty, then there must be an admin already present.
            }
        });

        // Set the window title to "Admin Registration"
        window.setTitle("Admin Registration");

        return regScene;
    }
    // This method builds the login screen with username and password field
    private GridPane buildLoginScreen(Stage window, Stage mainWindow) {
        GridPane loginGrid = new GridPane();    // Layout for login screen
        loginGrid.setPadding(new Insets(10));
        loginGrid.setVgap(8);   // Sets the vertical gap   
        loginGrid.setHgap(10);  // Sets the horizontal gap

        Label userLbl = new Label("Username:");
        TextField userTxt = new TextField();
        Label passLbl = new Label("Password:");
        PasswordField passTxt = new PasswordField();
        Button loginBtn = new Button("Login");      //Login button
        Hyperlink inviteLink = new Hyperlink("Use Invitation Code");       //In case the person wants to login through an invite code, this link will help generate a new invite code        
        Label statusLbl = new Label();

        loginGrid.add(userLbl, 0, 0);
        loginGrid.add(userTxt, 1, 0);
        loginGrid.add(passLbl, 0, 1);
        loginGrid.add(passTxt, 1, 1);
        loginGrid.add(loginBtn, 1, 2);
        loginGrid.add(inviteLink, 1, 3);
        loginGrid.add(statusLbl, 1, 4);

        // Set the window title to "Login"
        window.setTitle("Login");

        loginBtn.setOnAction(e -> {                 // This event is triggered when the login button is pressed
            String uname = userTxt.getText();       // Get the text from username field
            String pwd = passTxt.getText();         // Get the text from password field

            if (userDB.containsKey(uname)) {        // Check if there is any user with the same name in the database
                UserAccount user = userDB.get(uname);

                if (user.isTempPass()) {            // Check if the user is using a temporary password or an invite code
                    TempPass tp = tempPassDB.get(uname);
                    if (tp != null && tp.getPassword().equals(pwd)) {
                        if (LocalDateTime.now().isAfter(tp.getExpiry())) {      // Check if the temporary password is expired
                            statusLbl.setText("Temporary password expired.");
                            return;
                        }
                        window.setScene(new Scene(buildPassResetScreen(window, user), 400, 300));
                        return;
                    } else {
                        statusLbl.setText("Invalid temporary password.");   
                        return;
                    }
                }

                if (String.valueOf(user.getPassword()).equals(pwd)) {
                    // If user is missing any of the important credentials, he is prompted to complete his profile first
                    if (user.getFirstName() == null || user.getLastName() == null || user.getEmail() == null) {
                        window.setScene(new Scene(completeUserProfile(window, user), 500, 400));
                    }  
                    else {
                        // If the user has multiple roles, allow them to select a role
                        if (user.getRoles().size() > 1) {                   
                            window.setScene(new Scene(selectUserRole(window, user), 400, 200));
                        }
                        // Else, if they only have one role, a home screen for that specific role is displayed 
                        else {
                            String role = user.getRoles().iterator().next();
                            displayHomeScreen(window, user, role);
                        }
                    }
                } else {
                    statusLbl.setText("Incorrect password.");
                }
            } else {
                statusLbl.setText("User not found.");
            }
        });
        // If the user presses on the hyperlink, he will be directed to another screen where he would be prompted to enter the invite code and then set up his account
        inviteLink.setOnAction(e -> {
            window.setScene(new Scene(enterInviteCode(window), 400, 200));
        });

        return loginGrid;   // Return the login screen layout
    }
    // A method to display the home screen for the currently logged in user
    private void displayHomeScreen(Stage window, UserAccount user, String role) {
        VBox homeLayout = new VBox(10);
        homeLayout.setPadding(new Insets(10));
        
        // Welcome message to the user
        String displayName = user.getPreferredName() != null ? user.getPreferredName() : user.getFirstName();
        Label welcomeLbl = new Label("Welcome, " + displayName + " (" + role + ")!");       
        Button logoutBtn = new Button("Logout");

        homeLayout.getChildren().addAll(welcomeLbl, logoutBtn);
        // If the user is an admin, then admin privileges are provided in the home screen.
        if (ROLE_ADMINISTRATOR.equals(role)) {
            loggedInAdmin = user.getUsername();

            Button inviteBtn = new Button("Invite User");
            inviteBtn.setOnAction(e -> openInviteUserScreen(window));

            Button resetBtn = new Button("Reset User Account");
            resetBtn.setOnAction(e -> openResetUserScreen(window));

            Button deleteBtn = new Button("Delete User Account");
            deleteBtn.setOnAction(e -> openDeleteUserScreen(window));

            Button listBtn = new Button("List Users");
            listBtn.setOnAction(e -> openListUsersScreen(window));

            Button modifyBtn = new Button("Modify User Roles");
            modifyBtn.setOnAction(e -> openModifyRolesScreen(window));

            homeLayout.getChildren().addAll(inviteBtn, resetBtn, deleteBtn, listBtn, modifyBtn);

            // Set the window title to "Admin Home"
            window.setTitle("Admin Home");
        } else {
            // Set the window title to "User Home"
            window.setTitle("User Home");
        }
        // Logout button action to clear admin session and return to login screen
        logoutBtn.setOnAction(e -> {
            loggedInAdmin = null;
            window.setScene(new Scene(buildLoginScreen(window, window), 400, 350));
        });

        Scene homeScene = new Scene(homeLayout, 400, 300);
        window.setScene(homeScene);
    }
    // A method to open the screen to invite a new user
    private void openInviteUserScreen(Stage window) {
        GridPane inviteGrid = new GridPane();
        inviteGrid.setPadding(new Insets(10));
        inviteGrid.setVgap(8);
        inviteGrid.setHgap(10);

        // Label and text field for entering the email of the user to be invited
        Label emailLbl = new Label("Email:");
        TextField emailTxt = new TextField();
        // To specify the role of the user
        Label rolesLbl = new Label("Roles:");
        CheckBox adminChk = new CheckBox("Admin");
        CheckBox learnerChk = new CheckBox("Student");
        CheckBox teacherChk = new CheckBox("Teacher");
        VBox rolesBox = new VBox(5, adminChk, learnerChk, teacherChk);

        Label levelLbl = new Label("Topic Level:");
        // combo box for selecting the topic level from Beginner to Expert
        ComboBox<String> levelCombo = new ComboBox<>();
        levelCombo.getItems().addAll("Beginner", "Intermediate", "Advanced", "Expert");
        levelCombo.setValue("Intermediate");
        
        // Buttons for sending the invite and going back to the home screen
        Button sendInviteBtn = new Button("Send Invite");
        Button backBtn = new Button("Back");
        Label statusLbl = new Label();

        inviteGrid.add(emailLbl, 0, 0);
        inviteGrid.add(emailTxt, 1, 0);
        inviteGrid.add(rolesLbl, 0, 1);
        inviteGrid.add(rolesBox, 1, 1);
        inviteGrid.add(levelLbl, 0, 2);
        inviteGrid.add(levelCombo, 1, 2);
        inviteGrid.add(sendInviteBtn, 1, 3);
        inviteGrid.add(backBtn, 0, 3);
        inviteGrid.add(statusLbl, 1, 4);

        // Set the window title to "Invite User"
        window.setTitle("Invite User");

        /* 
        * The following event handler handles when the invite button is clicked.
        * It checks what roles are selected for the invited user, and gets the invite code and display it to the person inviting other user.
        */
        sendInviteBtn.setOnAction(e -> {
            String email = emailTxt.getText();
            String topicLevel = levelCombo.getValue();
            String code = generateInviteCode();

            Set<String> roles = new HashSet<>();
            // Check what roles are selected for the invited user
            if (adminChk.isSelected()) {
                roles.add(ROLE_ADMINISTRATOR);
            }
            if (learnerChk.isSelected()) {
                roles.add(ROLE_LEARNER);
            }
            if (teacherChk.isSelected()) {
                roles.add(ROLE_TEACHER);
            }
            // At least one role must be selected
            if (roles.isEmpty()) {
                statusLbl.setText("Select at least one role.");
                return;
            }
            // Invitation code is generated and an invite object in initialized with the code and user information
            Invite invite = new Invite(code, roles, email, topicLevel);
            inviteDB.put(code, invite);     // The code is kept in DB to verify later on if the invited user is using the correct invite code

            statusLbl.setText("Invitation sent to " + email + " with code: " + code);
        });
        // Back button to take the user back to the home screen
        backBtn.setOnAction(e -> displayHomeScreen(window, userDB.get(loggedInAdmin), ROLE_ADMINISTRATOR));
        
        Scene inviteScene = new Scene(inviteGrid, 500, 400);
        window.setScene(inviteScene);
    }

    // A method to create a 6-character alphanumeric invite code
    private String generateInviteCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        StringBuilder code = new StringBuilder(6); // code length is 6
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return code.toString();
    }
    // A method to build the reset screen
    private void openResetUserScreen(Stage window) {
        GridPane resetGrid = new GridPane();
        resetGrid.setPadding(new Insets(10));
        resetGrid.setVgap(8);
        resetGrid.setHgap(10);

        // Creating labels and text fields for user input
        Label userLbl = new Label("Username:");
        TextField userTxt = new TextField();
        Label dateLbl = new Label("Expiry Date (yyyy-MM-dd):");
        TextField dateTxt = new TextField();
        Label timeLbl = new Label("Expiry Time (24-hour HH:mm):");
        TextField timeTxt = new TextField();
        
        Button resetBtn = new Button("Reset Account");
        Button backBtn = new Button("Back");
        Label statusLbl = new Label();

        // Adding components to the grid
        resetGrid.add(userLbl, 0, 0);
        resetGrid.add(userTxt, 1, 0);
        resetGrid.add(dateLbl, 0, 1);
        resetGrid.add(dateTxt, 1, 1);
        resetGrid.add(timeLbl, 0, 2);
        resetGrid.add(timeTxt, 1, 2);
        resetGrid.add(resetBtn, 1, 3);
        resetGrid.add(backBtn, 0, 3);
        resetGrid.add(statusLbl, 1, 4);

        // Set the window title to "Reset User Account"
        window.setTitle("Reset User Account");

        // In the event that the reset button is clicked, the following event is triggered.
        resetBtn.setOnAction(e -> {
            // Extract the username, date, and time from the fields.
            String uname = userTxt.getText();
            String dateStr = dateTxt.getText();
            String timeStr = timeTxt.getText();
            
            // Input validation takes place
            if (!userDB.containsKey(uname)) {
                statusLbl.setText("User not found.");
                return;
            }
            // Ensure date and time fields are not empty
            if (dateStr.isEmpty() || timeStr.isEmpty()) {
                statusLbl.setText("Enter date and time.");
                return;
            }

            try {
                DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                LocalDateTime expiryDT = LocalDateTime.parse(dateStr + "T" + timeStr, dtFormatter);

                // create a temporary password and set it for the user
                String tempPass = generateTempPassword();
                TempPass tempPassword = new TempPass(tempPass, expiryDT);
                tempPassDB.put(uname, tempPassword);

                UserAccount user = userDB.get(uname);
                user.setTempPass(true);     // the temporary password flag is set to true

                statusLbl.setText("Temporary password set for " + uname + ": " + tempPass);

            } catch (Exception ex) {
                // Handle invalid date or time format
                statusLbl.setText("Invalid date/time format.");     
            }
        });
        // Back button to return to home screen in case the user changes his mind or no longer wants to reset his password
        backBtn.setOnAction(e -> displayHomeScreen(window, userDB.get(loggedInAdmin), ROLE_ADMINISTRATOR));

        Scene resetScene = new Scene(resetGrid, 500, 300);
        window.setScene(resetScene);
    }   
    // A method to create a random password of 6 digits.
    private String generateTempPassword() {
        Random rnd = new Random();
        int tempPass = 100000 + rnd.nextInt(900000);
        return String.valueOf(tempPass);
    }
    // Opens the screen to delete a user account
    private void openDeleteUserScreen(Stage window) {
        GridPane delGrid = new GridPane();
        delGrid.setPadding(new Insets(10));
        delGrid.setVgap(8);
        delGrid.setHgap(10);
        // Creating labels and text fields for user input
        Label userLbl = new Label("Username:");
        TextField userTxt = new TextField();
        Button delBtn = new Button("Delete User");
        Button backBtn = new Button("Back");        // A back button in case the user no longer wants to complete the task
        Label statusLbl = new Label();

        delGrid.add(userLbl, 0, 0);
        delGrid.add(userTxt, 1, 0);
        delGrid.add(delBtn, 1, 1);
        delGrid.add(backBtn, 0, 1);
        delGrid.add(statusLbl, 1, 2);

        // Set the window title to "Delete User Account"
        window.setTitle("Delete User Account");

        /* 
        * Clicking 'Delete User' Button will trigger the following event
        * After checking for inputs, it asks for confirmation through alert, and after that, removes the user
        */
        delBtn.setOnAction(e -> {
            String uname = userTxt.getText();
            // Checks if there is any user with the username given
            if (!userDB.containsKey(uname)) {
                statusLbl.setText("User not found.");
                return;
            }
            // An alert message to ask the user to confirm his choice
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete User");
            alert.setHeaderText("Delete user: " + uname + "?");
            alert.setContentText("This cannot be undone.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Remove user from the database
                userDB.remove(uname);
                statusLbl.setText("User " + uname + " deleted.");
            } else {
                statusLbl.setText("Deletion canceled.");
            }
        });
        // Back button to return to the home screen
        backBtn.setOnAction(e -> displayHomeScreen(window, userDB.get(loggedInAdmin), ROLE_ADMINISTRATOR));

        Scene delScene = new Scene(delGrid, 500, 300);
        window.setScene(delScene);
    }
    // Opens the screen to list all user accounts
    private void openListUsersScreen(Stage window) {
        VBox listLayout = new VBox(10);     // Since we want the users to appear vertically, VBOX is an appropriate option
        listLayout.setPadding(new Insets(10));      // padding of 10 around all directions

        Label titleLbl = new Label("User Accounts:");
        listLayout.getChildren().add(titleLbl);
        // Loop through each user in the db and display their information
        for (UserAccount user : userDB.values()) {
            String fullName = (user.getFirstName() != null ? user.getFirstName() : "") + " "
                    + (user.getMiddleName() != null ? user.getMiddleName() + " " : "")
                    + (user.getLastName() != null ? user.getLastName() : "");
            Label userLbl = new Label("Username: " + user.getUsername() +
                    ", Name: " + fullName.trim() +
                    ", Roles: " + user.getRoles());
            listLayout.getChildren().add(userLbl);
        }
        // Back button to return to the home screen
        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> displayHomeScreen(window, userDB.get(loggedInAdmin), ROLE_ADMINISTRATOR));
        listLayout.getChildren().add(backBtn);

        // Set the window title to "List Users"
        window.setTitle("List Users");

        Scene listScene = new Scene(listLayout, 500, 400);
        window.setScene(listScene);
    }
    // This method opens the Modify User Roles screen where an admin can update the roles of a specific user.
    private void openModifyRolesScreen(Stage window) {
        GridPane modGrid = new GridPane();
        modGrid.setPadding(new Insets(10));
        modGrid.setVgap(8);
        modGrid.setHgap(10);

        // Create labels and text fields for entering the username
        Label userLbl = new Label("Username:");
        TextField userTxt = new TextField();
        // Create checkboxes for selecting different user roles
        Label rolesLbl = new Label("Roles:");
        CheckBox adminChk = new CheckBox("Admin");
        CheckBox learnerChk = new CheckBox("Student");
        CheckBox teacherChk = new CheckBox("Teacher");
        VBox rolesBox = new VBox(5, adminChk, learnerChk, teacherChk);
        // Create buttons for updating roles and going back to the home screen.
        Button updateBtn = new Button("Update Roles");
        Button backBtn = new Button("Back");
        Label statusLbl = new Label();

        modGrid.add(userLbl, 0, 0);
        modGrid.add(userTxt, 1, 0);
        modGrid.add(rolesLbl, 0, 1);
        modGrid.add(rolesBox, 1, 1);
        modGrid.add(updateBtn, 1, 2);
        modGrid.add(backBtn, 0, 2);
        modGrid.add(statusLbl, 1, 3);

        // Set the window title to "Modify User Roles"
        window.setTitle("Modify User Roles");

        // On clicking the update Roles button, the following event handler takes control and let the user change his role
        updateBtn.setOnAction(e -> {
            String uname = userTxt.getText();
            // Validate the username
            if (!userDB.containsKey(uname)) {
                statusLbl.setText("User not found.");
                return;
            }
            // Check which roles are selected
            Set<String> roles = new HashSet<>();
            if (adminChk.isSelected()) {
                roles.add(ROLE_ADMINISTRATOR);
            }
            if (learnerChk.isSelected()) {
                roles.add(ROLE_LEARNER);
            }
            if (teacherChk.isSelected()) {
                roles.add(ROLE_TEACHER);
            }
            // If no role is chosen, prompt the user to select at least one
            if (roles.isEmpty()) {
                statusLbl.setText("Select at least one role.");
                return;
            }
            // Update the user's role in the database
            UserAccount user = userDB.get(uname);
            user.setRoles(roles);
            // Confirmation message if successful
            statusLbl.setText("Roles updated for " + uname);
        });
        // Back button to take the user back to the home screen
        backBtn.setOnAction(e -> displayHomeScreen(window, userDB.get(loggedInAdmin), ROLE_ADMINISTRATOR));

        Scene modScene = new Scene(modGrid, 500, 400);
        window.setScene(modScene);
    }
    // This method opens the screen where the user can enter an invitation code for registration.
    private GridPane enterInviteCode(Stage window) {
        // Create a new GridPane layout with padding and gaps.
        GridPane codeGrid = new GridPane();
        codeGrid.setPadding(new Insets(10));
        codeGrid.setVgap(8);
        codeGrid.setHgap(10);   
        // Labels and fields for getting the invite code
        Label codeLbl = new Label("Invitation Code:");
        TextField codeTxt = new TextField();
        // Two buttons for the user to choose
        Button contBtn = new Button("Continue");
        Button backBtn = new Button("Back");
        Label statusLbl = new Label();

        codeGrid.add(codeLbl, 0, 0);
        codeGrid.add(codeTxt, 1, 0);
        codeGrid.add(contBtn, 1, 1);
        codeGrid.add(backBtn, 0, 1);
        codeGrid.add(statusLbl, 1, 2);

        // Set the window title to "Enter Invitation Code"
        window.setTitle("Enter Invitation Code");

        // If continue button is clicked, the following event handler validates and checks if the invite code is correct
        contBtn.setOnAction(e -> {
            String code = codeTxt.getText();
            if (inviteDB.containsKey(code)) {
                Invite invite = inviteDB.get(code);
                // Redirect to the new user registration page if successful
                window.setScene(new Scene(registerNewUser(window, invite), 400, 250));
            } else {
                statusLbl.setText("Invalid code.");
            }
        });
        // If back button is clicked, the user is taken back to the home screen
        backBtn.setOnAction(e -> {
            window.setScene(new Scene(buildLoginScreen(window, window), 400, 350));
        });

        return codeGrid;
    }
    // This method opens the registration screen for creating a new user account after a valid invite.
    private GridPane registerNewUser(Stage window, Invite invite) {
        GridPane regGrid = new GridPane();
        regGrid.setPadding(new Insets(10));
        regGrid.setVgap(8);
        regGrid.setHgap(10);
        // labels and fields for entering username and password details.
        Label userLbl = new Label("Username:");
        TextField userTxt = new TextField();
        Label passLbl = new Label("Password:");
        PasswordField passTxt = new PasswordField();
        Label confirmPassLbl = new Label("Confirm Password:");
        PasswordField confirmPassTxt = new PasswordField();
        Button regBtn = new Button("Create Account");
        Label statusLbl = new Label();

        regGrid.add(userLbl, 0, 0);
        regGrid.add(userTxt, 1, 0);
        regGrid.add(passLbl, 0, 1);
        regGrid.add(passTxt, 1, 1);
        regGrid.add(confirmPassLbl, 0, 2);
        regGrid.add(confirmPassTxt, 1, 2);
        regGrid.add(regBtn, 1, 3);
        regGrid.add(statusLbl, 1, 4);

        // Set the window title to "User Registration"
        window.setTitle("User Registration");

        /*
        * On clicking the register button, the following event handler, after validating the user details,
        * creates a new user and stores his information in the database
        */
        regBtn.setOnAction(e -> {
            String uname = userTxt.getText();
            String pwd = passTxt.getText();
            String confirmPwd = confirmPassTxt.getText();
            // Input validation
            if (!validatePassword(pwd)) {
                statusLbl.setText("Password must be 8+ chars, with uppercase, digit, special char.");
                return;
            }
            // Check if both the passwords are same
            if (!pwd.equals(confirmPwd)) {
                statusLbl.setText("Passwords do not match.");
                return;
            }
            // If there already exists a user with the same username, it informs the user
            if (userDB.containsKey(uname)) {
                statusLbl.setText("Username exists.");
                return;
            }
            // A new user object to be added
            UserAccount newUser = new UserAccount(uname, pwd.toCharArray(), invite.getRoles());
            newUser.setEmail(invite.getEmail());
            newUser.setTopicLevel(invite.getTopicLevel());
            userDB.put(uname, newUser);
            // Remove the invite code from the database after successfully signing up
            inviteDB.remove(invite.getCode());
            // Alert the user of successful creation of the account
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registration Successful");
            alert.setHeaderText(null);
            alert.setContentText("Account created. Please log in.");
            alert.showAndWait();

            window.setScene(new Scene(buildLoginScreen(window, window), 400, 350));
        });

        return regGrid;
    }
    // A method to display the screen to allow selecting a role for any user
    private GridPane selectUserRole(Stage window, UserAccount user) {
        GridPane roleGrid = new GridPane();
        roleGrid.setPadding(new Insets(10));
        roleGrid.setVgap(8);
        roleGrid.setHgap(10);
        // Labels and TextFields for desired role
        Label selectLbl = new Label("Choose a role:");
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll(user.getRoles());
        roleCombo.setValue(roleCombo.getItems().get(0));
        Button contBtn = new Button("Continue");

        roleGrid.add(selectLbl, 0, 0);
        roleGrid.add(roleCombo, 0, 1);
        roleGrid.add(contBtn, 0, 2);

        // Set the window title to "Select Role"
        window.setTitle("Select Role");

        // If the continue button is pressed, after validating the choice, role is updated, and home screen is shown accordingly.
        contBtn.setOnAction(e -> {
            String selectedRole = roleCombo.getValue();
            if (selectedRole != null) {
                displayHomeScreen(window, user, selectedRole);
            }
        });

        return roleGrid;
    }
    // A method to build the password reset screen
    private GridPane buildPassResetScreen(Stage window, UserAccount user) {
        GridPane resetGrid = new GridPane();
        resetGrid.setPadding(new Insets(10));
        resetGrid.setVgap(8);
        resetGrid.setHgap(10);

        // required labels, password fields, button, and status label for feedback

        Label newPassLbl = new Label("New Password:");
        PasswordField newPassTxt = new PasswordField();
        Label confirmNewPassLbl = new Label("Confirm New Password:");
        PasswordField confirmNewPassTxt = new PasswordField();
        Button resetBtn = new Button("Reset Password");
        Label statusLbl = new Label();

        resetGrid.add(newPassLbl, 0, 0);
        resetGrid.add(newPassTxt, 1, 0);
        resetGrid.add(confirmNewPassLbl, 0, 1);
        resetGrid.add(confirmNewPassTxt, 1, 1);
        resetGrid.add(resetBtn, 1, 2);
        resetGrid.add(statusLbl, 1, 3);

        // Set the window title to "Reset Password"
        window.setTitle("Reset Password");

        // The setOnAction event is fired when the reset button is clicked.
        resetBtn.setOnAction(e -> {
            String newPwd = newPassTxt.getText();       // get the password input
            String confirmNewPwd = confirmNewPassTxt.getText();     //get the confirm password input
            
            // Check if both password and confirm password has the same value
            if (!validatePassword(newPwd)) {
                statusLbl.setText("Password must be 8+ chars, with uppercase, digit, special char.");
                return;
            }

            if (!newPwd.equals(confirmNewPwd)) {
                statusLbl.setText("Passwords do not match.");
                return;
            }
            // Otherwise, set the new password for the user
            user.setPassword(newPwd.toCharArray());
            user.setTempPass(false);  // remove the temporary password flag
            tempPassDB.remove(user.getUsername());

            statusLbl.setText("Password reset. Please log in.");            // Redirect the user back to the login page
            window.setScene(new Scene(buildLoginScreen(window, window), 400, 350));
        });

        return resetGrid;   // Return the complete password reset screen layout
    }
    // Builds the user profile completion screen
    private GridPane completeUserProfile(Stage window, UserAccount user) {
        GridPane profileGrid = new GridPane();
        profileGrid.setPadding(new Insets(10));
        profileGrid.setVgap(8);
        profileGrid.setHgap(10);
        
        // Declare and initialize the labels, text fields, and button for user input
        Label firstLbl = new Label("First Name:");
        TextField firstTxt = new TextField();
        Label middleLbl = new Label("Middle Name:");
        TextField middleTxt = new TextField();
        Label lastLbl = new Label("Last Name:");
        TextField lastTxt = new TextField();
        Label prefLbl = new Label("Preferred Name:");
        TextField prefTxt = new TextField();
        Label emailLbl = new Label("Email:");
        TextField emailTxt = new TextField(user.getEmail() != null ? user.getEmail() : "");
        Button finishBtn = new Button("Finish Setup");
        Label statusLbl = new Label();

        // After declaration and initialization, add final components to the grid with column and row positions
        profileGrid.add(firstLbl, 0, 0);
        profileGrid.add(firstTxt, 1, 0);
        profileGrid.add(middleLbl, 0, 1);
        profileGrid.add(middleTxt, 1, 1);
        profileGrid.add(lastLbl, 0, 2);
        profileGrid.add(lastTxt, 1, 2);
        profileGrid.add(prefLbl, 0, 3);
        profileGrid.add(prefTxt, 1, 3);
        profileGrid.add(emailLbl, 0, 4);
        profileGrid.add(emailTxt, 1, 4);
        profileGrid.add(finishBtn, 1, 5);
        profileGrid.add(statusLbl, 1, 6);

        // Set the window title to "Complete Profile"
        window.setTitle("Complete Profile");

        /* 
        * In the event that the finish button is pressed, we extract the user information, validate it, create a new profile for the user, and display the home screen
        * depending on the role of the user  
        */  
        finishBtn.setOnAction(e -> {
            String firstName = firstTxt.getText();
            String middleName = middleTxt.getText();
            String lastName = lastTxt.getText();
            String preferredName = prefTxt.getText();
            String email = emailTxt.getText();

            // Input validation takes place 
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                statusLbl.setText("Fill in required fields.");
                return;
            }

            user.setFirstName(firstName);
            user.setMiddleName(middleName);
            user.setLastName(lastName);
            user.setPreferredName(preferredName.isEmpty() ? null : preferredName); // Preferred name being optional, we assign a value if there is any.
            user.setEmail(email);
            
            // Show success message and proceed with role selection or home screen
            statusLbl.setText("Profile complete. Redirecting...");
            // If the user has more than one role, we ask which role he would like to move forward with
            if (user.getRoles().size() > 1) {
                window.setScene(new Scene(selectUserRole(window, user), 400, 200));
            } else {
                String role = user.getRoles().iterator().next();
                displayHomeScreen(window, user, role);
            }
        });

        return profileGrid;
    }
    // A method to  validate the password strength
    private boolean validatePassword(String pwd) {
        String pattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&#]).{8,}$";
        return Pattern.matches(pattern, pwd);
    }

    // The class for User Account with the required properties
    static class UserAccount {
        private String username;
        private char[] password;
        private Set<String> roles;
        private boolean tempPass;
        private String firstName;
        private String middleName;
        private String lastName;
        private String preferredName;
        private String email;
        private String topicLevel;

        /*
         * The constructor takes name, password, and the role from the user input. 
         * In case there is no role specified, it creates a set that can store the roles for the user.
         * topic level is by default set to intermediate
         */
        public UserAccount(String username, char[] password, Set<String> roles) {
            this.username = username;
            this.password = password;
            this.roles = roles != null ? roles : new HashSet<>();
            this.tempPass = false;
            this.topicLevel = "Intermediate";
        }

        // Getter and setter methods

        public String getUsername() {
            return username;
        }

        public char[] getPassword() {
            return password;
        }

        public void setPassword(char[] password) {
            this.password = password;
        }

        public Set<String> getRoles() {
            return roles;
        }

        public void setRoles(Set<String> roles) {
            this.roles = roles;
        }

        public boolean isTempPass() {
            return tempPass;
        }

        public void setTempPass(boolean tempPass) {
            this.tempPass = tempPass;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getMiddleName() {
            return middleName;
        }

        public void setMiddleName(String middleName) {
            this.middleName = middleName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getPreferredName() {
            return preferredName;
        }

        public void setPreferredName(String preferredName) {
            this.preferredName = preferredName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getTopicLevel() {
            return topicLevel;
        }

        public void setTopicLevel(String topicLevel) {
            this.topicLevel = topicLevel;
        }
    }

    // Invite class representing an invitation to a user
    static class Invite {
        private String code;  // invitation code
        private Set<String> roles;  // Set of roles associated with the invitation
        private String email;  // email address of the invited user
        private String topicLevel;  // Topic level for the user's access

        // Constructor to initialize the object with code, roles, email, and topic level
        public Invite(String code, Set<String> roles, String email, String topicLevel) {
            this.code = code;  
            this.roles = roles;  
            this.email = email;
            this.topicLevel = topicLevel;  
        }

        // Getter method for the invitation code
        public String getCode() {
            return code;  
        }

        // Getter method for the set of roles
        public Set<String> getRoles() {
            return roles;  
        }

        // Getter method for the email address
        public String getEmail() {
            return email; 
        }

        // Getter method for the topic level
        public String getTopicLevel() {
            return topicLevel;  
        }
    }

    // Class representing a temporary password for a user with its expiration time
    static class TempPass {
        private String password;  // Temporary pass
        private LocalDateTime expiry;  // Expiration time of the temporary pass

        // Constructor to initialize the TempPass object with password and expiry time
        public TempPass(String password, LocalDateTime expiry) {
            this.password = password;  
            this.expiry = expiry;  
        }

        // Getter method for the temporary password
        public String getPassword() {
            return password;  
        }

        // Getter method for the expiration time
        public LocalDateTime getExpiry() {
            return expiry;  
        }
    }
}
