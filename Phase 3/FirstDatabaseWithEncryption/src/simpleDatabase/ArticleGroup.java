package simpleDatabase;

// Class to manage a group of articles with access control for different user roles.
import java.util.HashSet;
import java.util.Set;

public class ArticleGroup {
    // Group details and role-based access management
    private String groupName; 
    private Set<String> articleIds; 
    private Set<String> instructorAdmins; 
    private Set<String> instructorViewers; 
    private Set<String> studentViewers; 
    private boolean specialAccess; // Flag to indicate special access control

    // Constructor to initialize the group with a name and special access flag
    public ArticleGroup(String groupName, boolean specialAccess) {
        this.groupName = groupName;
        this.specialAccess = specialAccess;
        this.articleIds = new HashSet<>();
        this.instructorAdmins = new HashSet<>();
        this.instructorViewers = new HashSet<>();
        this.studentViewers = new HashSet<>();
    }

    // Getter for group name
    public String getGroupName() {
        return groupName;
    }

    // Add or remove articles from the group
    public void addArticle(String articleId) {
        articleIds.add(articleId);
    }

    public void removeArticle(String articleId) {
        articleIds.remove(articleId);
    }

    public Set<String> getArticleIds() {
        return articleIds; // Get all article IDs
    }

    // Manage instructor admins
    public void addInstructorAdmin(String username) {
        instructorAdmins.add(username);
    }

    public void removeInstructorAdmin(String username) {
        instructorAdmins.remove(username);
    }

    public Set<String> getInstructorAdmins() {
        return instructorAdmins; // Get all instructor admins
    }

    // Manage instructor viewers
    public void addInstructorViewer(String username) {
        instructorViewers.add(username);
    }

    public void removeInstructorViewer(String username) {
        instructorViewers.remove(username);
    }

    public Set<String> getInstructorViewers() {
        return instructorViewers; // Get all instructor viewers
    }

    // Manage student viewers
    public void addStudentViewer(String username) {
        studentViewers.add(username);
    }

    public void removeStudentViewer(String username) {
        studentViewers.remove(username);
    }

    public Set<String> getStudentViewers() {
        return studentViewers; // Get all student viewers
    }

    // Get and set the special access flag
    public boolean isSpecialAccess() {
        return specialAccess;
    }

    public void setSpecialAccess(boolean specialAccess) {
        this.specialAccess = specialAccess;
    }
}
