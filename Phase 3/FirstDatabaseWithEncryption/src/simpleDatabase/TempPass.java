package simpleDatabase;

import java.time.LocalDateTime;

// Class representing a temporary password with an expiration time
public class TempPass {
    // Temporary password Attributes
    private String password; 
    private LocalDateTime expiry; 

    // Constructor to initialize temporary password and its expiry date
    public TempPass(String password, LocalDateTime expiry) {
        this.password = password;
        this.expiry = expiry;
    }

    // Getter for the temporary password
    public String getPassword() {
        return password; 
    }

    // Getter for the expiration time
    public LocalDateTime getExpiry() {
        return expiry; 
    }

    // Setter for temporary password
    public void setPassword(String password) {
        this.password = password;
    }

    // Setter for expiration time
    public void setExpiry(LocalDateTime expiry) {
        this.expiry = expiry;
    }
}
