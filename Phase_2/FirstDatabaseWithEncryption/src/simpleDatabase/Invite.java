package simpleDatabase;

import java.io.Serializable;
import java.util.Set;

// The Invite class generates an invitation code for new users
public class Invite implements Serializable {
    private static final long serialVersionUID = 1L;

    private String code; // unique invite code
    private Set<String> roles; // roles assigned to the invitee
    private String email; // invitee's mail
    private String topicLevel; // topic level associated with the invite

    // Constructor to initialize an invite with code, roles, email, and topic level
    public Invite(String code, Set<String> roles, String email, String topicLevel) {
        this.code = code;
        this.roles = roles;
        this.email = email;
        this.topicLevel = topicLevel;
    }

    // Gets the invitation code
    public String getCode() {
        return code;
    }

    // Gets the roles associated with the invite
    public Set<String> getRoles() {
        return roles;
    }

    // Gets the invitee's email
    public String getEmail() {
        return email;
    }

    // Gets the topic level associated with the invitation
    public String getTopicLevel() {
        return topicLevel;
    }
}
