/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package chessBug;

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
public class GamePage {

    //Attributes
    //Chess game state
    ChessGame game;
    Boolean playerColor; //true is white; flase is black
    //Promotion variables
    char[] promotionChoice = new char[1]; //char reference that can be modified by event handling lambda functions
    PromotionSelection promotionLambda = (PromotionSelection & Serializable) pawn -> {
        return promotionChoice[0];
    };//Use promotionChoice to determine new piece

    //Page state
    String selectedSquare = null;
    GridPane gameBoard;

    //Constructors
    public GamePage() {
        game = new ChessGame(promotionLambda);
        playerColor = true;
        gameBoard = createGameBoard(true);
        updateGameDisplay();
    }

    //Getter Methods
    public GridPane getGameBoard() {
        return gameBoard;
    }

    //Methods
    private GridPane createGameBoard(boolean isWhitePerspective) {
        //Start new game
        game = new ChessGame(promotionLambda);

        //Create chessBoard
        GridPane chessBoard = new GridPane();
        chessBoard.getStyleClass().add("chessBoard");

        //Layout chess board within chessBoard =================================
        for (int row = 0; row < 8; row++) {
            //Create notation labels for the board -----------------------------
            //Collumn labels:   a, b, c, d, e, f, g, h
            Label colLabel = new Label(String.valueOf((char) ('a' + row))); //Create label
            chessBoard.add(colLabel, (isWhitePerspective) ? row + 1 : 8 - row, 8); //Add label to chessBoard
            GridPane.setHalignment(colLabel, HPos.CENTER); //specify label alignment

            //Row labels:       8, 7, 6, 5, 4, 3, 2, 1
            Label rowLabel = new Label(String.valueOf(8 - row)); //Create label
            chessBoard.add(rowLabel, 0, (isWhitePerspective) ? row : 7 - row); //Add label to chessBoard
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
                chessBoard.add(square, (isWhitePerspective) ? col + 1 : 8 - col, (isWhitePerspective) ? 7 - row : row);
            }
        }
        //----------------------------------------------------------------------
        return chessBoard;
    }

    public void updateGameDisplay() {
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

    private void boardInteraction(BorderPane square) {
        if (!game.getGameComplete()
                && game.getPlayerTurn() == playerColor //correct color turn
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
                    //TODO: Handle promotion details
                    /*Note: promote calls playerMove(gameIndex, chessBoard, messageBoard, square)
                    just like the else's statement, but it first requires for the
                    selection of a piece.*/
                    //promote(pawn, gameIndex, chessBoard, messageBoard, square);
                } else {
                    playerMove(square);
                }
            }
            //==================================================================
        }
    }
    
    public void playerMove(BorderPane square) {
        //Preform gameTurn: gameTurn will return true if it is a valid move
        //If the game move is valid
        if (game.gameTurn(selectedSquare, square.getId())) {
            //Update the board display
            updateGameDisplay();
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
    public void promote(Pawn pawn, int gameIndex, GridPane chessBoard, VBox messageBoard, BorderPane square) {
        //Clear messageBoard
        messageBoard.getChildren().clear();

        //Enter promotion prompt -----------------------------------------------
        messageBoard.getChildren().add(new Label(
                ((pawn.getColor()) ? "White" : "Black") + "'s pawn on "
                + ChessGame.encodeNotation(pawn.getRow(), pawn.getCol()) + " has promoted."
        ));
        messageBoard.getChildren().add(new Label("Select a new piece: "));
        // ---------------------------------------------------------------------

        //Enter form input -----------------------------------------------------
        //Create ToggleGroup for RadioButtons
        ToggleGroup promotionOptions = new ToggleGroup();

        //Create RadioButtons
        RadioButton rbQueen = new RadioButton("Queen");
        rbQueen.setOnAction(event -> promotionChoice[0] = 'Q');
        rbQueen.setToggleGroup(promotionOptions);

        RadioButton rbRook = new RadioButton("Rook");
        rbRook.setOnAction(event -> promotionChoice[0] = 'R');
        rbRook.setToggleGroup(promotionOptions);

        RadioButton rbBishop = new RadioButton("Bishop");
        rbBishop.setOnAction(event -> promotionChoice[0] = 'B');
        rbBishop.setToggleGroup(promotionOptions);

        RadioButton rbKnight = new RadioButton("Knight");
        rbKnight.setOnAction(event -> promotionChoice[0] = 'N');
        rbKnight.setToggleGroup(promotionOptions);

        //Create Submit Button
        Button btnSubmit = new Button("Submit");
        btnSubmit.setOnAction(event -> {
            if (promotionChoice[0] != '0') {
                playerMove(square);
            }
        });

        //Add form elements to messageBoard
        messageBoard.getChildren().addAll(rbQueen, rbRook, rbBishop, rbKnight, btnSubmit);
        // ---------------------------------------------------------------------
    }
}
