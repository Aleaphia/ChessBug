/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package chessBug.game;

import chessGame.*;
import chessBug.network.*;
import java.io.*;
import java.util.*;

import javafx.geometry.*;

import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.Node;
public class MatchListView {
    private GameController controller;
    
    //Page state
    private VBox page = new VBox();
    
    //Constructors
    public MatchListView(GameController controller) {
        //Connect to controller
        this.controller = controller;
        
        //Load prompt
        createPrompt();
    }
    
    //Getter/Setter Methods
    public Node getPage(){return page;}

    
    private void createPrompt(){        
       controller.getMatchList().forEach(match ->{
           Label currMatch = new Label(match.toString());
           
           currMatch.setOnMouseClicked( event -> {
               controller.matchSelection(match);
           });
           
           page.getChildren().add(currMatch);
       });
        page.setMinHeight(100);
        page.setAlignment(Pos.TOP_CENTER);
    }
}
