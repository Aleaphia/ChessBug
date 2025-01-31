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
import javafx.event.Event;
/**
 *
 * @author shosh
 */
public class ChessBug extends Application {
    //Global variables
    VBox page = new VBox(); // space to change with page details
    
    /**Delete
     * ChessGame game = null;
    
    //Promotion variables
    char[] promotionChoice = new char[1]; //char reference that can be modified by event handling lambda functions
    PromotionSelection promotionLambda = (PromotionSelection & Serializable) pawn -> {return promotionChoice[0];};//Use promotionChoice to determine new piece
    */
    
    @Override
    public void start(Stage primaryStage) {
    //Create stage layout ======================================================
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
            {"Preferences", "About"}, // Setting
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
            case "New Game" -> openNewGame();
            default -> page.getChildren().add(new Label("Debug: " + newPage));
            case "Preferences" -> System.out.println("Navigating to Prefrences...");
            case "About" -> System.out.println("Showing About page...");
            case "User Profile" -> {
                // Navigate to User Profile page
                System.out.println("Navigating to User Profile...");
                // Implement ProfileControl usage here!
            } 
        }
    }
    
    private void openNewGame(){
        GamePage gamePage = new GamePage();
        page.getChildren().add(gamePage.getGameBoard());
        
    }
//    DELETE
    //private GridPane createGameBoard(boolean isWhitePerspective){
//        //Start new game
//        game = new ChessGame(promotionLambda);
//        
//        //Create chessBoard
//        GridPane chessBoard = new GridPane();
//        chessBoard.getStyleClass().add("chessBoard");
//        
//        //Layout chess board within chessBoard =================================
//        for (int row = 0; row < 8; row++) {
//            //Create notation labels for the board -----------------------------
//            //Collumn labels:   a, b, c, d, e, f, g, h
//            Label colLabel = new Label(String.valueOf((char) ('a' + row))); //Create label
//            chessBoard.add(colLabel, (isWhitePerspective)? row + 1 : 8 - row, 8); //Add label to chessBoard
//            GridPane.setHalignment(colLabel, HPos.CENTER); //specify label alignment
//
//            //Row labels:       8, 7, 6, 5, 4, 3, 2, 1
//            Label rowLabel = new Label(String.valueOf(8 - row)); //Create label
//            chessBoard.add(rowLabel, 0, (isWhitePerspective)? row : 7 - row); //Add label to chessBoard
//            //specify label alignment
//            GridPane.setValignment(rowLabel, VPos.CENTER);
//            GridPane.setHalignment(rowLabel, HPos.RIGHT);
//            //------------------------------------------------------------------
//
//            //Create each board square in the row ------------------------------
//            for (int col = 0; col < 8; col++) {
//                //Create new pane to represent square
//                BorderPane square = new BorderPane();
//                //Set square ID to notational square name (e.g., b4, c6, h1, etc.)
//                square.setId("" + (char) ('a' + col) + (row + 1));
//                
//                //Give squares a class based on color to create checkered pattern
//                if ((row + col) % 2 != 0) {
//                    square.getStyleClass().add("white");
//                } else {
//                    square.getStyleClass().add("black");
//                }
//
//                //Square styles
//                //Specify size for square
//                int squareSize = 50;
//                square.setMinHeight(squareSize);
//                square.setMinWidth(squareSize);
//                //Align square
//                GridPane.setValignment(square, VPos.CENTER);
//                GridPane.setHalignment(square, HPos.CENTER);
//
//                //TODO: Add functionality to pressing a square
//                //square.setOnMouseClicked(event -> boardInteraction(chessBoard, square));
//
//                //Add square to chessBoard
//                chessBoard.add(square, (isWhitePerspective)? col + 1 : 8 - col, (isWhitePerspective)? 7 - row : row);
//            }
//        }
//        //----------------------------------------------------------------------
//        updateGameDisplay(chessBoard);
//        
//        return chessBoard;
//    }
    
//    private void updateGameDisplay(GridPane chessBoard){
//         //Update each square in chessBoard to reflect game's condition
//        /*
//        Note: we only need to modify squares (not labels), and all squares are
//        created from the BoarderPane class, so we can modify all BoarderPane
//        children of chessBoard.
//        */
//        chessBoard.getChildren().forEach(child -> {
//            if (child instanceof BorderPane square) {
//                //System.out.println("Debug: Board id: " + board.getParent().getId() + ", game list size: " + gameList.size());
//                
//                //Clear the square's display content
//                square.getChildren().clear();
//        
//                //Get the piece inhabiting the corresponding location in the game
//                Piece piece = game.getLocalPiece(square.getId());
//                //If the piece is not null, load the correct image representation of the piece
//                if (piece != null) {
//                    //Determine imageFileName for based on the piece
//                    String imageFileName = (piece.getColor()) ? "white" : "black";
//                    imageFileName += piece.getClass().getSimpleName();
//                    
//                    //Load corresponding image
//                    try (FileInputStream imageFile = new FileInputStream("pieceImages/" + imageFileName + ".png")) {
//                        //Create image
//                        ImageView icon = new ImageView(new Image(imageFile));
//                        //Style image
//                        icon.setFitHeight(square.getMinHeight() - 6); //Set Height of image. Note x - 6 allows for insets of 3px
//                        icon.setPreserveRatio(true); //Maintain ratio
//                        //Add image
//                        square.setCenter(icon);
//                    } catch (IOException e) {
//                        //If image is not found, use a label to hold the piece's string representation
//                        square.getChildren().add(new Label(piece.toString()));
//                    }
//                }
//            }
//        });
//        //======================================================================
//        
//    }
    
    public static void main(String[] args) {
        Application.launch(args);
    }
}
