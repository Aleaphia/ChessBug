/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package chessBug.game;

import chessGame.*;
import java.awt.Color;
import chessBug.network.*;
import java.io.*;
import java.util.*;
import java.util.stream.Stream;

import javafx.geometry.*;

import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.Node;
import javafx.event.Event;
import javafx.event.ActionEvent;
import javafx.util.Duration;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;


/**
 *
 * @author shosh
 */
public class GamePage {
    HBox page = new HBox();
    //Attributes
    //Chess game state
    int gameMove = 0;
    Boolean playerColor; //true is white; flase is black
    //Promotion variables
    char[] promotionChoice = new char[1]; //char reference that can be modified by event handling lambda functions
    PromotionSelection promotionLambda = (PromotionSelection & Serializable) pawn -> {
        return promotionChoice[0];
    };//Use promotionChoice to determine new piece
    ChessGame game = new ChessGame(promotionLambda);

    //Database Connection
    Client client;
    Match match;
    Chat chat;
    
    //Page state
    GridPane gameBoard = new GridPane();
    VBox msgBoard = new VBox();
    GridPane notationScreen = new GridPane();
    
    String selectedSquare = null;
    

    //Constructors
    //
    public GamePage(Client player, User opponent, boolean playerColor) { //New game
        //Determine player color
        this.playerColor = playerColor;

        //Connect to database
        try{
            //Save client
            client = player;
            //Create new match
            if (playerColor)
                match = client.createMatch(client.getOwnUser(), opponent);
            else 
                match = client.createMatch(opponent, client.getOwnUser());
            //Get chat
            chat = match.getChat();
        }
        catch( Exception e){
            System.out.println("Error");
        }  
        
        //Create new match in database
        
        
        //load match
        loadGame();
        
        //page layout
        createGameBoard(true);
        createMsgBoard();
        createNotationBoard();
        page.getChildren().addAll(msgBoard, gameBoard, notationScreen);
        
        //Update game state
        updateGameDisplay();
        
        //Check database
        continueDatabaseChecks();
    }
    
    public GamePage(Client player, Match match){
        //Get match info
        client = player;
        this.match = match;
        chat = match.getChat();
        playerColor = match.getWhite().equals(client.getOwnUser()); //Assumes player is valid player in match
        
        //Get moves from database
        loadGame(match.getAllMoves());
        
        //page layout
        createGameBoard(true);
        createMsgBoard();
        createNotationBoard();
        page.getChildren().addAll(msgBoard, gameBoard, notationScreen);
        
        //Update game state
        updateGameDisplay();
        
        //Check database
        continueDatabaseChecks();
    }
    
    //loop database check
    private void continueDatabaseChecks(){
        //Check database
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), (ActionEvent event) -> {
            //Add repeated database checks here ================================
            match.poll(client).forEach((move) -> playerMove(move));
            updateMsgBoard();
            
            // =================================================================
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    //Getter Methods
    public Node getPage(){return page;}
   
    private void loadGame(){
        loadGame(new ArrayList<>());
    }
    private void loadGame(ArrayList<String> moveList){
        //make previous moves
        for(String move : moveList){
            String from = move.substring(0,2);
            String to = move.substring(2,4);
            //System.out.println( "Debug: From: " + from + "\tTo: " + to);
            if (move.length() == 5){ // promotion move
                promotionChoice[0] = move.charAt(4); //promotion character located at 4th index
            }
            game.gameTurn(from, to);
            updateNotationBoard(move);
        }
        promotionChoice[0] = '0';
        updateGameDisplay();
       
    }

    //Chess game methods
    private void createGameBoard(boolean isWhitePerspective) {
        //Create chessBoard
        gameBoard = new GridPane();
        gameBoard.getStyleClass().add("chessBoard");

        //Layout chess board within chessBoard =================================
        for (int row = 0; row < 8; row++) {
            //Create notation labels for the board -----------------------------
            //Collumn labels:   a, b, c, d, e, f, g, h
            Label colLabel = new Label(String.valueOf((char) ('a' + row))); //Create label
            gameBoard.add(colLabel, (isWhitePerspective) ? row + 1 : 8 - row, 8); //Add label to chessBoard
            GridPane.setHalignment(colLabel, HPos.CENTER); //specify label alignment

            //Row labels:       8, 7, 6, 5, 4, 3, 2, 1
            Label rowLabel = new Label(String.valueOf(8 - row)); //Create label
            gameBoard.add(rowLabel, 0, (isWhitePerspective) ? row : 7 - row); //Add label to chessBoard
            //specify label alignment
            GridPane.setValignment(rowLabel, VPos.CENTER);
            GridPane.setHalignment(rowLabel, HPos.RIGHT);
            //------------------------------------------------------------------

            //Create each board square in the row ------------------------------
            for (int col = 0; col < 8; col++) {
                //Create new pane to represent square
                BorderPane square = new BorderPane();
                //Set square ID to notational square name (e.g., b4, c6, h1, etc.)
                square.setId("" + (char) ('a' + col) + (row + 1));

                //Give squares a class based on color to create checkered pattern
                if ((row + col) % 2 != 0) {
                    square.getStyleClass().add("white");
                } else {
                    square.getStyleClass().add("black");
                }

                //Square styles
                //Specify size for square
                int squareSize = 50;
                square.setMinHeight(squareSize);
                square.setMinWidth(squareSize);
                //Align square
                GridPane.setValignment(square, VPos.CENTER);
                GridPane.setHalignment(square, HPos.CENTER);

                //Add functionality to pressing a square
                square.setOnMouseClicked(event -> boardInteraction((BorderPane) event.getSource()));

                //Add square to chessBoard
                gameBoard.add(square, (isWhitePerspective) ? col + 1 : 8 - col, (isWhitePerspective) ? 7 - row : row);
            }
        }
        //----------------------------------------------------------------------
    }

    private void updateGameDisplay() {
        //Update each square in chessBoard to reflect game's condition
        /*
        Note: we only need to modify squares (not labels), and all squares are
        created from the BoarderPane class, so we can modify all BoarderPane
        children of chessBoard.
         */
        gameBoard.getChildren().forEach(child -> {
            if (child instanceof BorderPane square) {
                //System.out.println("Debug: Board id: " + board.getParent().getId() + ", game list size: " + gameList.size());

                //Clear the square's display content
                square.getChildren().clear();

                //Get the piece inhabiting the corresponding location in the game
                Piece piece = game.getLocalPiece(square.getId());
                //If the piece is not null, load the correct image representation of the piece
                if (piece != null) {
                    //Determine imageFileName for based on the piece
                    String imageFileName = (piece.getColor()) ? "white" : "black";
                    imageFileName += piece.getClass().getSimpleName();

                    //Load corresponding image
                    try (FileInputStream imageFile = new FileInputStream("pieceImages/" + imageFileName + ".png")) {
                        //Create image
                        ImageView icon = new ImageView(new Image(imageFile));
                        //Style image
                        icon.setFitHeight(square.getMinHeight() - 6); //Set Height of image. Note x - 6 allows for insets of 3px
                        icon.setPreserveRatio(true); //Maintain ratio
                        //Add image
                        square.setCenter(icon);
                    } catch (IOException e) {
                        //If image is not found, use a label to hold the piece's string representation
                        square.getChildren().add(new Label(piece.toString()));
                    }
                }
            }
        });
        //======================================================================

    }
    private void updateNotationBoard(String move){
        String expandedMove = move.substring(0,2) + "-" + move.substring(2,4) +
                ((move.length() == 4)? "":("=" + move.substring(4))); //add promotion info if needed
        if (!game.getPlayerTurn()){ //White just moved
                notationScreen.add(new Label(Integer.toString(++gameMove)), 0 , gameMove);
                notationScreen.add(new Label(expandedMove), 1 , gameMove);
            }
            else{ //Black just moved
                notationScreen.add(new Label(expandedMove), 2 , gameMove);
            }
    }

    private void boardInteraction(BorderPane square) {
        if (!game.getGameComplete()
                //&& game.getPlayerTurn() == playerColor //correct color turn //ADD BACK LATER
                ) {
            /*
            One of two valid actions may occur when a user selects a square:
                1) The user selects a peice to move
                2) The user selects a square they want the selected peice to move to
            
            These different events have different qualifying features and need
            different information. Path 1 needs to know the identity of the local
            piece. Path 2 needs to know if the move is valid. We will start this
            method my gathering these two information pieces.
             */

            //Option 1 info: Get the Piece on the selected square
            /*(Note: if square is empty than the Piece will be null)*/
            Piece localPiece = game.getLocalPiece(square.getId());
            //Option 2 info: Check if the board interaction causes a valid move
            /* Valid Move Note
            When a piece is selected, all valid moves for a selected piece are
            displayed to the user using the style class "possibleMove". We can
            see if a move is valid by checking if the selected square has the
            "possibleMove" style class.
             */
            boolean validMove = square.getStyleClass().contains("possibleMove");

            //Remove special style classes from all grid elements
            gameBoard.getChildren().forEach(gridContent -> {
                gridContent.getStyleClass().remove("selected");
                gridContent.getStyleClass().remove("possibleMove");
            });

            //Path 1: The user selects a piece to move =========================
            /*Qualifications:
                - a piece of the correct color has been selected
             */
            if (localPiece != null //A piece has been selected
                    && localPiece.getColor() == game.getPlayerTurn() //Check that piece color matches players turn's color
                    ) {
                //Select Square
                //Add square to selectedSquareList at the corresponding index
                selectedSquare = square.getId();
                //Signify selection with style class
                square.getStyleClass().add("selected");

                //Display all possible moves for the selected piece
                //Get list of possible moves
                ArrayList<String> possibleMoves = game.getMoveListForLocalPiece(square.getId());
                //Add a style class to each valid move
                gameBoard.getChildren().forEach(gridContent -> {
                    if (possibleMoves.contains(gridContent.getId())) {
                        gridContent.getStyleClass().add("possibleMove");
                    }
                });
            } //==================================================================
            //Path 2: The user selects a square to move to =====================
            /*Qualifications:
                - A 'mover' piece has been selected
                    (i.e., the corresponding entry in selectedSquareList is set)
             */ else if (selectedSquare != null) { //There is a selected piece
                //Check for promotion move (promotions require an extra prompt for piece selection)
                if (game.getLocalPiece(selectedSquare) instanceof Pawn pawn //'mover' piece is a pawn
                        && (square.getId().contains("1") || square.getId().contains("8")) //pawn is moving to 1st or 8th rank
                        && validMove //the move is valid (this prevents prompt display for illegal promotion moves)
                        ) {
                    //Handle promotion details
                    /*Note: promote calls playerMove(square)
                    just like the else's statement, but it first requires for the
                    selection of a piece.*/
                    promote(square);
                } else {
                    playerMove(square);
                }
            }
            //==================================================================
        }
    }
    
    public void playerMove(String notation){
        if (notation.length() == 5)
            promotionChoice[0] = notation.charAt(4);
        
        playerMove(notation.substring(0,2), notation.substring(2,4));
    }
    public void playerMove(BorderPane square) {
        playerMove(selectedSquare,square.getId());
    }
    private void playerMove(String fromSquare, String toSquare){
        //Preform gameTurn: gameTurn will return true if it is a valid move
        //If the game move is valid
        if (game.gameTurn(fromSquare, toSquare)) {
            //Update the board display
            updateGameDisplay();
            //Update Notation Board
            String notationMove = fromSquare + toSquare;
            notationMove += (promotionChoice[0] == '0')? "" : promotionChoice[0]; //Add promotion choice if relevent
            updateNotationBoard(notationMove);
            match.makeMove(client, notationMove);
            //Deselect square
            selectedSquare = null;
        }
        else { //If the game move is Illegal, output error message
            //TODO messageBoard.getChildren().add(new Label("Illegal move: try again."));
        }
        //Reset promotionChoice
        promotionChoice[0] = '0';
    }

    //Handling Promotion Moves: Create form for promotion piece selection
    public void promote(BorderPane square) {
        try{
            //Determine icons to display
            String color = (game.getPlayerTurn()) ? "white" : "black";
            String[] choices = {"Queen", "Rook", "Bishop", "Knight"};
            //Determine location to display them
            String locationName = square.getId();
            char col = locationName.charAt(0);
            int row = locationName.charAt(1) - '0';
            int dir = (row == 1)? 1:-1;
            for(String piece : choices){
                ImageView icon = new ImageView(new Image(new FileInputStream("pieceImages/" + color + piece + ".png")));
                icon.getStyleClass().add("promotionChoice");
                //Style image
                icon.setFitHeight(square.getMinHeight() - 6); //Set Height of image. Note x - 6 allows for insets of 3px
                icon.setPreserveRatio(true); //Maintain ratio
                //Add background
                //Add function
                icon.setOnMouseClicked(event -> {
                    promotionChoice[0] = (piece.charAt(0)=='K')?'N' : piece.charAt(0); //user the first letter of each peice (but knights use N instead of K)
                    playerMove(square);
                });
                
                //Add to pane
                getBorderPaneFromId("" + col + row).setCenter(icon);
                row += dir;
            }
        }catch(Exception e){
            
        }
    }
    
    private BorderPane getBorderPaneFromId(String id){
        BorderPane[] squarePane = new BorderPane[1];
        gameBoard.getChildren().forEach(child -> {
            if (child instanceof BorderPane square){
                if (square.getId().equals(id)){
                    squarePane[0] = square;
                }
            }
        });
        return squarePane[0];
    }
    
    //MsgBoard Interactions
    private void createMsgBoard(){
        VBox msgScreen = new VBox();
        TextField msgInput = new TextField();
        
        msgBoard.getChildren().addAll(msgScreen, msgInput);
        msgBoard.getStyleClass().add("chatBox");
        
//        chat.poll(client);
//        chat.getAllMessages().forEach(x -> {
//            String msg = x.getAuthor() + ": " + x.getContent();
//            System.out.println(msg);
//            msgScreen.getChildren().add(new Label(msg));
//        });
        
        msgInput.setOnAction(event -> {
            //Formulate message
            //TO-DO add real player names
            String user = (playerColor)?"White" : "Black";
            String msg = msgInput.getText();
            
            //Display msg on screen
            msgScreen.getChildren().add(new Label(user + ": " + msg));
            
//            chat.send(client, msg);
            
            //Clear input
            msgInput.setText("");
                });

        msgBoard.setMinHeight(100);
        msgBoard.setAlignment(Pos.BOTTOM_CENTER);
    }
    private void updateMsgBoard(){
//        Stream<Message> newPoll = chat.poll(client);
//        newPoll.forEach(x -> {
//            String msg = x.getAuthor() + ": " + x.getContent();
//            VBox msgScreen = (VBox)msgBoard.getChildren().get(0);            
//            msgScreen.getChildren().add(new Label(msg));
//        });
    }
    
    private void createNotationBoard(){
        
        //gameBoard.add(square, (isWhitePerspective) ? col + 1 : 8 - col, (isWhitePerspective) ? 7 - row : row);
        notationScreen.add(new Label("White"), 1, 0);
        notationScreen.add(new Label("Black"), 2, 0);
        
    }
    

}
