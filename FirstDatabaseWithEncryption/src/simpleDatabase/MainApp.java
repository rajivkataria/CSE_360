package simpleDatabase;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import Encryption.EncryptionHelper;
/*
	* Main application class launches the JavaFX application.
	* Contains the main method and initial setup for encryption.
*/
public class MainApp extends Application {

    // Instance of EncryptionHelper for encryption tasks
    protected EncryptionHelper encryptionHelper;

    public static void main(String[] args) {
        // Add BouncyCastle as the security provider
        Security.addProvider(new BouncyCastleProvider());
        launch(args); // Launch the JavaFX application
    }

    @Override
    public void start(Stage mainWindow) {
        try {
            encryptionHelper = new EncryptionHelper(); // Initialize encryption helper
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Initializes the User Management Controller
        UserManagementController userController = new UserManagementController();
        userController.setEncryptionHelper(encryptionHelper);

        // Check if any users exist already in db
        if (UserManagementController.userDB.isEmpty()) {
            // If no users, show admin registration scene
            mainWindow.setScene(userController.initAdminRegistration(mainWindow));
        } else {
            // Otherwise, show the default login screen
            mainWindow.setScene(new Scene(userController.buildLoginScreen(mainWindow), 400, 350));
        }
        mainWindow.show(); // Display the window
    }
}
