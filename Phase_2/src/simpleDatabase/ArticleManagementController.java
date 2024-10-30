package simpleDatabase;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import Encryption.EncryptionHelper;
import Encryption.EncryptionUtils;

import java.io.*;
import java.util.*;

/**
 * Class for article management functionalities.
 * Handles creating, editing, backing up, and restoring articles.
 */
public class ArticleManagementController {

    // Simulated article database storing articles by ID 
    protected static Map<Long, CreateArticle> articleDB = new HashMap<>(); 

    // Counter for the next available article ID to ensure unique article IDs
    protected static long nextArticleId = 1;

    // Helper for encryption and decryption operations during backup and restore
    protected EncryptionHelper encryptionHelper;

    /**
     * Sets the encryption helper to be used for secure data handling.
     */
    public void setEncryptionHelper(EncryptionHelper helper) {
        this.encryptionHelper = helper;
    }

    /**
     * Generates a unique article ID by incrementing the counter.
     */
    protected long generateArticleId() {
        return nextArticleId++;
    }

    /**
     * Opens the Create Article screen, allowing users to enter details for a new article.
     * Users can specify article level, title, description, keywords, body, and links.
     */
    public void openCreateArticleScreen(Stage window, UserAccount user) {
        // Layout setup for the article creation form
        GridPane createGrid = new GridPane();
        createGrid.setPadding(new Insets(10));
        createGrid.setVgap(8);
        createGrid.setHgap(10);

        // UI components for entering article details
        Label levelLbl = new Label("Level:");
        ComboBox<String> levelCombo = new ComboBox<>();
        levelCombo.getItems().addAll("Beginner", "Intermediate", "Advanced", "Expert");
        // Default level selection
        levelCombo.setValue("Intermediate");

        Label groupsLbl = new Label("Groups (comma-separated):");
        TextField groupsTxt = new TextField();

        Label titleLbl = new Label("Title:");
        TextField titleTxt = new TextField();

        Label descLbl = new Label("Short Description:");
        TextField descTxt = new TextField();

        Label keywordsLbl = new Label("Keywords (comma-separated):");
        TextField keywordsTxt = new TextField();

        Label bodyLbl = new Label("Body:");
        TextArea bodyTxt = new TextArea();

        Label linksLbl = new Label("Links (comma-separated):");
        TextField linksTxt = new TextField();

        Button saveBtn = new Button("Save Article");
        Button backBtn = new Button("Back");
        Label statusLbl = new Label();

        // Add components to the layout grid
        createGrid.add(levelLbl, 0, 0);
        createGrid.add(levelCombo, 1, 0);
        createGrid.add(groupsLbl, 0, 1);
        createGrid.add(groupsTxt, 1, 1);
        createGrid.add(titleLbl, 0, 2);
        createGrid.add(titleTxt, 1, 2);
        createGrid.add(descLbl, 0, 3);
        createGrid.add(descTxt, 1, 3);
        createGrid.add(keywordsLbl, 0, 4);
        createGrid.add(keywordsTxt, 1, 4);
        createGrid.add(bodyLbl, 0, 5);
        createGrid.add(bodyTxt, 1, 5);
        createGrid.add(linksLbl, 0, 6);
        createGrid.add(linksTxt, 1, 6);
        createGrid.add(saveBtn, 1, 7);
        createGrid.add(backBtn, 0, 7);
        createGrid.add(statusLbl, 1, 8);

        // Set window title
        window.setTitle("Create Help Article");

        // Save button action lets user save the article if title and body fields are filled
        saveBtn.setOnAction(e -> {
            String level = levelCombo.getValue();
            Set<String> groups = new HashSet<>(Arrays.asList(groupsTxt.getText().split(",")));
            String title = titleTxt.getText();
            String shortDesc = descTxt.getText();
            Set<String> keywords = new HashSet<>(Arrays.asList(keywordsTxt.getText().split(",")));
            String body = bodyTxt.getText();
            List<String> links = Arrays.asList(linksTxt.getText().split(","));

            // Validate required fields (title and body)
            if (title.isEmpty() || body.isEmpty()) {
                statusLbl.setText("Title and Body are required.");
                return;
            }

            // Generate a unique ID for the article and save it to the database
            long id = generateArticleId();
            CreateArticle article = new CreateArticle(id, level, groups, title, shortDesc, keywords, body, links);
            articleDB.put(id, article);

            // Display confirmation message with the article ID
            statusLbl.setText("Article saved with ID: " + id);
        });

        // Back button action to return to home screen based on user role
        backBtn.setOnAction(e -> {
            UserManagementController userController = new UserManagementController();
            userController.setEncryptionHelper(encryptionHelper);

            // Display the appropriate home screen based on user role
            if (user.getRoles().contains(UserManagementController.ROLE_ADMINISTRATOR)) {
                userController.displayHomeScreen(window, user, UserManagementController.ROLE_ADMINISTRATOR);
            } else {
                userController.displayHomeScreen(window, user, UserManagementController.ROLE_TEACHER);
            }
        });

        // Set up and display the create article screen
        Scene createScene = new Scene(createGrid, 600, 600);
        window.setScene(createScene);
    }

    /**
     * Opens the Manage Articles screen, displaying all existing articles with options to
     * view, edit, or delete each article.
     */
    public void openManageArticlesScreen(Stage window, UserAccount user) {
        VBox manageLayout = new VBox(10);
        manageLayout.setPadding(new Insets(10));

        // Display the title for the article management screen
        Label titleLbl = new Label("Help Articles:");
        manageLayout.getChildren().add(titleLbl);

        // List all articles with options to view, edit, or delete
        for (CreateArticle article : articleDB.values()) {
            Label articleLbl = new Label("ID: " + article.getId() + ", Title: " + article.getTitle());
            Button viewBtn = new Button("View");
            Button editBtn = new Button("Edit");
            Button deleteBtn = new Button("Delete");

            HBox articleBox = new HBox(5, articleLbl, viewBtn, editBtn, deleteBtn);
            manageLayout.getChildren().add(articleBox);

            // View, Edit, and Delete button actions
            viewBtn.setOnAction(e -> openViewArticleScreen(window, user, article));
            editBtn.setOnAction(e -> openEditArticleScreen(window, user, article));
            deleteBtn.setOnAction(e -> {
                // Confirm deletion of the article
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Article");
                alert.setHeaderText("Delete article: " + article.getTitle() + "?");
                alert.setContentText("This cannot be undone.");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    articleDB.remove(article.getId());
                    manageLayout.getChildren().remove(articleBox); // Remove article from the UI
                }
            });
        }

        // Back button to return to the home screen
        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> {
            UserManagementController userController = new UserManagementController();
            userController.setEncryptionHelper(encryptionHelper);

            // Display the appropriate home screen based on user role
            if (user.getRoles().contains(UserManagementController.ROLE_ADMINISTRATOR)) {
                userController.displayHomeScreen(window, user, UserManagementController.ROLE_ADMINISTRATOR);
            } else {
                userController.displayHomeScreen(window, user, UserManagementController.ROLE_TEACHER);
            }
        });
        manageLayout.getChildren().add(backBtn);

        // Set up and display the manage articles screen
        window.setTitle("Manage Help Articles");
        Scene manageScene = new Scene(manageLayout, 600, 600);
        window.setScene(manageScene);
    }


    /**
     * Opens the View Article screen, displaying all details of a selected article.
     */
    public void openViewArticleScreen(Stage window, UserAccount user, CreateArticle article) {
        VBox viewLayout = new VBox(10);
        viewLayout.setPadding(new Insets(10));

        // Display article details
        Label titleLbl = new Label("Title: " + article.getTitle());
        Label levelLbl = new Label("Level: " + article.getLevel());
        Label groupsLbl = new Label("Groups: " + article.getGroups());
        Label descLbl = new Label("Short Description: " + article.getShortDescription());
        Label keywordsLbl = new Label("Keywords: " + article.getKeywords());
        Label bodyLbl = new Label("Body:");
        TextArea bodyTxt = new TextArea(article.getBody());
        bodyTxt.setEditable(false); // Read-only mode for body text
        Label linksLbl = new Label("Links: " + article.getLinks());

        // Back button to return to the appropriate screen based on user role
        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> {
            if (user.getRoles().contains(UserManagementController.ROLE_ADMINISTRATOR) || 
                user.getRoles().contains(UserManagementController.ROLE_TEACHER)) {
                openManageArticlesScreen(window, user);
            } else {
                openSearchArticlesScreen(window, user);
            }
        });

        // Add components to the layout and set the scene
        viewLayout.getChildren().addAll(titleLbl, levelLbl, groupsLbl, descLbl, keywordsLbl, bodyLbl, bodyTxt, linksLbl, backBtn);
        window.setTitle("View Help Article");
        Scene viewScene = new Scene(viewLayout, 600, 600);
        window.setScene(viewScene);
    }

    /*
     * Opens the Edit Article screen, allowing the user to modify and save changes to the article.
     */
    public void openEditArticleScreen(Stage window, UserAccount user, CreateArticle article) {
        GridPane editGrid = new GridPane();
        editGrid.setPadding(new Insets(10));
        editGrid.setVgap(8);
        editGrid.setHgap(10);

        // Components for editing article fields
        Label levelLbl = new Label("Level:");
        ComboBox<String> levelCombo = new ComboBox<>();
        levelCombo.getItems().addAll("Beginner", "Intermediate", "Advanced", "Expert");
        levelCombo.setValue(article.getLevel());

        Label groupsLbl = new Label("Groups (comma-separated):");
        TextField groupsTxt = new TextField(String.join(",", article.getGroups()));

        Label titleLbl = new Label("Title:");
        TextField titleTxt = new TextField(article.getTitle());

        Label descLbl = new Label("Short Description:");
        TextField descTxt = new TextField(article.getShortDescription());

        Label keywordsLbl = new Label("Keywords (comma-separated):");
        TextField keywordsTxt = new TextField(String.join(",", article.getKeywords()));

        Label bodyLbl = new Label("Body:");
        TextArea bodyTxt = new TextArea(article.getBody());

        Label linksLbl = new Label("Links (comma-separated):");
        TextField linksTxt = new TextField(String.join(",", article.getLinks()));

        Button saveBtn = new Button("Save Changes");
        Button backBtn = new Button("Back");
        Label statusLbl = new Label();

        // Add components to the grid layout
        editGrid.add(levelLbl, 0, 0);
        editGrid.add(levelCombo, 1, 0);
        editGrid.add(groupsLbl, 0, 1);
        editGrid.add(groupsTxt, 1, 1);
        editGrid.add(titleLbl, 0, 2);
        editGrid.add(titleTxt, 1, 2);
        editGrid.add(descLbl, 0, 3);
        editGrid.add(descTxt, 1, 3);
        editGrid.add(keywordsLbl, 0, 4);
        editGrid.add(keywordsTxt, 1, 4);
        editGrid.add(bodyLbl, 0, 5);
        editGrid.add(bodyTxt, 1, 5);
        editGrid.add(linksLbl, 0, 6);
        editGrid.add(linksTxt, 1, 6);
        editGrid.add(saveBtn, 1, 7);
        editGrid.add(backBtn, 0, 7);
        editGrid.add(statusLbl, 1, 8);

        // Save button action to update the article with edited values
        saveBtn.setOnAction(e -> {
            String level = levelCombo.getValue();
            Set<String> groups = new HashSet<>(Arrays.asList(groupsTxt.getText().split(",")));
            String title = titleTxt.getText();
            String shortDesc = descTxt.getText();
            Set<String> keywords = new HashSet<>(Arrays.asList(keywordsTxt.getText().split(",")));
            String body = bodyTxt.getText();
            List<String> links = Arrays.asList(linksTxt.getText().split(","));

            // Validate required fields
            if (title.isEmpty() || body.isEmpty()) {
                statusLbl.setText("Title and Body are required.");
                return;
            }

            // Update article attributes and display confirmation
            article.setLevel(level);
            article.setGroups(groups);
            article.setTitle(title);
            article.setShortDescription(shortDesc);
            article.setKeywords(keywords);
            article.setBody(body);
            article.setLinks(links);

            statusLbl.setText("Article updated.");
        });

        // Back button to return to the manage articles screen
        backBtn.setOnAction(e -> openManageArticlesScreen(window, user));

        // Set up and display the edit article screen
        Scene editScene = new Scene(editGrid, 600, 600);
        window.setScene(editScene);
    }

    /*
     * Opens the Backup Articles screen, where the user can specify groups and a filename for backing up articles.
     */
    public void openBackupArticlesScreen(Stage window, UserAccount user) {
        GridPane backupGrid = new GridPane();
        backupGrid.setPadding(new Insets(10));
        backupGrid.setVgap(8);
        backupGrid.setHgap(10);

        // UI components for specifying groups and filename for backup
        Label groupsLbl = new Label("Groups to backup (comma-separated):");
        TextField groupsTxt = new TextField();

        Label filenameLbl = new Label("Backup Filename:");
        TextField filenameTxt = new TextField();

        Button backupBtn = new Button("Backup");
        Button backBtn = new Button("Back");
        Label statusLbl = new Label();

        // Add components to the layout
        backupGrid.add(groupsLbl, 0, 0);
        backupGrid.add(groupsTxt, 1, 0);
        backupGrid.add(filenameLbl, 0, 1);
        backupGrid.add(filenameTxt, 1, 1);
        backupGrid.add(backupBtn, 1, 2);
        backupGrid.add(backBtn, 0, 2);
        backupGrid.add(statusLbl, 1, 3);

        // Set window title
        window.setTitle("Backup Articles");

        // Backup button action to initiate article backup based on groups and filename
        backupBtn.setOnAction(e -> {
            String filename = filenameTxt.getText();
            
            // Validate the filename
            if (filename.isEmpty()) {
                statusLbl.setText("Filename is required.");
                return;
            }
            
            // Parse groups and attempt to back up the articles
            Set<String> groups = new HashSet<>(Arrays.asList(groupsTxt.getText().split(",")));
            try {
                backupArticles(filename, groups);
                statusLbl.setText("Backup successful.");
            } catch (Exception ex) {
                ex.printStackTrace();
                statusLbl.setText("Backup failed.");
            }
        });

        // Back button action to return to home screen based on user role
        backBtn.setOnAction(e -> {
            UserManagementController userController = new UserManagementController();
            userController.setEncryptionHelper(encryptionHelper);
            
            // Display the appropriate home screen based on the user's role
            if (user.getRoles().contains(UserManagementController.ROLE_ADMINISTRATOR)) {
                userController.displayHomeScreen(window, user, UserManagementController.ROLE_ADMINISTRATOR);
            } else {
                userController.displayHomeScreen(window, user, UserManagementController.ROLE_TEACHER);
            }
        });

        // Set up and display the backup articles screen
        Scene backupScene = new Scene(backupGrid, 500, 300);
        window.setScene(backupScene);
    }

    /**
     * Backs up articles to a specified file. If groups are provided, only articles belonging to 
     * those groups are backed up. Otherwise, all articles are included.
     */
    protected void backupArticles(String filename, Set<String> groups) throws Exception {
        List<CreateArticle> articlesToBackup = new ArrayList<>();
        
        // Add articles to backup list based on group filtering
        if (groups.isEmpty() || groups.contains("")) {
        	// Backup all if no group specified
            articlesToBackup.addAll(articleDB.values());
        } else {
            for (CreateArticle article : articleDB.values()) {
                for (String group : article.getGroups()) {
                    if (groups.contains(group.trim())) {
                        articlesToBackup.add(article);
                        break;
                    }
                }
            }
        }

        // Serialize articles, encrypt data, and save to file
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(articlesToBackup);
        oos.close();

        byte[] plainData = baos.toByteArray();
        byte[] iv = EncryptionUtils.getInitializationVector(filename.toCharArray());
        byte[] encryptedData = encryptionHelper.encrypt(plainData, iv);

        // Write encrypted data to the specified backup file
        FileOutputStream fos = new FileOutputStream(filename);
        fos.write(encryptedData);
        fos.close();
    }


    /**
     * Opens the Restore Articles screen, which allows the user to select a backup file and specify whether
     * to merge the restored articles with or replace existing articles.
     */
    public void openRestoreArticlesScreen(Stage window, UserAccount user) {
        // Set up layout for the restore screen
        GridPane restoreGrid = new GridPane();
        restoreGrid.setPadding(new Insets(10));
        restoreGrid.setVgap(8);
        restoreGrid.setHgap(10);

        // UI components for file input and restore options
        Label filenameLbl = new Label("Backup Filename:");
        TextField filenameTxt = new TextField();

        Label optionLbl = new Label("Restore Option:");
        RadioButton removeBtn = new RadioButton("Remove existing articles");
        RadioButton mergeBtn = new RadioButton("Merge with existing articles");
        ToggleGroup optionGroup = new ToggleGroup();
        removeBtn.setToggleGroup(optionGroup);
        mergeBtn.setToggleGroup(optionGroup);
        
        // Default option is to merge with existing articles
        mergeBtn.setSelected(true);
        VBox optionBox = new VBox(5, removeBtn, mergeBtn);

        Button restoreBtn = new Button("Restore");
        Button backBtn = new Button("Back");
        Label statusLbl = new Label();

        // Add components to the layout grid
        restoreGrid.add(filenameLbl, 0, 0);
        restoreGrid.add(filenameTxt, 1, 0);
        restoreGrid.add(optionLbl, 0, 1);
        restoreGrid.add(optionBox, 1, 1);
        restoreGrid.add(restoreBtn, 1, 2);
        restoreGrid.add(backBtn, 0, 2);
        restoreGrid.add(statusLbl, 1, 3);

        // Set the window title for the restore screen
        window.setTitle("Restore Articles");

        // Restore button action lets user restore articles from the specified backup file
        restoreBtn.setOnAction(e -> {
            String filename = filenameTxt.getText();
            
            // Validating the filename field
            if (filename.isEmpty()) {
                statusLbl.setText("Filename is required.");
                return;
            }

            // Determine if existing articles should be removed before restore
            boolean removeExisting = removeBtn.isSelected();

            try {
                // try to restore articles from the backup file
                restoreArticles(filename, removeExisting);
                statusLbl.setText("Restore successful.");
                
                // Refresh the Manage Articles screen after restore
                openManageArticlesScreen(window, user);
            } catch (Exception ex) {
                ex.printStackTrace();
                statusLbl.setText("Restore failed.");
            }
        });

        // Back button action lets user return to home screen based on user role
        backBtn.setOnAction(e -> {
            UserManagementController userController = new UserManagementController();
            userController.setEncryptionHelper(encryptionHelper);
            
            // Display the appropriate home screen based on user role
            if (user.getRoles().contains(UserManagementController.ROLE_ADMINISTRATOR)) {
                userController.displayHomeScreen(window, user, UserManagementController.ROLE_ADMINISTRATOR);
            } else {
                userController.displayHomeScreen(window, user, UserManagementController.ROLE_TEACHER);
            }
        });

        // Set up and display the restore articles screen
        Scene restoreScene = new Scene(restoreGrid, 500, 300);
        window.setScene(restoreScene);
    }

    /**
     * Restores articles from an encrypted backup file, which allows the user to choose between
     * merging the restored articles with existing ones or replacing all existing articles.
     */
    protected void restoreArticles(String filename, boolean removeExisting) {
        try {
            // Read encrypted data from the specified file
            FileInputStream fis = new FileInputStream(filename);
            byte[] encryptedData = fis.readAllBytes();
            fis.close();

            // Decrypt the data using an initialization vector derived from the filename
            byte[] iv = EncryptionUtils.getInitializationVector(filename.toCharArray());
            byte[] plainData = encryptionHelper.decrypt(encryptedData, iv);

            // Deserialize the decrypted data into a list of articles
            ByteArrayInputStream bais = new ByteArrayInputStream(plainData);
            ObjectInputStream ois = new ObjectInputStream(bais);

            @SuppressWarnings("unchecked")
            List<CreateArticle> restoredArticles = (List<CreateArticle>) ois.readObject();
            ois.close();

            // If removeExisting is true, clear the current article db
            if (removeExisting) {
                articleDB.clear();
                nextArticleId = 1;
            }

            // Add each restored article to the database and update next ID
            for (CreateArticle article : restoredArticles) {
                if (article.getId() >= nextArticleId) {
                    nextArticleId = article.getId() + 1;
                }
                articleDB.put(article.getId(), article);
            }

            // Log the number of articles restored with their IDs
            System.out.println("Number of articles restored: " + restoredArticles.size());
            System.out.println("Article IDs restored: " + articleDB.keySet());

        } catch (Exception e) {
            e.printStackTrace();
            
            // Display an error alert to the user if restoration fails
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Restore Failed");
            alert.setHeaderText("An error occurred during restore.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    public void openSearchArticlesScreen(Stage window, UserAccount user) {
        // Set up layout for search functionality
        VBox searchLayout = new VBox(10);
        searchLayout.setPadding(new Insets(10));

        // Initialize UI components
        Label keywordLbl = new Label("Enter keywords to search:");
        TextField keywordTxt = new TextField();
        Button searchBtn = new Button("Search");
        ListView<String> resultList = new ListView<>();
        Button viewBtn = new Button("View Article");
        Button backBtn = new Button("Back");
        Label statusLbl = new Label();

        // Add UI components to the layout
        searchLayout.getChildren().addAll(keywordLbl, keywordTxt, searchBtn, resultList, viewBtn, backBtn, statusLbl);

        // Set the title of the window for the search screen
        window.setTitle("Search Help Articles");

        // Search button action: search articles based on keywords
        searchBtn.setOnAction(e -> {
            String keywords = keywordTxt.getText().toLowerCase();
            
            // Check if keywords are provided and, if not, prompt user to enter keywords
            if (keywords.isEmpty()) {
                statusLbl.setText("Enter keywords to search.");
                return;
            }
            
            // Clear previous search results from resultList
            resultList.getItems().clear();
            
            // Iterate through articles in the database and add matching articles to resultList
            for (CreateArticle article : articleDB.values()) {
                for (String keyword : article.getKeywords()) {
                    if (keyword.toLowerCase().contains(keywords)) {
                        resultList.getItems().add("ID: " + article.getId() + ", Title: " + article.getTitle());
                        break; // Exit loop after finding a matching keyword for this article
                    }
                }
            }

            // Update status label based on whether articles were found
            if (resultList.getItems().isEmpty()) {
                statusLbl.setText("No articles found.");
            } else {
                statusLbl.setText(resultList.getItems().size() + " article(s) found.");
            }
        });

        // View button action: display the selected article in a view window
        viewBtn.setOnAction(e -> {
            String selected = resultList.getSelectionModel().getSelectedItem();
            
            // Check if an article is selected; if not, prompt the user
            if (selected == null) {
                statusLbl.setText("Select an article to view.");
                return;
            }

            // Extract the article ID from the selected item text
            long id = Long.parseLong(selected.substring(4, selected.indexOf(",")));
            CreateArticle article = articleDB.get(id);

            // Open the view article screen for the selected article
            openViewArticleScreen(window, user, article);
        });

        // Back button action to return to the home screen based on user role
        backBtn.setOnAction(e -> {
            UserManagementController userController = new UserManagementController();
            userController.setEncryptionHelper(encryptionHelper);
            
            // Display the appropriate home screen based on the user's role
            userController.displayHomeScreen(window, user, UserManagementController.ROLE_LEARNER);
        });

        // Sets up and display the search articles screen
        Scene searchScene = new Scene(searchLayout, 600, 600);
        window.setScene(searchScene);
    }
}
