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
import javafx.scene.control.ScrollPane;

public class GameView {

    private final GameController controller;

    //Page state
    private final BorderPane page = new BorderPane();
    private GridPane gameBoard;
    private VBox chatContent;
    private GridPane notationContent;
    private VBox msgBoard = new VBox();

    private String selectedSquare = null;

    //Constructors
    public GameView(GameController controller) {
        this.controller = controller;
        buildGamePage();
    }

    //Getter/Setters ===========================================================
    //Getter Methods
    public BorderPane getPage() {return page;}
    private BorderPane getBorderPaneFromId(String id) {
        BorderPane[] squarePane = new BorderPane[1];
        gameBoard.getChildren().forEach(child -> {
            if (child instanceof BorderPane square) {
                if (square.getId().equals(id)) {
                    squarePane[0] = square;
                }
            }
        });
        return squarePane[0];
    }
    
    //Setter Methods
    public void deselectSquare() {selectedSquare = null;}
    
    //Other Methods ============================================================
    //Builder methods ----------------------------------------------------------
    /** buildGamePage - creates the page layout to display match, chat, and notation
     */
    private void buildGamePage() {
        //Clear page
        page.getChildren().clear();

        //Create spaces
        createGameBoard(controller.getPlayerColor());
        
        //Page layout
        page.setCenter(gameBoard);
        page.setLeft(createChatSpace());
        page.setRight(createNotationSpace());  
                
        //Style
        page.getStyleClass().add("page");
        msgBoard.getStyleClass().add("page");
        
    }
    
    //Assistant methods to build the differnt portions of the game page
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
            
            //Styles
            colLabel.getStyleClass().add("boardLabel");
            rowLabel.getStyleClass().add("boardLabel");
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
        gameBoard.add(msgBoard, 0, 9, 9, 1); //Add msgBoard
    }
    private VBox createChatSpace() {
        //Create Chat space
        VBox chatSpace = new VBox();
        chatContent = new VBox(); //global variable to allow direct and easy manipulation for new mgs
        TextField msgInput = new TextField();
        
        //ScrollPanes contain the chat contents to prevent chat page overflow
        ScrollPane scroll = new ScrollPane(chatContent);
        
        //Placement properties
        chatSpace.setMaxWidth(200);
        
        VBox.setVgrow(scroll, Priority.ALWAYS);
        scroll.setMaxWidth(Double.MAX_VALUE);
        scroll.setPrefHeight(gameBoard.getHeight());
        scroll.setVvalue(1.0); 
        chatContent.heightProperty().addListener(observable -> scroll.setVvalue(1D));
        
        chatContent.setAlignment(Pos.TOP_LEFT);

        //chat space components
        chatSpace.getChildren().addAll(scroll, msgInput);
        
        //Styles ---------------------------------------------------------------
        scroll.getStyleClass().add("chatBox");
        chatContent.getStyleClass().add("chatBox");

        // ---------------------------------------------------------------------
        //Function
        msgInput.setOnAction(event -> {
            //Formulate message
            String msg = msgInput.getText();

            //Send msg to database
            controller.sendChatMessage(msg);

            //Clear input
            msgInput.setText("");
        });
        
        return chatSpace;
    }
    private VBox createNotationSpace() {
        //Create notation space
        VBox notationSpace = new VBox();
        GridPane notationLabel = new GridPane();
        notationContent = new GridPane(); //global variable to allow direct and easy manipulation
        
        //ScrollPanes contain the chat contents to prevent page overflow
        ScrollPane scroll = new ScrollPane(notationContent);
        
        //Placement properties
        notationSpace.setMaxWidth(200);
        
        VBox.setVgrow(scroll, Priority.ALWAYS);
        scroll.setMaxWidth(Double.MAX_VALUE);
        scroll.setPrefHeight(gameBoard.getHeight());
        scroll.setVvalue(1.0); 
        notationContent.heightProperty().addListener(observable -> scroll.setVvalue(1D));
        
        notationContent.setAlignment(Pos.TOP_LEFT);
        
        //Style
        notationSpace.getStyleClass().add("notationBoard");
        notationLabel.getStyleClass().addAll("notationGrid", "notationBoard");
        notationContent.getStyleClass().addAll("notationGrid", "notationBoard");
        
        //notation space components
        notationSpace.getChildren().addAll(notationLabel, scroll);
        
        //Create notation space
        Label labelW = new Label("White");
        Label labelB = new Label("Black");
        notationLabel.add(labelW, 1, 0);
        notationLabel.add(labelB, 2, 0);
        //Hold space 
        Label newLabel = new Label();
        newLabel.setMinWidth(20);
        notationLabel.add(newLabel,0,0);
        
        //Style
        labelW.getStyleClass().addAll("notationLabel", "header");
        labelB.getStyleClass().addAll("notationLabel", "header");

        return notationSpace;
    }
    
    //Board interactions -------------------------------------------------------
    private void boardInteraction(BorderPane square) {
        if (!controller.isGameComplete() && controller.isThisPlayersTurn()  //correct color turn
                ){
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
            Piece localPiece = controller.getLocalPiece(square.getId());
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
                    && localPiece.getColor() == controller.getPlayerTurnBoolean() //Check that piece color matches players turn's color
                    ) {
                //Select Square
                //Add square to selectedSquareList at the corresponding index
                selectedSquare = square.getId();
                //Signify selection with style class
                square.getStyleClass().add("selected");

                //Display all possible moves for the selected piece
                //Get list of possible moves
                ArrayList<String> possibleMoves = controller.getMoveListForLocalPiece(square.getId());
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
                String potentialMove = selectedSquare + square.getId();
                //Check for promotion move (promotions require an extra prompt for piece selection)
                if (controller.getLocalPiece(selectedSquare) instanceof Pawn pawn //'mover' piece is a pawn
                        && (square.getId().contains("1") || square.getId().contains("8")) //pawn is moving to 1st or 8th rank
                        && validMove //the move is valid (this prevents prompt display for illegal promotion moves)
                        ) {
                    //Handle promotion details
                    /*Note: promote calls playerMove(square)
                    just like the else's statement, but it first requires for the
                    selection of a piece.*/
                    promote(square, potentialMove);
                } else {
                    controller.playerMove(potentialMove);
                }
            }
            //==================================================================
        }
    }
    //Handling Promotion Moves: Create form for promotion piece selection
    private void promote(BorderPane square, String potentialMove) {
        //Determine icons to display
        String color = (controller.getPlayerTurnBoolean()) ? "white" : "black";
        String[] choices = {"Queen", "Rook", "Bishop", "Knight"};
        //Determine location to display them
        String locationName = square.getId();
        char col = locationName.charAt(0);
        int row = locationName.charAt(1) - '0';
        int dir = (row == 1) ? 1 : -1;
        //Display promotion prompt
        try {
            for (String piece : choices) {
                ImageView icon = new ImageView(new Image(new FileInputStream("pieceImages/" + color + piece + ".png")));
                icon.getStyleClass().add("promotionChoice");
                //Style image
                icon.setFitHeight(square.getMinHeight() - 6); //Set Height of image. Note x - 6 allows for insets of 3px
                icon.setPreserveRatio(true); //Maintain ratio
                //Add function
                icon.setOnMouseClicked(event -> {
                    char promotionChoice = (piece.charAt(0) == 'K') ? 'N' : piece.charAt(0); //user the first letter of each peice (but knights use N instead of K)
                    controller.playerMove(potentialMove + promotionChoice);
                });

                //Add to pane
                getBorderPaneFromId("" + col + row).setCenter(icon);
                row += dir;
            }
        } catch (Exception e) {
            System.out.println("Error loading promotion prompt images");
        }
    }
    
    //Refresher methods --------------------------------------------------------
    //Public methods
    /**refresh - reloads entire page - refreshes game board and chat
     * @param client - database connection needed to update the message board
     */
    public void refresh(Client client) {
        refreshGameDisplay();
        internalRefreshMessageBoard(client);
    }

    /** refreshMessageBoard - checks database for new messages - only refreshes chat
     * @param client - database connection
     */
    public void refreshMessageBoard(Client client) {
        internalRefreshMessageBoard(client);
    }
    
    //Private methdos
    private void internalRefreshMessageBoard(Client client) {
        
        //Get any new messages, add each message to the chat
        controller.getChatMessages().forEach(msg -> {
            long time = System.currentTimeMillis(); //DEBUG
            HBox messageContainer = new HBox();
            System.out.println(System.currentTimeMillis() - time); //DEBUG
            //Build content
            //profile picture
            ImageView pfpView = new ImageView(new Image(client.getUserProfilePictureURL(msg.getAuthor())));
            System.out.println(System.currentTimeMillis() - time); //DEBUG
            StackPane pfpViewContainer = new StackPane(pfpView);
            
            pfpView.setFitWidth(32);
            pfpView.setFitHeight(32);
            pfpViewContainer.getStyleClass().add("chatPfp");
            System.out.println(System.currentTimeMillis() - time);
            
            //Message
            Label label = new Label(msg.getAuthor() + ": " + msg.getContent());
            
            label.getStyleClass().addAll("chatMessage",
                    //Test if the client player sent this message and add appropriate style class
                    (msg.getAuthor().equals(controller.getUsername()))? 
                            "thisPlayerMessage": "otherPlayerMessage");
            System.out.println(System.currentTimeMillis() - time);
            
            //Add contents to chat container
            messageContainer.getChildren().addAll(pfpViewContainer, label);
            chatContent.getChildren().add(messageContainer);
            System.out.println(System.currentTimeMillis() - time);
        });
    }
    private void refreshGameDisplay() {
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
                Piece piece = controller.getLocalPiece(square.getId());
                //If the piece is not null, load the correct image representation of the piece
                if (piece != null) {
                    //Determine imageFileName for based on the piece
                    String imageFileName = (piece.getColor()) ? "white" : "black";
                    imageFileName += piece.getClass().getSimpleName();

                    //Load corresponding image
                    try (InputStream imageFile = GameView.class.getResourceAsStream("/resources/images/pieces/" + imageFileName + ".png")) {
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
    }

    //Display/add new content ------------------------------------------------------
    /** displayBotMessage - displays message in chat box with "botMessage" style
     * @param msg - message to display
     */
    public void displayBotMessage(String msg){
        Label curr = new Label(msg);
        curr.getStyleClass().addAll("botMessage");
        msgBoard.getChildren().clear();
        msgBoard.getChildren().add(curr);
    }
    
    /** addToNotationBoard - Add a new move to the notation board
     * @param move - coordinate notation move (e.g., e2e4 or e7e8Q)
     * @param playerTurn - true means it's white's turn, false means it's black's turn
     * @param gameMove - the move number according to standard chess notation (each play gest a first, second, ...  move)
     */
    public void addToNotationBoard(String move, Boolean playerTurn, int gameMove) {
        //Expanded notation string
        String expandedMove = move.substring(0, 2) + "-" + move.substring(2, 4)
                + ((move.length() == 4) ? "" : ("=" + move.substring(4))); //add promotion info if needed
        Label newLabel = new Label(expandedMove);
        //Place notation one the notation board
        if (playerTurn) { //White just moved
            Label gameMoveLabel = new Label(Integer.toString(gameMove));
            gameMoveLabel.setMinWidth(20);
            
            notationContent.add(gameMoveLabel, 0, gameMove); //Add new turn label
            notationContent.add(newLabel, 1, gameMove); //Add white move
        } else { //Black just moved
            notationContent.add(newLabel, 2, gameMove); //Add black move
        }
        GridPane.setVgrow(newLabel, Priority.ALWAYS);
        GridPane.setHgrow(newLabel, Priority.ALWAYS);
        newLabel.setMinHeight(30);
        
        //Style
        newLabel.getStyleClass().add("notationLabel");
    }
}
