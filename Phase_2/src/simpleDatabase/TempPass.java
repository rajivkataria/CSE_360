package simpleDatabase;

import java.io.Serializable;
import java.time.LocalDateTime;

//The TempPass represents a temporary password for account recovery with an expiration time
public class TempPass implements Serializable {
    private static final long serialVersionUID = 1L;

    private String password; // Temp password
    private LocalDateTime expiry; // Expiry time

    // Constructor to initialize TempPass with password and the expiration time
    public TempPass(String password, LocalDateTime expiry) {
        this.password = password;
        this.expiry = expiry;
    }

    // Gets the temporary password
    public String getPassword() {
        return password;
    }

    // Gets the expiry time
    public LocalDateTime getExpiry() {
        return expiry;
    }
}
