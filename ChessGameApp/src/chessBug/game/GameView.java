/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package chessBug.game;

import chessBug.misc.GameSelectionUI;
import chessGame.*;
import chessBug.network.*;
import java.io.*;
import java.util.*;

import javafx.geometry.*;

import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;

public class GameView {

    private GameController controller;

    //Page state
    private BorderPane page = new BorderPane();
    private GridPane gameBoard;
    private VBox chatContent;
    private GridPane notationContent;

    private String selectedSquare = null;

    //Constructors
    public GameView(GameController controller) {
        this.controller = controller;

        buildGameSelectionPrompt();
    }

    //Getter/Setter Methods
    public BorderPane getPage() {
        return page;
    }
    public void displayMessage(String msg){
        Label curr = new Label(msg);
        curr.getStyleClass().addAll("chatMessage","botMessage");
        chatContent.getChildren().add(curr);
    }

    public void deselectSquare() {
        selectedSquare = null;
    }

    //Refresher methods
    public void refresh() {
        refreshGameDisplay();
        internalRefreshMessageBoard();
    }

    public void refreshMessageBoard() {
        internalRefreshMessageBoard();
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
        //======================================================================

    }

    private void internalRefreshMessageBoard() {
        //Get any new messages
        //Add each message to the chat
        controller.getChatMessages().forEach(x -> {
            String msg = x.getAuthor() + ": " + x.getContent();
            Label label = new Label(msg);
            label.getStyleClass().addAll("chatMessage",
                    //Test if the client player sent this message and add appropriate style class
                    (x.getAuthor().equals(controller.getUserName()))? 
                            "thisPlayerMessage": "otherPlayerMessage");
            chatContent.getChildren().add(label);
        });
    }

    public void addToNotationBoard(String move, Boolean playerTurn, int gameMove) {
        //Expanded notation string
        String expandedMove = move.substring(0, 2) + "-" + move.substring(2, 4)
                + ((move.length() == 4) ? "" : ("=" + move.substring(4))); //add promotion info if needed
        //Place notation one the notation board
        if (playerTurn) { //White just moved
            notationContent.add(new Label(Integer.toString(gameMove)), 0, gameMove); //Add new turn label
            notationContent.add(new Label(expandedMove), 1, gameMove); //Add white move
        } else { //Black just moved
            notationContent.add(new Label(expandedMove), 2, gameMove); //Add black move
        }
    }

    private void boardInteraction(BorderPane square) {
        if (!controller.getGameComplete() && controller.isThisPlayersTurn()  //correct color turn
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
                    && localPiece.getColor() == controller.getPlayerTurn() //Check that piece color matches players turn's color
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
    public void promote(BorderPane square, String potentialMove) {
        //Determine icons to display
        String color = (controller.getPlayerTurn()) ? "white" : "black";
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

    //Page space creation
    private void buildGameSelectionPrompt() {
        //Clear page
        page.getChildren().clear();

        //Game Prompt Panel
        VBox promptSelectionPanel = new VBox();
        page.setCenter(promptSelectionPanel);

        //New game button
        Button newGame = new Button("New Game");
        newGame.setOnMouseClicked(event -> buildGameBuildPrompt());
        promptSelectionPanel.getChildren().addAll(newGame, new GameSelectionUI(controller).getPage());
    }

    private void buildGameBuildPrompt() {
        //Clear page
        page.getChildren().clear();

        //Game Prompt Panel
        VBox promptSelectionPanel = new VBox();
        page.setCenter(promptSelectionPanel);

        //Select Color
        promptSelectionPanel.getChildren().add(new Label("Select color:"));
        char[] colorSelection = new char[1];
        colorSelection[0] = '0'; //w for white, b for black, r for random
        ToggleGroup colorOptions = new ToggleGroup();
        String[] colorOptionList = {"white", "black", "random"};

        for (String option : colorOptionList) {
            RadioButton curr = new RadioButton(option);
            curr.setOnAction(event -> colorSelection[0] = option.charAt(0));
            curr.setToggleGroup(colorOptions);
            promptSelectionPanel.getChildren().add(curr);
        }

        //Challenge friend: list friends in radio buttons
        promptSelectionPanel.getChildren().add(new Label("Challenge friend:"));
        Friend[] friendSelection = new Friend[1];
        friendSelection[0] = null;
        ToggleGroup friendOptions = new ToggleGroup();

        controller.getFriendList().forEach(friend -> {
            RadioButton curr = new RadioButton(friend.getUsername());
            curr.setOnAction(event -> friendSelection[0] = friend);
            curr.setToggleGroup(friendOptions);
            promptSelectionPanel.getChildren().add(curr);
        });

        //Create game button
        Button createGame = new Button("Request Game");
        createGame.setOnMouseClicked(event -> {
            if (colorSelection[0] != '0' && friendSelection[0] != null){
                //Determine color
                boolean playerColor;
                switch (colorSelection[0]) {
                    case 'w' -> playerColor = true; //white
                    case 'b' -> playerColor = false; // black
                    default -> playerColor = new Random().nextBoolean(); //random
                }

                //Create new game
                controller.sendGameRequest(playerColor, friendSelection[0]);
                buildGameSelectionPrompt();
            }
            
        });
        promptSelectionPanel.getChildren().add(createGame);
        
    }

    public void buildGamePage() {
        //Clear page
        page.getChildren().clear();

        //Create spaces
        createGameBoard(controller.getPlayerColor());
        
        //Page layout
        page.setCenter(gameBoard);
        page.setLeft(createChatSpace());
        page.setRight(createNotationSpace());    
    }

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
    }

    private VBox createChatSpace() {
        //Create Chat space
        VBox chatSpace = new VBox();
        chatContent = new VBox(); //global variable to allow direct and easy manipulation for new mgs
        TextField msgInput = new TextField();
        
        //ScrollPanes contain the chat contents to prevent chat page overflow
        ScrollPane scroll = new ScrollPane(chatContent);
        //ScrollPane policies
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        //scroll.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        chatSpace.setMaxWidth(200);
        scroll.setPrefWidth(Double.MAX_VALUE);
        scroll.setMaxWidth(Double.MAX_VALUE);
        
        chatContent.setMaxWidth(170);
        chatContent.setAlignment(Pos.TOP_LEFT);

        //chat space components
        chatSpace.getChildren().addAll(scroll, msgInput);
        
        //Styles ---------------------------------------------------------------
        scroll.getStyleClass().add("chatBox");
        chatContent.getStyleClass().add("chatBox");
//        chatContent.setPrefHeight(2 * page.getHeight() -  msgInput.getHeight());

        // ---------------------------------------------------------------------
        //Function
        msgInput.setOnAction(event -> {
            //Formulate message
            String user = controller.getUserName();
            String msg = msgInput.getText();

            //Display msg on screen
            Label label = new Label(user + ": " + msg);
            label.getStyleClass().addAll("chatMessage","thisPlayerMessage"); //Style
            chatContent.getChildren().add(label);

            //Send msg to database
            controller.sendChatMessage(msg);

            //Clear input
            msgInput.setText("");
        });

        scroll.setPrefHeight(gameBoard.getHeight());
        scroll.setMinHeight(100);
        chatContent.setAlignment(Pos.BOTTOM_CENTER);
        
        return chatSpace;
    }

    private VBox createNotationSpace() {
        //Create notation space
        VBox notationSpace = new VBox();
        GridPane notationLabel = new GridPane();
        notationContent = new GridPane(); //global variable to allow direct and easy manipulation
        
        //ScrollPanes contain the chat contents to prevent page overflow
        ScrollPane scroll = new ScrollPane(notationContent);
        //ScrollPane policies
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        
        
        //notation space components
        notationSpace.getChildren().addAll(notationLabel, scroll);
        
        //Create notation space
        notationLabel.add(new Label("White"), 1, 0);
        notationLabel.add(new Label("Black"), 2, 0);
        //Hold space 
        Label newLabel = new Label();
        newLabel.setMinWidth(25);
        notationLabel.add(newLabel,0,0);

        return notationSpace;
    }
}
