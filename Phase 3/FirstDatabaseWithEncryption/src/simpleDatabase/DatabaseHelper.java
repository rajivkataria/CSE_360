package simpleDatabase;

import java.sql.*;
import java.util.Base64;

import org.bouncycastle.util.Arrays;

import Encryption.EncryptionHelper;
import Encryption.EncryptionUtils;

/**
 * DatabaseHelper is a utility class for managing database connections and
 * operations related to user management, including registration, login,
 * and retrieval of user details. This class integrates encryption to
 * securely handle passwords.
 */
class DatabaseHelper {

    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "org.h2.Driver";   
    static final String DB_URL = "jdbc:h2:~/firstDatabase";

    // Database credentials
    static final String USER = "sa"; 
    static final String PASS = "";

    private Connection connection = null;
    private Statement statement = null;
    
    // Helper for encryption and decryption of passwords
    private EncryptionHelper encryptionHelper;

    /**
     * Initializes the DatabaseHelper and sets up encryption.
     * throws Exception if initialization of EncryptionHelper fails.
     */
    public DatabaseHelper() throws Exception {
        encryptionHelper = new EncryptionHelper();
    }

    /**
     * Connects to the database by loading the JDBC driver and establishing a connection.
     * throws SQLException if a database access error occurs.
     */
    public void connectToDatabase() throws SQLException {
        try {
            Class.forName(JDBC_DRIVER); // Load the JDBC driver
            System.out.println("Connecting to database...");
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement();
            createTables();  // Create necessary tables if they don't exist
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        }
    }

    /**
     * Creates the tables required for the application if they do not exist.
     * throws SQLException if there is an error in table creation.
     */
    private void createTables() throws SQLException {
        String userTable = "CREATE TABLE IF NOT EXISTS articles (" +
                           "id INT AUTO_INCREMENT PRIMARY KEY, " +
                           "title VARCHAR(255), " +
                           "authors VARCHAR(255), " +
                           "abstract VARCHAR(255), " +
                           "keywords VARCHAR(255), " +
                           "body TEXT, " +
                           "references TEXT)";
        statement.execute(userTable);
    }

    /**
     * Checks if the database is empty by querying the number of articles.
     * returns true if the database is empty, false otherwise.
     * throws SQLException if a database access error occurs.
     */
    public boolean isDatabaseEmpty() throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM articles";
        ResultSet resultSet = statement.executeQuery(query);
        if (resultSet.next()) {
            return resultSet.getInt("count") == 0;
        }
        return true;
    }

    /**
     * Registers a new user by storing the encrypted password and role.
     * throws Exception if encryption or SQL execution fails.
     */
    public void register(String email, String password, String role) throws Exception {
        String encryptedPassword = Base64.getEncoder().encodeToString(
                encryptionHelper.encrypt(password.getBytes(), EncryptionUtils.getInitializationVector(email.toCharArray()))
        );
        
        String insertUser = "INSERT INTO cse360users (email, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
            pstmt.setString(1, email);
            pstmt.setString(2, encryptedPassword);
            pstmt.setString(3, role);
            pstmt.executeUpdate();
        }
    }

    /**
     * Authenticates a user by verifying the encrypted password and role.
     * returns true if the credentials are correct, false otherwise.
     * throws Exception if encryption or SQL execution fails.
     */
    public boolean login(String email, String password, String role) throws Exception {
        String encryptedPassword = Base64.getEncoder().encodeToString(
                encryptionHelper.encrypt(password.getBytes(), EncryptionUtils.getInitializationVector(email.toCharArray()))
        );

        String query = "SELECT * FROM cse360users WHERE email = ? AND password = ? AND role = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, email);
            pstmt.setString(2, encryptedPassword);
            pstmt.setString(3, role);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // True if a matching record is found
            }
        }
    }

    /*
     * Checks if a user with the given email exists in the database.
     * returns true if the user exists, false otherwise.
     */
    public boolean doesUserExist(String email) {
        String query = "SELECT COUNT(*) FROM cse360users WHERE email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
     * Displays all user details for admin viewing, including decrypted passwords.
     */
    public void displayUsersByAdmin() throws Exception {
        String sql = "SELECT * FROM cse360users";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            int id = rs.getInt("id");
            String email = rs.getString("email");
            String role = rs.getString("role");
            String encryptedPassword = rs.getString("password");
            char[] decryptedPassword = EncryptionUtils.toCharArray(
                    encryptionHelper.decrypt(
                            Base64.getDecoder().decode(encryptedPassword),
                            EncryptionUtils.getInitializationVector(email.toCharArray())
                    )
            );

            // Displays values
            System.out.print("ID: " + id);
            System.out.print(", Email: " + email);
            System.out.print(", Password: ");
            EncryptionUtils.printCharArray(decryptedPassword);
            System.out.println(", Role: " + role);

            Arrays.fill(decryptedPassword, '0'); // Clear password from memory
        }
    }

    /*
     * Displays basic user details for non-admin viewing, including decrypted passwords.
     */
    public void displayUsersByUser() throws Exception {
        String sql = "SELECT * FROM cse360users";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            int id = rs.getInt("id");
            String email = rs.getString("email");
            String role = rs.getString("role");
            String encryptedPassword = rs.getString("password");
            char[] decryptedPassword = EncryptionUtils.toCharArray(
                    encryptionHelper.decrypt(
                            Base64.getDecoder().decode(encryptedPassword),
                            EncryptionUtils.getInitializationVector(email.toCharArray())
                    )
            );

            // Displays values
            System.out.print("ID: " + id);
            System.out.print(", Email: " + email);
            System.out.print(", Password: ");
            EncryptionUtils.printCharArray(decryptedPassword);
            System.out.println(", Role: " + role);

            Arrays.fill(decryptedPassword, '0'); // Clear password from memory
        }
    }

    // Closes the database connection and statement resources, handling any SQL exceptions.
    public void closeConnection() {
        try {
            if (statement != null) statement.close();
        } catch (SQLException se2) {
            se2.printStackTrace();
        }
        try {
            if (connection != null) connection.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }
}
