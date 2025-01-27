/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package chessBug;
import listHelper.SavableList;
import chessGame.*;
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
/**
 *
 * @author shosh
 */
public class ChessBug extends Application {
    @Override
    public void start(Stage primaryStage) {
    //Create stage layout ======================================================
        //Main pane
        VBox mainPane = new VBox();
        mainPane.getStyleClass().add("background");
        
        //Create Menu
        MenuBar menuBar = new MenuBar();
        mainPane.getChildren().add(menuBar);
        //TODO - Create menuBar options
        String[] menus = {"Home", "Games" , "Settings"};
        String[][] menuOptions = {
            {"Dash Board"}, // Home
            {"New Game"}, // Games
            {}  // Setting
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
        System.out.println(newPage);
    }
    
    public static void main(String[] args) {
        Application.launch(args);
    }
}
