package simpleDatabase;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import Encryption.EncryptionHelper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Controller class for user management functionalities.
 * Handles user registration, login, profile completion, password reset, etc.
 */
public class UserManagementController {

    // Simulated databases
    protected static Map<String, UserAccount> userDB = new HashMap<>();
    protected static Map<String, Invite> inviteDB = new HashMap<>();
    protected static Map<String, TempPass> tempPassDB = new HashMap<>();

    // Role constants
    protected static final String ROLE_ADMINISTRATOR = "Administrator";
    protected static final String ROLE_LEARNER = "Student";
    protected static final String ROLE_TEACHER = "Teacher";

    // Variables to keep track of the currently logged-in user's username
    protected String loggedInAdmin;

    // Encryption helper
    protected EncryptionHelper encryptionHelper;

    // Set the encryption helper
    public void setEncryptionHelper(EncryptionHelper helper) {
        this.encryptionHelper = helper;
    }

    // Admin registration scene
    public Scene initAdminRegistration(Stage window) {
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

        // Event handler for registration button
        regBtn.setOnAction(e -> {
            String uname = userTxt.getText();
            String pwd = passTxt.getText();
            String confirmPwd = confirmPassTxt.getText();

            if (!validatePassword(pwd)) {
                statusLbl.setText("Password must be 8+ chars, with uppercase, digit, special char.");
                return;
            }
            if (!pwd.equals(confirmPwd)) {
                statusLbl.setText("Passwords do not match.");
                return;
            }
            if (userDB.isEmpty()) {
                Set<String> roles = new HashSet<>();
                roles.add(ROLE_ADMINISTRATOR);
                UserAccount adminUser = new UserAccount(uname, pwd.toCharArray(), roles);
                userDB.put(uname, adminUser);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Registration Successful");
                alert.setHeaderText(null);
                alert.setContentText("Admin account created. Please log in.");
                alert.showAndWait();

                window.setScene(new Scene(buildLoginScreen(window), 400, 350));
            } else {
                statusLbl.setText("An admin already exists. Please log in.");
            }
        });

        window.setTitle("Admin Registration");

        return regScene;
    }

    // Build the login screen for users
    public GridPane buildLoginScreen(Stage window) {
        // Layout setup for the login screen with padding and spacing
        GridPane loginGrid = new GridPane();
        loginGrid.setPadding(new Insets(10));
        loginGrid.setVgap(8);
        loginGrid.setHgap(10);

        // Username and password labels and text fields
        Label userLbl = new Label("Username:");
        TextField userTxt = new TextField();
        Label passLbl = new Label("Password:");
        PasswordField passTxt = new PasswordField();

        // Login button and invitation code hyperlink
        Button loginBtn = new Button("Login");
        Hyperlink inviteLink = new Hyperlink("Use Invitation Code");

        // Status label to display login messages
        Label statusLbl = new Label();

        // Add elements to login screen layout
        loginGrid.add(userLbl, 0, 0);
        loginGrid.add(userTxt, 1, 0);
        loginGrid.add(passLbl, 0, 1);
        loginGrid.add(passTxt, 1, 1);
        loginGrid.add(loginBtn, 1, 2);
        loginGrid.add(inviteLink, 1, 3);
        loginGrid.add(statusLbl, 1, 4);

        // Set window title
        window.setTitle("Login");

        // Define login button functionality
        loginBtn.setOnAction(e -> {
            String uname = userTxt.getText(); // Get entered username
            String pwd = passTxt.getText();   // Get entered password

            // Verify if user exists in the database
            if (userDB.containsKey(uname)) {
                UserAccount user = userDB.get(uname);

                // Check if the user has a temporary password
                if (user.isTempPass()) {
                    TempPass tp = tempPassDB.get(uname);
                    // Verify temporary password validity and expiry
                    if (tp != null && tp.getPassword().equals(pwd)) {
                        if (LocalDateTime.now().isAfter(tp.getExpiry())) {
                            statusLbl.setText("Temporary password expired.");
                            return;
                        }
                        // Redirect to password reset screen
                        window.setScene(new Scene(buildPassResetScreen(window, user), 400, 300));
                        return;
                    } else {
                        statusLbl.setText("Invalid temporary password.");
                        return;
                    }
                }

                // Check if entered password matches the stored password
                if (String.valueOf(user.getPassword()).equals(pwd)) {
                    // Check if the user's profile is complete
                    if (user.getFirstName() == null || user.getLastName() == null || user.getEmail() == null) {
                        window.setScene(new Scene(completeUserProfile(window, user), 500, 400));
                    } else {
                        // Handle cases based on the user's roles
                        if (user.getRoles().size() > 1) {
                            window.setScene(new Scene(selectUserRole(window, user), 400, 200));
                        } else {
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

        // Define action for invitation code link
        inviteLink.setOnAction(e -> {
            window.setScene(new Scene(enterInviteCode(window), 400, 200));
        });

        return loginGrid;
    }

    // Display home screen for different user roles
    public void displayHomeScreen(Stage window, UserAccount user, String role) {
        VBox homeLayout = new VBox(10); // Vertical layout for home screen
        homeLayout.setPadding(new Insets(10));

        // Display welcome message with the user's preferred or first name
        String displayName = user.getPreferredName() != null ? user.getPreferredName() : user.getFirstName();
        Label welcomeLbl = new Label("Welcome, " + displayName + " (" + role + ")!");
        Button logoutBtn = new Button("Logout");

        homeLayout.getChildren().addAll(welcomeLbl, logoutBtn);

        // Add admin-specific controls if the user has an administrator role
        if (ROLE_ADMINISTRATOR.equals(role)) {
            loggedInAdmin = user.getUsername();

            // Buttons for user management and article management
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

            // Article management controls
            ArticleManagementController articleController = new ArticleManagementController();
            articleController.setEncryptionHelper(encryptionHelper);

            Button createArticleBtn = new Button("Create Help Article");
            createArticleBtn.setOnAction(e -> articleController.openCreateArticleScreen(window, user));

            Button manageArticlesBtn = new Button("Manage Help Articles");
            manageArticlesBtn.setOnAction(e -> articleController.openManageArticlesScreen(window, user));

            Button backupBtn = new Button("Backup Articles");
            backupBtn.setOnAction(e -> articleController.openBackupArticlesScreen(window, user));

            Button restoreBtn = new Button("Restore Articles");
            restoreBtn.setOnAction(e -> articleController.openRestoreArticlesScreen(window, user));

            // Add all admin controls to home layout
            homeLayout.getChildren().addAll(inviteBtn, resetBtn, deleteBtn, listBtn, modifyBtn,
                    createArticleBtn, manageArticlesBtn, backupBtn, restoreBtn);

            window.setTitle("Admin Home");
        } else if (ROLE_TEACHER.equals(role)) {
            // Teacher-specific controls for article management
            ArticleManagementController articleController = new ArticleManagementController();
            articleController.setEncryptionHelper(encryptionHelper);

            Button createArticleBtn = new Button("Create Help Article");
            createArticleBtn.setOnAction(e -> articleController.openCreateArticleScreen(window, user));

            Button manageArticlesBtn = new Button("Manage Help Articles");
            manageArticlesBtn.setOnAction(e -> articleController.openManageArticlesScreen(window, user));

            Button backupBtn = new Button("Backup Articles");
            backupBtn.setOnAction(e -> articleController.openBackupArticlesScreen(window, user));

            Button restoreBtn = new Button("Restore Articles");
            restoreBtn.setOnAction(e -> articleController.openRestoreArticlesScreen(window, user));

            homeLayout.getChildren().addAll(createArticleBtn, manageArticlesBtn, backupBtn, restoreBtn);

            window.setTitle("Instructor Home");
        } else {
            // Default user home screen with basic functionality
            window.setTitle("User Home");
            ArticleManagementController articleController = new ArticleManagementController();
            articleController.setEncryptionHelper(encryptionHelper);

            Button searchArticlesBtn = new Button("Search Help Articles");
            searchArticlesBtn.setOnAction(e -> articleController.openSearchArticlesScreen(window, user));
            homeLayout.getChildren().add(searchArticlesBtn);
        }

        // Logout button action to return to the login screen
        logoutBtn.setOnAction(e -> {
            loggedInAdmin = null;
            window.setScene(new Scene(buildLoginScreen(window), 400, 350));
        });

        // Set home screen dimensions and display
        Scene homeScene = new Scene(homeLayout, 400, 500);
        window.setScene(homeScene);
    }

    // Validate password against a strong password pattern
    protected boolean validatePassword(String pwd) {
        String pattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&#]).{8,}$";
        return Pattern.matches(pattern, pwd);
    }

    // Open screen for inviting a new user (admin function)
    public void openInviteUserScreen(Stage window) {
        GridPane inviteGrid = new GridPane(); // Layout for invite screen
        inviteGrid.setPadding(new Insets(10));
        inviteGrid.setVgap(8);
        inviteGrid.setHgap(10);

        // Input fields for email, roles, and topic level
        Label emailLbl = new Label("Email:");
        TextField emailTxt = new TextField();

        Label rolesLbl = new Label("Roles:");
        CheckBox adminChk = new CheckBox("Admin");
        CheckBox learnerChk = new CheckBox("Student");
        CheckBox teacherChk = new CheckBox("Teacher");
        VBox rolesBox = new VBox(5, adminChk, learnerChk, teacherChk);

        Label levelLbl = new Label("Topic Level:");
        ComboBox<String> levelCombo = new ComboBox<>();
        levelCombo.getItems().addAll("Beginner", "Intermediate", "Advanced", "Expert");
        levelCombo.setValue("Intermediate");

        // Buttons for sending invite and returning back
        Button sendInviteBtn = new Button("Send Invite");
        Button backBtn = new Button("Back");
        Label statusLbl = new Label();

        // Add elements to invite screen layout
        inviteGrid.add(emailLbl, 0, 0);
        inviteGrid.add(emailTxt, 1, 0);
        inviteGrid.add(rolesLbl, 0, 1);
        inviteGrid.add(rolesBox, 1, 1);
        inviteGrid.add(levelLbl, 0, 2);
        inviteGrid.add(levelCombo, 1, 2);
        inviteGrid.add(sendInviteBtn, 1, 3);
        inviteGrid.add(backBtn, 0, 3);
        inviteGrid.add(statusLbl, 1, 4);

        window.setTitle("Invite User");

        // Action for sending an invitation
        sendInviteBtn.setOnAction(e -> {
            String email = emailTxt.getText();
            String topicLevel = levelCombo.getValue();
            String code = generateInviteCode();

            Set<String> roles = new HashSet<>();
            if (adminChk.isSelected()) roles.add(ROLE_ADMINISTRATOR);
            if (learnerChk.isSelected()) roles.add(ROLE_LEARNER);
            if (teacherChk.isSelected()) roles.add(ROLE_TEACHER);

            if (roles.isEmpty()) {
                statusLbl.setText("Select at least one role.");
                return;
            }

            Invite invite = new Invite(code, roles, email, topicLevel);
            inviteDB.put(code, invite);
            statusLbl.setText("Invitation sent to " + email + " with code: " + code);
        });

        // Action for back button to return to admin home screen
        backBtn.setOnAction(e -> displayHomeScreen(window, userDB.get(loggedInAdmin), ROLE_ADMINISTRATOR));

        Scene inviteScene = new Scene(inviteGrid, 500, 400);
        window.setScene(inviteScene);
    }

    // Generate a 6-character code for invitations
    protected String generateInviteCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        StringBuilder code = new StringBuilder(6); // Code length is 6
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return code.toString();
    }

    // Reset user account screen for setting temporary passwords
    public void openResetUserScreen(Stage window) {
        GridPane resetGrid = new GridPane();
        resetGrid.setPadding(new Insets(10));
        resetGrid.setVgap(8);
        resetGrid.setHgap(10);

        // Input fields for username, expiry date, and expiry time
        Label userLbl = new Label("Username:");
        TextField userTxt = new TextField();
        Label dateLbl = new Label("Expiry Date (yyyy-MM-dd):");
        TextField dateTxt = new TextField();
        Label timeLbl = new Label("Expiry Time (24-hour HH:mm):");
        TextField timeTxt = new TextField();

        // Buttons for reset and back, with status label
        Button resetBtn = new Button("Reset Account");
        Button backBtn = new Button("Back");
        Label statusLbl = new Label();

        resetGrid.add(userLbl, 0, 0);
        resetGrid.add(userTxt, 1, 0);
        resetGrid.add(dateLbl, 0, 1);
        resetGrid.add(dateTxt, 1, 1);
        resetGrid.add(timeLbl, 0, 2);
        resetGrid.add(timeTxt, 1, 2);
        resetGrid.add(resetBtn, 1, 3);
        resetGrid.add(backBtn, 0, 3);
        resetGrid.add(statusLbl, 1, 4);

        window.setTitle("Reset User Account");

        // Action for reset button to set temporary password
        resetBtn.setOnAction(e -> {
            String uname = userTxt.getText();
            String dateStr = dateTxt.getText();
            String timeStr = timeTxt.getText();

            if (!userDB.containsKey(uname)) {
                statusLbl.setText("User not found.");
                return;
            }
            if (dateStr.isEmpty() || timeStr.isEmpty()) {
                statusLbl.setText("Enter date and time.");
                return;
            }

            // Attempt to parse date and time and set temporary password
            try {
                DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                LocalDateTime expiryDT = LocalDateTime.parse(dateStr + "T" + timeStr, dtFormatter);

                String tempPass = generateTempPassword();
                TempPass tempPassword = new TempPass(tempPass, expiryDT);
                tempPassDB.put(uname, tempPassword);

                UserAccount user = userDB.get(uname);
                user.setTempPass(true);

                statusLbl.setText("Temporary password set for " + uname + ": " + tempPass);

            } catch (Exception ex) {
                statusLbl.setText("Invalid date/time format.");
            }
        });

        // Action for back button to return to admin home screen
        backBtn.setOnAction(e -> displayHomeScreen(window, userDB.get(loggedInAdmin), ROLE_ADMINISTRATOR));

        Scene resetScene = new Scene(resetGrid, 500, 300);
        window.setScene(resetScene);
    }


    // Generate Temporary Password
    protected String generateTempPassword() {
        Random rnd = new Random();
        int tempPass = 100000 + rnd.nextInt(900000);
        return String.valueOf(tempPass);
    }

    // Delete User Screen
    public void openDeleteUserScreen(Stage window) {
        GridPane delGrid = new GridPane();
        delGrid.setPadding(new Insets(10));
        delGrid.setVgap(8);
        delGrid.setHgap(10);

        Label userLbl = new Label("Username:");
        TextField userTxt = new TextField();
        Button delBtn = new Button("Delete User");
        Button backBtn = new Button("Back");
        Label statusLbl = new Label();

        delGrid.add(userLbl, 0, 0);
        delGrid.add(userTxt, 1, 0);
        delGrid.add(delBtn, 1, 1);
        delGrid.add(backBtn, 0, 1);
        delGrid.add(statusLbl, 1, 2);

        window.setTitle("Delete User Account");

        delBtn.setOnAction(e -> {
            String uname = userTxt.getText();
            if (!userDB.containsKey(uname)) {
                statusLbl.setText("User not found.");
                return;
            }
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete User");
            alert.setHeaderText("Delete user: " + uname + "?");
            alert.setContentText("This cannot be undone.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                userDB.remove(uname);
                statusLbl.setText("User " + uname + " deleted.");
            } else {
                statusLbl.setText("Deletion canceled.");
            }
        });

        backBtn.setOnAction(e -> displayHomeScreen(window, userDB.get(loggedInAdmin), ROLE_ADMINISTRATOR));

        Scene delScene = new Scene(delGrid, 500, 300);
        window.setScene(delScene);
    }

    // List Users Screen
    public void openListUsersScreen(Stage window) {
        VBox listLayout = new VBox(10);
        listLayout.setPadding(new Insets(10));

        Label titleLbl = new Label("User Accounts:");
        listLayout.getChildren().add(titleLbl);

        for (UserAccount user : userDB.values()) {
            String fullName = (user.getFirstName() != null ? user.getFirstName() : "") + " "
                    + (user.getMiddleName() != null ? user.getMiddleName() + " " : "")
                    + (user.getLastName() != null ? user.getLastName() : "");
            Label userLbl = new Label("Username: " + user.getUsername() +
                    ", Name: " + fullName.trim() +
                    ", Roles: " + user.getRoles());
            listLayout.getChildren().add(userLbl);
        }

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> displayHomeScreen(window, userDB.get(loggedInAdmin), ROLE_ADMINISTRATOR));
        listLayout.getChildren().add(backBtn);

        window.setTitle("List Users");

        Scene listScene = new Scene(listLayout, 500, 400);
        window.setScene(listScene);
    }

    // Modify User Roles Screen
    public void openModifyRolesScreen(Stage window) {
        GridPane modGrid = new GridPane();
        modGrid.setPadding(new Insets(10));
        modGrid.setVgap(8);
        modGrid.setHgap(10);

        Label userLbl = new Label("Username:");
        TextField userTxt = new TextField();
        Label rolesLbl = new Label("Roles:");
        CheckBox adminChk = new CheckBox("Admin");
        CheckBox learnerChk = new CheckBox("Student");
        CheckBox teacherChk = new CheckBox("Teacher");
        VBox rolesBox = new VBox(5, adminChk, learnerChk, teacherChk);
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

        window.setTitle("Modify User Roles");

        updateBtn.setOnAction(e -> {
            String uname = userTxt.getText();
            if (!userDB.containsKey(uname)) {
                statusLbl.setText("User not found.");
                return;
            }
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
            if (roles.isEmpty()) {
                statusLbl.setText("Select at least one role.");
                return;
            }
            UserAccount user = userDB.get(uname);
            user.setRoles(roles);
            statusLbl.setText("Roles updated for " + uname);
        });

        backBtn.setOnAction(e -> displayHomeScreen(window, userDB.get(loggedInAdmin), ROLE_ADMINISTRATOR));

        Scene modScene = new Scene(modGrid, 500, 400);
        window.setScene(modScene);
    }

    // Enter Invitation Code Screen
    public GridPane enterInviteCode(Stage window) {
        GridPane codeGrid = new GridPane();
        codeGrid.setPadding(new Insets(10));
        codeGrid.setVgap(8);
        codeGrid.setHgap(10);

        Label codeLbl = new Label("Invitation Code:");
        TextField codeTxt = new TextField();
        Button contBtn = new Button("Continue");
        Button backBtn = new Button("Back");
        Label statusLbl = new Label();

        codeGrid.add(codeLbl, 0, 0);
        codeGrid.add(codeTxt, 1, 0);
        codeGrid.add(contBtn, 1, 1);
        codeGrid.add(backBtn, 0, 1);
        codeGrid.add(statusLbl, 1, 2);

        window.setTitle("Enter Invitation Code");

        contBtn.setOnAction(e -> {
            String code = codeTxt.getText();
            if (inviteDB.containsKey(code)) {
                Invite invite = inviteDB.get(code);
                window.setScene(new Scene(registerNewUser(window, invite), 400, 250));
            } else {
                statusLbl.setText("Invalid code.");
            }
        });

        backBtn.setOnAction(e -> {
            window.setScene(new Scene(buildLoginScreen(window), 400, 350));
        });

        return codeGrid;
    }

 // Register New User Screen
    public GridPane registerNewUser(Stage window, Invite invite) {
        // Create layout grid for registration form
        GridPane regGrid = new GridPane();
        regGrid.setPadding(new Insets(10)); // Padding around grid
        regGrid.setVgap(8);                 // Vertical gap between elements
        regGrid.setHgap(10);                // Horizontal gap between elements

        // Labels and fields for user input
        Label userLbl = new Label("Username:");
        TextField userTxt = new TextField();
        Label passLbl = new Label("Password:");
        PasswordField passTxt = new PasswordField();
        Label confirmPassLbl = new Label("Confirm Password:");
        PasswordField confirmPassTxt = new PasswordField();
        Button regBtn = new Button("Create Account"); // Submit button for registration
        Label statusLbl = new Label(); // Status message for feedback

        // Add all components to grid layout
        regGrid.add(userLbl, 0, 0);
        regGrid.add(userTxt, 1, 0);
        regGrid.add(passLbl, 0, 1);
        regGrid.add(passTxt, 1, 1);
        regGrid.add(confirmPassLbl, 0, 2);
        regGrid.add(confirmPassTxt, 1, 2);
        regGrid.add(regBtn, 1, 3);
        regGrid.add(statusLbl, 1, 4);

        // Set window title
        window.setTitle("User Registration");

        // Action handler for registration button
        regBtn.setOnAction(e -> {
            String uname = userTxt.getText();         // Get username
            String pwd = passTxt.getText();           // Get password
            String confirmPwd = confirmPassTxt.getText(); // Confirm password

            // Check if password meets security requirements
            if (!validatePassword(pwd)) {
                statusLbl.setText("Password must be 8+ chars, with uppercase, digit, special char.");
                return;
            }
            // Check if passwords match
            if (!pwd.equals(confirmPwd)) {
                statusLbl.setText("Passwords do not match.");
                return;
            }
            // Check if username already exists
            if (userDB.containsKey(uname)) {
                statusLbl.setText("Username exists.");
                return;
            }

            // Register new user with details from the invite
            UserAccount newUser = new UserAccount(uname, pwd.toCharArray(), invite.getRoles());
            newUser.setEmail(invite.getEmail());
            newUser.setTopicLevel(invite.getTopicLevel());
            userDB.put(uname, newUser); // Add new user to user database

            // Remove used invite from invite database
            inviteDB.remove(invite.getCode());

            // Inform user of successful registration
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registration Successful");
            alert.setHeaderText(null);
            alert.setContentText("Account created. Please log in.");
            alert.showAndWait();

            // Redirect to login screen
            window.setScene(new Scene(buildLoginScreen(window), 400, 350));
        });

        return regGrid;
    }

    // Select User Role Screen
    public GridPane selectUserRole(Stage window, UserAccount user) {
        // Create grid for role selection
        GridPane roleGrid = new GridPane();
        roleGrid.setPadding(new Insets(10)); // Padding around grid
        roleGrid.setVgap(8);                 // Vertical gap between elements
        roleGrid.setHgap(10);                // Horizontal gap between elements

        // Label and dropdown for selecting role
        Label selectLbl = new Label("Choose a role:");
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll(user.getRoles()); // Populate dropdown with user's roles
        roleCombo.setValue(roleCombo.getItems().get(0)); // Set default value
        Button contBtn = new Button("Continue"); // Continue button

        // Add components to grid layout
        roleGrid.add(selectLbl, 0, 0);
        roleGrid.add(roleCombo, 0, 1);
        roleGrid.add(contBtn, 0, 2);

        // Set window title
        window.setTitle("Select Role");

        // Event handler for continue button
        contBtn.setOnAction(e -> {
            String selectedRole = roleCombo.getValue(); // Get selected role
            if (selectedRole != null) {
                displayHomeScreen(window, user, selectedRole); // Display home screen for chosen role
            }
        });

        return roleGrid;
    }

    // Password Reset Screen
    public GridPane buildPassResetScreen(Stage window, UserAccount user) {
        // Create grid for password reset form
        GridPane resetGrid = new GridPane();
        resetGrid.setPadding(new Insets(10)); // Padding around grid
        resetGrid.setVgap(8);                 // Vertical gap between elements
        resetGrid.setHgap(10);                // Horizontal gap between elements

        // Labels and fields for new password input
        Label newPassLbl = new Label("New Password:");
        PasswordField newPassTxt = new PasswordField();
        Label confirmNewPassLbl = new Label("Confirm New Password:");
        PasswordField confirmNewPassTxt = new PasswordField();
        Button resetBtn = new Button("Reset Password"); // Submit button for resetting password
        Label statusLbl = new Label(); // Status message for feedback

        // Add components to grid layout
        resetGrid.add(newPassLbl, 0, 0);
        resetGrid.add(newPassTxt, 1, 0);
        resetGrid.add(confirmNewPassLbl, 0, 1);
        resetGrid.add(confirmNewPassTxt, 1, 1);
        resetGrid.add(resetBtn, 1, 2);
        resetGrid.add(statusLbl, 1, 3);

        // Set window title
        window.setTitle("Reset Password");

        // Event handler for reset button
        resetBtn.setOnAction(e -> {
            String newPwd = newPassTxt.getText(); // Get new password
            String confirmNewPwd = confirmNewPassTxt.getText(); // Confirm new password

            // Check if new password meets security requirements
            if (!validatePassword(newPwd)) {
                statusLbl.setText("Password must be 8+ chars, with uppercase, digit, special char.");
                return;
            }

            // Check if new passwords match
            if (!newPwd.equals(confirmNewPwd)) {
                statusLbl.setText("Passwords do not match.");
                return;
            }

            // Update user's password and clear temporary password status
            user.setPassword(newPwd.toCharArray());
            user.setTempPass(false);
            tempPassDB.remove(user.getUsername()); // Remove temporary password from tempPass database

            // Inform user of successful password reset
            statusLbl.setText("Password reset. Please log in.");
            window.setScene(new Scene(buildLoginScreen(window), 400, 350)); // Redirect to login screen
        });

        return resetGrid;
    }

    // Complete User Profile Screen
    public GridPane completeUserProfile(Stage window, UserAccount user) {
        // Create grid for user profile form
        GridPane profileGrid = new GridPane();
        profileGrid.setPadding(new Insets(10)); // Padding around grid
        profileGrid.setVgap(8);                 // Vertical gap between elements
        profileGrid.setHgap(10);                // Horizontal gap between elements

        // Labels and fields for profile information
        Label firstLbl = new Label("First Name:");
        TextField firstTxt = new TextField();
        Label middleLbl = new Label("Middle Name:");
        TextField middleTxt = new TextField();
        Label lastLbl = new Label("Last Name:");
        TextField lastTxt = new TextField();
        Label prefLbl = new Label("Preferred Name:");
        TextField prefTxt = new TextField();
        Label emailLbl = new Label("Email:");
        TextField emailTxt = new TextField(user.getEmail() != null ? user.getEmail() : ""); // Pre-fill email if available
        Button finishBtn = new Button("Finish Setup"); // Submit button for profile completion
        Label statusLbl = new Label(); // Status message for feedback

        // Add components to grid layout
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

        // Set window title
        window.setTitle("Complete Profile");

        // Event handler for finish setup button
        finishBtn.setOnAction(e -> {
            String firstName = firstTxt.getText(); // Get first name
            String middleName = middleTxt.getText(); // Get middle name
            String lastName = lastTxt.getText(); // Get last name
            String preferredName = prefTxt.getText(); // Get preferred name
            String email = emailTxt.getText(); // Get email

            // Check if required fields are filled
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                statusLbl.setText("Fill in required fields.");
                return;
            }

            // Set user profile fields
            user.setFirstName(firstName);
            user.setMiddleName(middleName);
            user.setLastName(lastName);
            user.setPreferredName(preferredName.isEmpty() ? null : preferredName);
            user.setEmail(email);

            // Inform user of successful profile completion and redirect
            statusLbl.setText("Profile complete. Redirecting...");
            if (user.getRoles().size() > 1) {
                window.setScene(new Scene(selectUserRole(window, user), 400, 200)); // Go to role selection screen if multiple roles
            } else {
                String role = user.getRoles().iterator().next(); // Get the only role
                displayHomeScreen(window, user, role); // Display home screen for assigned role
            }
        });

        return profileGrid;
    }
}
