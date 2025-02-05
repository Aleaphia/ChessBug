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

import listHelper.SavableList;
import chessGame.*;
import chessBug.game.*;
import chessBug.network.Client;
import chessBug.preferences.PreferencesController;
import chessBug.profile.ProfileController;

import java.io.*;
import java.util.*;

import javafx.application.*;
import javafx.stage.Stage;
import javafx.geometry.*;

import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.event.Event;
import javafx.event.EventHandler;
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
        VBox mainPane = new VBox();
        mainPane.getStyleClass().add("background");
        
        //Create Menu
        MenuBar menuBar = new MenuBar();
        mainPane.getChildren().addAll(menuBar, page);
        // TODO: Create menuBar options
        String[] menus = {"Home", "Games" , "Settings" , "Profile"};
        String[][] menuOptions = {
            {"Dash Board"}, // Home
            {"New Game", "Load Game", "DemoGame"}, // Games
            {"Preferences", "About"},  // Setting
            {"User Profile"} // Profile (added menu option)
        };
        fillMenuBar(menuBar, menus, menuOptions); //Creates dashboard based on above arrays
        
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

    private VBox createSidebar () {
        VBox sidebar = new VBox (10); // Vertical layour for sidebar
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setStyle("-fx-background-color: #2f3136; -fx-text-fill: white:");

        //Add logo or image to the sidebar
        ImageView logo = new ImageView(new Image("file:logo.png")); //Will need to be replaced
        logo.setFitHeight(50);
        logo.setFitWidth(50);

        // Sidebar buttons 
        Button homeButton = createSidebarButton("Home", event -> changePage("Dash Board!"));
        Button gamesButton = createSidebarButton("Game", event -> changePage("New Game"));
        Button settingsButton = createSidebarButton("Settings", event -> changePage("Preferences"));
        Button profileButton = createSidebarButton("Profile", event -> changePage("User Profile"));

         // Add items to the sidebar
        sidebar.getChildren().addAll(logo, homeButton, gamesButton, settingsButton, profileButton);
        
        return sidebar;
    }

    private Button createSidebarButton(String text, EventHandler event) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;");
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
                // page.getChildren().add(new ProfileController().getPage()); // Load User Profile Page
                break;
            default:
                page.getChildren().add(new Label("Welcome to ChessBug!"));
        }
    }
   
    
    public static void main(String[] args) {
        Application.launch(args);
    }
}
