package simpleDatabase;

import java.util.Set;

// Class representing an invite with roles and topic level details
public class Invite {
    // Invite attributes
    private String code; // Unique code for the invite
    private Set<String> roles; // Roles assigned to the invitee. admin, viewer etc
    private String email; 
    private String topicLevel; 

    // Constructor to initialize an invite with details
    public Invite(String code, Set<String> roles, String email, String topicLevel) {
        this.code = code;
        this.roles = roles;
        this.email = email;
        this.topicLevel = topicLevel;
    }

    // Getters for invite properties
    public String getCode() {
        return code; 
    }

    public Set<String> getRoles() {
        return roles; 
    }

    public String getEmail() {
        return email; 
    }

    public String getTopicLevel() {
        return topicLevel; 
    }
}
