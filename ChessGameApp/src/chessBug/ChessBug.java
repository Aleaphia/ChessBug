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
/**
 *
 * @author shosh
 */
public class ChessBug extends Application {
    //Global variables
    Pane page = new VBox(); // space to change with page details
//    Client client;
    
    @Override
    public void start(Stage primaryStage) {
    //Create stage layout ======================================================
        //Fake login TODO- make login page the first page pulled up
        try{
            // Connect to database
//            client = new Client("user", "p@ssw0rd!"); // (example user)
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
            {"New Game"}, // Games
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
    
    private void changePage(String newPage){
        page.getChildren().clear();
        
        switch (newPage){
            case "New Game" -> page.getChildren().add(new GamePage().getPage());//client.getFriends().get(0)).getPage()); //TODO Allow friend selection
            case "Preferences" -> {
                //Navigate to prefrences
                System.out.println("Navigating to Prefrences...");}
            case "About" -> {
                // Navigate to About page
                System.out.println("Showing About page...");
                }
            case "User Profile" -> {
                
                // Navigate to User Profile page
                System.out.println("Navigating to User Profile...");
                // Implement ProfileControl usage here!
            }
            
            default -> page.getChildren().add(new Label("Debug: " + newPage));

        }
    }
   
    
    public static void main(String[] args) {
        Application.launch(args);
    }
}
