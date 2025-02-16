/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package chessBug;
/*

//Loop to recheck the database for updates
Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), (ActionEvent event) -> {
            //Add repeated database checks here ================================
            updateMsgBoard();
            
            // =================================================================
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
*/

import org.json.JSONObject;

import chessBug.game.GameController;
import chessBug.home.HomeController;
import chessBug.network.Client;
import chessBug.network.ClientAuthException;
import chessBug.preferences.PreferencesController;
import chessBug.profile.ProfileController;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
/**
 *
 * @author shosh
 */
public class ChessBug extends Application {
    //Global variables
    Scene mainScene;
    Pane page = new VBox(); // space to change with page details
    HBox mainPane;
    Client client;

    LoginUI loginUI;
    
    @Override
    public void start(Stage primaryStage) {
    //Create stage layout ======================================================
        //Main pane
        mainPane = new HBox();
        mainPane.getStyleClass().add("background");

        loginUI = new LoginUI(
            (String username, String password) -> { // Handle login
                JSONObject out = new JSONObject();
                try {
                    client = new Client(username, password);
                    out.put("error", false);
                    //Create Menu
                    mainPane.getChildren().clear();
                    mainPane.getChildren().addAll(createSidebar(), page);
                } catch (ClientAuthException e) {
                    e.printStackTrace();
                    out.put("error", true);
                    out.put("response", e.getServerResponse());
                }
                return out;
            },
            (String username, String password) -> { // Handle account creation
                JSONObject out = new JSONObject();
                try {
                    client = Client.createAccount(username, password, "placeholder@email.com");
                    out.put("error", false);
                    //Create Menu
                    mainPane.getChildren().clear();
                    mainPane.getChildren().addAll(createSidebar(), page);
                } catch(ClientAuthException e){
                    e.printStackTrace();
                    out.put("error", true);
                    out.put("response", e.getServerResponse());
                }

                return out;
            }
        );

        mainPane.getChildren().add(loginUI.getPage());

        //Scene and Stage ------------------------------------------------------
        primaryStage.setTitle("ChessBug"); //Name for application stage
        mainScene = new Scene(mainPane, 800, 600); //Add mainPane to the mainScene
        primaryStage.setScene(mainScene);//Add mainScene to primaryStage
        
        //Style
        mainScene.getStylesheets().add("Styles.css");
       
        //Display
        primaryStage.show();
        //-----------------------------------------------------------------------

        //======================================================================
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10); // Vertical layout for sidebar
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setStyle("-fx-background-color: #2f3136; -fx-text-fill: white;");
    
        // Add logo or image to the sidebar
        ImageView logo = new ImageView(new Image("file:logo.png")); // Will need to be replaced
        logo.setFitHeight(50);
        logo.setFitWidth(50);
    
        // Sidebar buttons 
        Button homeButton = createSidebarButton("Home", event -> changePage("Dash Board!"));
        Button gamesButton = createSidebarButton("Games", event -> changePage("Games"));
        Button settingsButton = createSidebarButton("Settings", event -> changePage("Preferences"));
        Button profileButton = createSidebarButton("Profile", event -> changePage("User Profile"));
        Button logoutButton = createSidebarButton("Log Out", event -> changePage("Log Out"));
    
        // Add items to the sidebar
        sidebar.getChildren().addAll(logo, homeButton, gamesButton, settingsButton, profileButton, logoutButton);
    
        return sidebar;
    }
    
    private void hideMenu(VBox gameMenu) {
        // Hide the dropdown if the mouse is not hovering over the button or the menu
        if (!gameMenu.isHover()) {
            gameMenu.setVisible(false);
        }
    }

    private Button createSidebarButton(String text, EventHandler event) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;");
        button.setPrefWidth(200);
        button.setOnAction(event);
        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-background-color: #444750; -fx-text-fill: white; -fx-font-size: 16px;");
        });
        button.setOnMouseExited(e -> {
            button.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px;");
        });
    
        return button;
    }
/*
    private VBox createHomePage() {
        VBox homePage = new VBox(20); // Vertical layout with spacing between sections
        homePage.setPadding(new Insets(20, 20, 20, 20));
        homePage.setStyle("-fx-background-color: #36393f; -fx-text-fill: white;");  // Set text color to white for the entire homePage
    
        // Welcome message
        Label welcomeMessage = new Label("Welcome to ChessBug!");
        welcomeMessage.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;"); // Ensure text is white
        
        // User info (this could be dynamic)
        Label userInfo = new Label("User: ChessMaster123");
        userInfo.setStyle("-fx-font-size: 18px; -fx-text-fill: white;"); // Ensure text is white
    
        // Add a separator line
        homePage.getChildren().addAll(welcomeMessage, userInfo, new Separator());
    
        // Recent game statistics
        VBox statsSection = new VBox(10);
        statsSection.setStyle("-fx-background-color: #2a2d34; -fx-padding: 10px; -fx-border-radius: 8px; -fx-text-fill: white;");
        Label statsTitle = new Label("Recent Game Statistics");
        statsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label gamesPlayed = new Label("Games Played: 15");
        gamesPlayed.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");  // Ensure the text is white
        Label wins = new Label("Wins: 10");
        wins.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
        Label losses = new Label("Losses: 5");
        losses.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
        
        statsSection.getChildren().addAll(statsTitle, gamesPlayed, wins, losses);
        homePage.getChildren().add(statsSection);
        
        // Recent activity feed (activity log)
        VBox activityFeed = new VBox(10);
        activityFeed.setStyle("-fx-background-color: #2a2d34; -fx-padding: 10px; -fx-border-radius: 8px; -fx-text-fill: white;");
        Label activityFeedTitle = new Label("Recent Activity");
        activityFeedTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label activity1 = new Label("You played a match with User456.");
        activity1.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
        Label activity2 = new Label("You won against User789.");
        activity2.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
        Label activity3 = new Label("You started a new challenge with User321.");
        activity3.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
        
        activityFeed.getChildren().addAll(activityFeedTitle, activity1, activity2, activity3);
        homePage.getChildren().add(activityFeed);
        
        // Featured or Live Game section (Optional)
        VBox liveGameSection = new VBox(10);
        liveGameSection.setStyle("-fx-background-color: #2a2d34; -fx-padding: 10px; -fx-border-radius: 8px; -fx-text-fill: white;");
        Label liveGameTitle = new Label("Featured Game");
        liveGameTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label liveGame = new Label("Live Game: ChessMaster123 vs. User456");
        liveGame.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
        Button joinGameButton = new Button("Join Game");
        joinGameButton.setStyle("-fx-background-color: #4e8af3; -fx-text-fill: white;");
        
        liveGameSection.getChildren().addAll(liveGameTitle, liveGame, joinGameButton);
        homePage.getChildren().add(liveGameSection);
        
        // Add a final separator for design clarity
        homePage.getChildren().add(new Separator());
        
        return homePage;
    }
*/
    
    private void changePage(String newPage) {
        page.getChildren().clear(); // Clear the current page content

        // Change the content based on the menu item clicked
        switch (newPage) {
            case "Games":
                page.getChildren().add(new GameController(client).getPage()); // Load New Game Page
                break;
            case "Preferences":
                page.getChildren().add(new PreferencesController().getPage()); // Load Preferences Page
                break;
            case "User Profile":
                page.getChildren().add(new ProfileController(client).getPage()); // Load User Profile Page
                break;
            case "Dash Board!":  // Home page
                page.getChildren().add(new HomeController(client).getPage());
                break;
            case "Log Out":
                mainPane.getChildren().clear();
                mainPane.getChildren().add(loginUI.getPage());
                break;
            default:
                page.getChildren().add(new Label("Welcome to ChessBug!"));
        }
    }
   
    
    public static void main(String[] args) {
        Application.launch(args);
    }
}
