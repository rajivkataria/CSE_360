package simpleDatabase;

import java.io.Serializable;
import java.util.Set;

// The UserAccount class represents a user account
public class UserAccount implements Serializable {
    private static final long serialVersionUID = 1L;
    // User properties/attributes
    private String username; 
    private char[] password;
    private Set<String> roles;
    private boolean tempPass; // Indicates if the user has a temporary password
    private String firstName;
    private String middleName;
    private String lastName;
    private String preferredName;
    private String email;
    private String topicLevel; // User's topic proficiency level

    // Constructor to initialize UserAccount with username, password, and roles
    public UserAccount(String username, char[] password, Set<String> roles) {
        this.username = username;
        this.roles = roles != null ? roles : new java.util.HashSet<>(); // Initialize roles if null
        this.password = password;
        this.tempPass = false; // Default value is set to false
        this.topicLevel = "Intermediate"; // Default topic level is set to Intermediate
    }

    // Getters and Setters
    
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

    // Sets the user's topic proficiency level
    public void setTopicLevel(String topicLevel) {
        this.topicLevel = topicLevel;
    }

    // Gets the user's topic proficiency level
    public String getTopicLevel() {
        return topicLevel;
    }
}
