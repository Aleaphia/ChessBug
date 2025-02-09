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
    Pane page = new VBox(); // space to change with page details
    Client client;
    
    @Override
    public void start(Stage primaryStage) {
    //Create stage layout ======================================================
        //Main pane
        HBox mainPane = new HBox();
        mainPane.getStyleClass().add("background");

        LoginUI loginUI = new LoginUI(
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
        Scene mainScene = new Scene(mainPane, 800, 600); //Add mainPane to the mainScene
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
    
//        // Create VBox for game button and dropdown menu
//        VBox gameButtonContainer = new VBox();
//        gameButtonContainer.setSpacing(0);  // Remove extra spacing
//        gameButtonContainer.getChildren().addAll(gamesButton); // Add game button and menu
    
        // Add items to the sidebar
        sidebar.getChildren().addAll(logo, homeButton, gamesButton, settingsButton, profileButton);
    
        return sidebar;
    }
    
//    private Button createGamesButton() {
//        Button gamesButton = createSidebarButton("Game", event -> {});  // Empty event, we'll handle hover for dropdown visibility
//    
//        // Game submenu (Initially hidden)
//        VBox gameMenu = createGameMenu();
//        gameMenu.setVisible(false);  // Hide the menu initially
//    
//        // Make sure gameMenu is interactable by mouse
//        gameMenu.setMouseTransparent(false);  // Allow interaction with the dropdown
//    
//        // Scale transition to expand the button on hover
//        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), gamesButton);
//        scaleTransition.setToX(1.2);  // Scale 1.2x horizontally (expand)
//        scaleTransition.setToY(1.2);  // Scale 1.2x vertically (expand)
//    
//        gamesButton.setOnMouseEntered(e -> {
//            gameMenu.setVisible(true);  // Show the dropdown when hovering over the "Game" button
//            scaleTransition.play();  // Start the animation to expand the button
//        });
//    
//        gamesButton.setOnMouseExited(e -> {
//            scaleTransition.setToX(1);  // Reset the scale back to 1 (normal size)
//            scaleTransition.setToY(1);  // Reset the scale back to 1 (normal size)
//            scaleTransition.play();  // Start the animation to shrink the button back
//            hideMenu(gameMenu);  // Hide the dropdown when the mouse exits
//        });
//    
//        // Show game menu when hovering over the "Game" button
//        gamesButton.setOnMouseEntered(e -> gameMenu.setVisible(true));
//        gamesButton.setOnMouseExited(e -> hideMenu(gameMenu));  // Hide menu when mouse leaves
//    
//        // Keep the menu visible when hovering over the game menu itself
//        gameMenu.setOnMouseEntered(e -> gameMenu.setVisible(true));
//        gameMenu.setOnMouseExited(e -> hideMenu(gameMenu));  // Hide when leaving both button and menu
//    
//        return gamesButton; // Return just the game button
//    }
//    
//    private VBox createGameMenu() {
//        VBox gameMenu = new VBox(10);
//        gameMenu.setStyle("-fx-background-color: #3a3f47; -fx-padding: 10px; -fx-border-radius: 5px;");
//    
//        Button newGameButton = createSidebarButton("New Game", event -> changePage("New Game"));
//        Button loadGameButton = createSidebarButton("Load Game", event -> changePage("Load Game"));
//        Button demoGameButton = createSidebarButton("Demo Game", event -> changePage("Demo Game"));
//    
//        gameMenu.getChildren().addAll(newGameButton, loadGameButton, demoGameButton);
//    
//        // Set consistent width for the dropdown menu (same as sidebar buttons)
//        gameMenu.setMaxWidth(200);  // Ensure the width is the same as the other buttons
//    
//        return gameMenu;
//    }
    
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
    
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        
        // Main menu options
        String[] menus = {"Home", "Games", "Settings", "Profile"};
        String[][] menuOptions = {
            {"Dash Board"}, // Home
            {"New Game", "Load Game", "DemoGame"}, // Games
            {"Preferences", "About"},  // Settings
            {"User Profile"} // Profile
        };
        
        for (int i = 0; i < menus.length; i++) {
            Menu menu = new Menu(menus[i]);
            menuBar.getMenus().add(menu);
            
            for (int j = 0; j < menuOptions[i].length; j++) {
                MenuItem menuItem = new MenuItem(menuOptions[i][j]);
                menu.getItems().add(menuItem);
                menuItem.setOnAction(event -> changePage(menuItem.getText()));
            }
        }
        return menuBar;
    }
    
    private void fillMenuBar(MenuBar menuBar, String[] menus, String[][] menuOptions){
        //Add each menu to the MenuBar
        for (int i = 0; i < menus.length; i++){
            Menu menu = new Menu(menus[i]); //Create menu
            menuBar.getMenus().add(menu); //Add to container
            
            //Add each menu option to the menu
            for (int j = 0; j < menuOptions[i].length; j++){
                MenuItem menuItem = new MenuItem(menuOptions[i][j]); //Create menuItem
                menu.getItems().add(menuItem); //Add to container
                menu.setOnAction(event -> changePage(((MenuItem)event.getTarget()).getText()));
            }            
        }
    }
    
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
            case "About":
                page.getChildren().add(new Label("ChessBug - About Page"));
                break;
            case "User Profile":
                page.getChildren().add(new ProfileController(client).getPage()); // Load User Profile Page
                break;
            case "Dash Board!":  // Home page
                page.getChildren().add(createHomePage());
                break;   
            default:
                page.getChildren().add(new Label("Welcome to ChessBug!"));
        }
    }
   
    
    public static void main(String[] args) {
        Application.launch(args);
    }
}
