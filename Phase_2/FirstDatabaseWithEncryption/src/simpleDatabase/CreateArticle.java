package simpleDatabase;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * The CreateArTicle class helps create Article in the system, and provides essential fields for 
 * categorization, content, and metadata. This class implements Serializable 
 * to allow article instances to be backed up and then restored as objects.
 */
public class CreateArticle implements Serializable {
    private static final long serialVersionUID = 1L;

    // Unique ID
    private long id;
    
    // Article difficulty level (e.g., beginner, intermediate, advanced, expert)
    private String level;
    
    // Set of groups associated with the article
    private Set<String> groups;
    
    // Title of the article
    private String title;
    
    // Short description or summary of the article
    private String shortDescription;
    
    // Set of keywords or phrases associated with the article
    private Set<String> keywords;
    
    // Main content of the article
    private String body;
    
    // List of reference links or related articles
    private List<String> links;

    /**
     * Constructs a new CreateArticle with the specified attributes.
     */
    public CreateArticle(long id, String level, Set<String> groups, String title, String shortDescription,
                         Set<String> keywords, String body, List<String> links) {
        this.id = id;
        this.level = level;
        this.groups = groups;
        this.title = title;
        this.shortDescription = shortDescription;
        this.keywords = keywords;
        this.body = body;
        this.links = links;
    }

    // Getters and setters for accessing and updating article attributes

    /*
     * Gets the unique identifier for the article.
     */
    public long getId() {
        return id;
    }

    /*
     * Gets the difficulty level of the article.
     */
    public String getLevel() {
        return level;
    }

    /*
     * Gets the set of groups the article belongs to.
     */
    public Set<String> getGroups() {
        return groups;
    }

    /*
     * Gets the title of the article.
     */
    public String getTitle() {
        return title;
    }

    /*
     * Gets the short description of the article.
     */
    public String getShortDescription() {
        return shortDescription;
    }

    /*
     * Gets the set of keywords associated with the article.
     */
    public Set<String> getKeywords() {
        return keywords;
    }

    /*
     * Gets the main content of the article.
     */
    public String getBody() {
        return body;
    }

    /*
     * Gets the list of reference links or related articles.
     */
    public List<String> getLinks() {
        return links;
    }

    /*
     * Sets the difficulty level of the article and takes level the difficulty level to set
     */
    public void setLevel(String level) {
        this.level = level;
    }

    /*
     * Sets the groups associated with the article.
     */
    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }

    /*
     * Sets the title of the article.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /*
     * Sets the short description of the article.
     */
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    /*
     * Sets the keywords associated with the article.
     */
    public void setKeywords(Set<String> keywords) {
        this.keywords = keywords;
    }

    /*
     * Sets the main content of the article.
     */
    public void setBody(String body) {
        this.body = body;
    }

    /*
     * Sets the list of reference links or related articles.
     */
    public void setLinks(List<String> links) {
        this.links = links;
    }
}
