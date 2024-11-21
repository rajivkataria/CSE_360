package simpleDatabase;

// Class to represent a help article with metadata and content management.
public class HelpArticle {
    // Article properties
    private String id; // Unique identifier for the article
    private String title;
    private String authors; 
    private String abstractText; // Abstract or summary of the article
    private String level; // Difficulty or target level of the article
    private String body; // Full content of the article
    private boolean encrypted; // Flag to indicate if the article content is encrypted
    private String groupName; 

    // Constructor to initialize article with default group and encryption status
    public HelpArticle(String id, String title, String authors, String abstractText, String level, String body) {
        this.id = id;
        this.title = title;
        this.authors = authors;
        this.abstractText = abstractText;
        this.level = level;
        this.body = body;
        this.groupName = "General"; // Default group name
        this.encrypted = false; // Default encryption status
    }

    // Getters for article properties
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthors() {
        return authors;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public String getLevel() {
        return level;
    }

    public String getBody() {
        return body;
    }

    // Setter for article body
    public void setBody(String body) {
        this.body = body;
    }

    // Get and set the encryption status
    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    // Get and set the group name
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
