package simpleDatabase;

// Class representing a help message with optional response
public class HelpMessage {
    // Message attributes
    private String senderUsername; 
    private String topic; 
    private String message; 
    private String response; // Optional response to the message

    // Constructor to initialize a help message
    public HelpMessage(String senderUsername, String topic, String message) {
        this.senderUsername = senderUsername;
        this.topic = topic;
        this.message = message;
        this.response = null; // Default to no response
    }

    // Getters for message properties
    public String getSenderUsername() {
        return senderUsername; // Get sender's username
    }

    public String getTopic() {
        return topic; // Get message topic
    }

    public String getMessage() {
        return message; 
    }

    public String getResponse() {
        return response; 
    }

    // Setter to update the response
    public void setResponse(String response) {
        this.response = response;
    }
}
