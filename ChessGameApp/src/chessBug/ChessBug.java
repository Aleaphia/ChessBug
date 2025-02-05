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

import chessBug.game.GameController;
import chessBug.network.Client;
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
        //Fake login TODO- make login page the first page pulled up
        try{
            // Connect to database
            client = new Client("user", "p@ssw0rd!"); // (example user)
        } catch (Exception e){}
        //Main pane
        HBox mainPane = new HBox();
        mainPane.getStyleClass().add("background");
        
        //Create Menu
        /*MenuBar menuBar = new MenuBar();
        mainPane.getChildren().addAll(menuBar, page);
        // TODO: Create menuBar options
        String[] menus = {"Home", "Games" , "Settings" , "Profile"};
        String[][] menuOptions = {
            {"Dash Board"}, // Home
            {"New Game", "Load Game", "DemoGame"}, // Games
            {"Preferences", "About"},  // Setting
            {"User Profile"} // Profile (added menu option)
        };
        fillMenuBar(menuBar, menus, menuOptions); //Creates dashboard based on above arrays*/
        
        mainPane.getChildren().addAll(createSidebar(), page);

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
        Button gamesButton = createGamesButton(); // Create Game button with dropdown menu
        Button settingsButton = createSidebarButton("Settings", event -> changePage("Preferences"));
        Button profileButton = createSidebarButton("Profile", event -> changePage("User Profile"));
    
        // Create VBox for game button and dropdown menu
        VBox gameButtonContainer = new VBox();
        gameButtonContainer.setSpacing(0);  // Remove extra spacing
        gameButtonContainer.getChildren().addAll(gamesButton, createGameMenu()); // Add game button and menu
    
        // Add items to the sidebar
        sidebar.getChildren().addAll(logo, homeButton, gameButtonContainer, settingsButton, profileButton);
    
        return sidebar;
    }
    
    private Button createGamesButton() {
        Button gamesButton = createSidebarButton("Game", event -> {});  // Empty event, we'll handle hover for dropdown visibility
    
        // Game submenu (Initially hidden)
        VBox gameMenu = createGameMenu();
        gameMenu.setVisible(false);  // Hide the menu initially
    
        // Make sure gameMenu is interactable by mouse
        gameMenu.setMouseTransparent(false);  // Allow interaction with the dropdown
    
        // Scale transition to expand the button on hover
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), gamesButton);
        scaleTransition.setToX(1.2);  // Scale 1.2x horizontally (expand)
        scaleTransition.setToY(1.2);  // Scale 1.2x vertically (expand)
    
        gamesButton.setOnMouseEntered(e -> {
            gameMenu.setVisible(true);  // Show the dropdown when hovering over the "Game" button
            scaleTransition.play();  // Start the animation to expand the button
        });
    
        gamesButton.setOnMouseExited(e -> {
            scaleTransition.setToX(1);  // Reset the scale back to 1 (normal size)
            scaleTransition.setToY(1);  // Reset the scale back to 1 (normal size)
            scaleTransition.play();  // Start the animation to shrink the button back
            hideMenu(gameMenu);  // Hide the dropdown when the mouse exits
        });
    
        // Show game menu when hovering over the "Game" button
        gamesButton.setOnMouseEntered(e -> gameMenu.setVisible(true));
        gamesButton.setOnMouseExited(e -> hideMenu(gameMenu));  // Hide menu when mouse leaves
    
        // Keep the menu visible when hovering over the game menu itself
        gameMenu.setOnMouseEntered(e -> gameMenu.setVisible(true));
        gameMenu.setOnMouseExited(e -> hideMenu(gameMenu));  // Hide when leaving both button and menu
    
        return gamesButton; // Return just the game button
    }
    
    private VBox createGameMenu() {
        VBox gameMenu = new VBox(10);
        gameMenu.setStyle("-fx-background-color: #3a3f47; -fx-padding: 10px; -fx-border-radius: 5px;");
    
        Button newGameButton = createSidebarButton("New Game", event -> changePage("New Game"));
        Button loadGameButton = createSidebarButton("Load Game", event -> changePage("Load Game"));
        Button demoGameButton = createSidebarButton("Demo Game", event -> changePage("Demo Game"));
    
        gameMenu.getChildren().addAll(newGameButton, loadGameButton, demoGameButton);
    
        // Set consistent width for the dropdown menu (same as sidebar buttons)
        gameMenu.setMaxWidth(200);  // Ensure the width is the same as the other buttons
    
        return gameMenu;
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
        return button;
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
            case "New Game":
                page.getChildren().add(new GameController(client).getPage()); // Load New Game Page
                break;
            case "Load Game":
                page.getChildren().add(new GameController(client).getPage()); // Load Game Page
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
            default:
                page.getChildren().add(new Label("Welcome to ChessBug!"));
        }
    }
   
    
    public static void main(String[] args) {
        Application.launch(args);
    }
}
