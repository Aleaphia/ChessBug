package altmantermproject;

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




public class ApplicationGame extends Application {
    //Application's Global Variables
    //Holds all the different gameTabs
    TabPane gameTabsPane = new TabPane();
    //List of ChessGames, holds game information for all open games
    SavableList<ChessGame> gameList = new SavableList<>("savedGames.dat"); ;
    //a list of the last valid square selection from each open board
    ArrayList<String> selectedSquareList = new ArrayList<>();
    /* selectedSquareList Notes:
        - The string value within each entry is the notational name of the square
            (e.g., a5, c8, etc.)
        - seleectedSquareList should always have the same number of indexes as
            gameList, each index in each list corresponds to each other
        - A null value is used to indicate that no square is currently selected
        - A square can only be selected when it holds a piece that is the color
            of the current player turn
    */

    //Promotion variables
    char[] promotionChoice = new char[1]; //char reference that can be modified by event handling lambda functions
    PromotionSelection promotionLambda = (PromotionSelection & Serializable) pawn -> {return promotionChoice[0];};//Use promotionChoice to determine new piece
    
    //Overridden methods for Application Class
    @Override
    public void start(Stage primaryStage) {
        //Setting starting values for global variables
        promotionChoice[0] = '0'; //char literal '0' is used by the program as a default for promotionChoice
        //Create stage layout ==================================================
        //Main pane ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        VBox mainPane = new VBox();
        mainPane.getStyleClass().add("background");

        //Create Menu ----------------------------------------------------------
        /* MenuBar notes
            - MenuBar object can contain Menu objects
            - Menu object can contain MenuItem objects
            - MenuItem objects have setOnAction event
        */
        MenuBar menuBar = new MenuBar();
        
        //Create option on menu
        Menu menuGameOptions = new Menu("Game Options");
        //Create items in menu option and add functionality
        MenuItem menuItemDeleteGame = new MenuItem("Delete Game");
        menuItemDeleteGame.setOnAction(event -> deleteGame());
        MenuItem menuItemNewGame = new MenuItem("New Game");
        menuItemNewGame.setOnAction(event -> newGame());
        //Add items to menu option
        menuGameOptions.getItems().addAll(menuItemDeleteGame, menuItemNewGame);
        
        //Create option on menu
        Menu menuItemDisplayOption = new Menu("Display Options");
        //Create items in menu option and add functionality
        MenuItem menuItemWhitePerpective = new MenuItem("White's Perspective");
        menuItemWhitePerpective.setOnAction(event -> {
            //Identify nodes within tab
            HBox gamePane = (HBox)gameTabsPane.getSelectionModel().getSelectedItem().getContent();
            GridPane chessBoard = (GridPane) gamePane.getChildren().get(0);
            VBox messageBoard = (VBox) gamePane.getChildren().get(1);
            
            //Layout chess board within chessBoard
            fillInGameBoardSquares(gamePane, chessBoard, messageBoard, true);
        });
        MenuItem menuItemBlackPerpective = new MenuItem("Blacks's Perspective");
        menuItemBlackPerpective.setOnAction(event -> {
            //Identify nodes within tab
            HBox gamePane = (HBox)gameTabsPane.getSelectionModel().getSelectedItem().getContent();
            GridPane chessBoard = (GridPane) gamePane.getChildren().get(0);
            VBox messageBoard = (VBox) gamePane.getChildren().get(1);
            
            //Layout chess board within chessBoard
            fillInGameBoardSquares(gamePane, chessBoard, messageBoard, false);
        });

        //Add items to menu option
        menuItemDisplayOption.getItems().addAll(menuItemWhitePerpective, menuItemBlackPerpective);
        
        //Add menu option to menu bar
        menuBar.getMenus().addAll(menuGameOptions, menuItemDisplayOption);        
        
        
        //Add menu to mainPane
        mainPane.getChildren().add(menuBar);
        //----------------------------------------------------------------------

        //Create pages for differnet games: Fill gameTabsPane ------------------
        //Load any games in gameList
        if (gameList.getSize() > 0){
            for(int index = 0; index < gameList.getSize(); index++){
                // Ensure that the game's promotion method matches application assumptions by setting promotion method
                gameList.get(index).setPromotionMethod(promotionLambda);
                // Add a corresponding empty entry to selectedSquareList
                selectedSquareList.add(null);
                // Set up the game's tab display
                createGameBoard(index);
            }
        }
        //Otherwise create a new game
        else newGame();
        
        //Specify gameTabsPane closing policy
        gameTabsPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        //Add gameTabsPane to mainPane
        mainPane.getChildren().add(gameTabsPane);
        //----------------------------------------------------------------------
        //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        
        //Scene and Stage ++++++++++++++++++++++++++++++++++++++++++++++++++++++
        primaryStage.setTitle("Chess"); //Name for application stage
        //Add mainPane to the mainScene
        Scene mainScene = new Scene(mainPane, 800, 600);
        //Add style sheet to mainScene
        mainScene.getStylesheets().add("Styles.css");
        //Add mainScene to primaryStage
        primaryStage.setScene(mainScene);
        //Display primaryStage
        primaryStage.show();
        //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        //======================================================================

    }
    @Override
    public void stop(){
        /* Serialiazable Lambda Note
        The reference to promotionChoice[0] is not serializable, so to allow
        ChessGame to be serializable (and therefore be saved using ObjectOutputStream, 
        we need to change the promotion method to a serializable lambda function.
        
        The following is a serializable lambda function
        (PromotionSelection & Serializable) pawn -> {return 'Q';}
        */
        //Change the promotion method for every game to make them serializable
        for(int index = 0; index < gameList.getSize(); index++){
            gameList.get(index).setPromotionMethod((PromotionSelection & Serializable) pawn -> {return 'Q';});
        }
        //Save game list
        gameList.save();
    }

    // Methods to handle application updates
    //Menu Options
    private void deleteGame(){
        //Find index of deleted game
        int deletedIndex = gameTabsPane.getSelectionModel().getSelectedIndex();

        // Remove corresponding gameList/selectedSquareList index
        gameList.remove(deletedIndex);
        selectedSquareList.remove(deletedIndex);
        // Remove tab from gameTabsPane
        gameTabsPane.getTabs().remove(deletedIndex);

        // Change game ids for all tabs with indexes above the deleted index
        gameTabsPane.getTabs().forEach(tab -> {
            // Get tab index (saved in tab content's ID)
            int currIndex = Integer.parseInt(tab.getContent().getId());
            // For all the values above the deleted page, lower ID nubmer by one
            if (currIndex > deletedIndex) {
                tab.getContent().setId("" + (currIndex - 1));
            }
        });
    }
    private void newGame() {
        //Create a new chess game with the appropriate promotion method
        gameList.add(new ChessGame( promotionLambda ));
        //Add a corresponding entry in selectedSquareList
        selectedSquareList.add(null);
        
        //Set up  the display for the new game
        createGameBoard(gameList.getSize()-1);
        
        //Change tab pane selection
        gameTabsPane.getSelectionModel().select(gameList.getSize()-1);
    }
    
    //Creation of game display
    private void createGameBoard(int gameListIndex){
        //General Layout
        /* Layout Image: chessAppLayout.jpg
        gamePane (HBox) will contain everything needed for a single game
        chessBoard (GridPane) will make up the visual chess board
        messageBoard (VBox) will provide additional feedback to users
         */
        
        //Create panes for general layout ======================================
        //Create gamePane
        HBox gamePane = new HBox();
        gamePane.setId("" + gameListIndex);
        //Create chessBoard
        GridPane chessBoard = new GridPane();
        chessBoard.getStyleClass().add("chessBoard");
        //Create messageBoard
        VBox messageBoard = new VBox();
        messageBoard.getStyleClass().add("messageBoard");
        
        //Add chessBoard and messageBoard to gamePane
        gamePane.getChildren().addAll(chessBoard, messageBoard);
        //======================================================================
        
        //Layout chess board within chessBoard
        fillInGameBoardSquares(gamePane, chessBoard, messageBoard, true);
        //Create
        Tab newTab = new Tab("game " + (String.valueOf(gameListIndex + 1)));
        newTab.setContent(gamePane);
        gameTabsPane.getTabs().add(newTab);
    }
    private void fillInGameBoardSquares(HBox gamePane, GridPane chessBoard, VBox messageBoard, boolean perspective){
        //Clear chessBoard display
        chessBoard.getChildren().clear();
        //Layout chess board within chessBoard =================================
        for (int row = 0; row < 8; row++) {
            //Create notation labels for the board -----------------------------
            //Collumn labels:   a, b, c, d, e, f, g, h
            Label colLabel = new Label(String.valueOf((char) ('a' + row))); //Create label
            chessBoard.add(colLabel, (perspective)? row + 1 : 8 - row, 8); //Add label to chessBoard
            GridPane.setHalignment(colLabel, HPos.CENTER); //specify label alignment

            //Row labels:       8, 7, 6, 5, 4, 3, 2, 1
            Label rowLabel = new Label(String.valueOf(8 - row)); //Create label
            chessBoard.add(rowLabel, 0, (perspective)? row : 7 - row); //Add label to chessBoard
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
                square.setOnMouseClicked(event -> boardInteraction(gamePane, chessBoard, messageBoard, square));

                //Add square to chessBoard
                chessBoard.add(square, (perspective)? col + 1 : 8 - col, (perspective)? 7 - row : row);
            }
            //------------------------------------------------------------------
        }
        
        //Fill chessBoard with game's current state
        updateDisplay(chessBoard, messageBoard);
    }
    //Updating board display
    public void updateDisplay(GridPane chessBoard, VBox messageBoard) {
        //Ches current game from gameList
        ChessGame game = gameList.get(Integer.parseInt(chessBoard.getParent().getId()));
        
        //Update ChessBoard ====================================================
        //Update each square in chessBoard to reflect game's condition
        /*
        Note: we only need to modify squares (not labels), and all squares are
        created from the BoarderPane class, so we can modify all BoarderPane
        children of chessBoard.
        */
        chessBoard.getChildren().forEach(child -> {
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
        
        //Update messageBoard ==================================================
        //Clear messageBoard
        messageBoard.getChildren().clear();
        //Check for end conditions
        if (game.getGameComplete()) {
            messageBoard.getChildren().add(new Label(game.checkEnd()));
        }
        else { //Game is not over
            //Check for check
            if (game.checkAttack()) {
                messageBoard.getChildren().add(new Label("Check!"));
            }
            //Output 'player turn' message
            messageBoard.getChildren().add(new Label(((game.getPlayerTurn()) ?
                    "White" : "Black") + "'s turn."));
        }
        //======================================================================
    }
    //Event handling for board interactions
    private void boardInteraction(HBox gamePane, GridPane chessBoard, VBox messageBoard, BorderPane square) {
        //System.out.println("DEBUG: ID-" + square.getId() + ", Parent:" + square.getParent().getId());
        
        //Get game
        int gameIndex = Integer.parseInt(gamePane.getId());
        ChessGame game = gameList.get(gameIndex);
        
        //Check that the game is still going
        if (!game.getGameComplete()){
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
            chessBoard.getChildren().forEach(gridContent -> {
                gridContent.getStyleClass().remove("selected");
                gridContent.getStyleClass().remove("possibleMove");
            });
            
            //Path 1: The user selects a piece to move =========================
            /*Qualifications:
                - a piece of the correct color has been selected
            */
            if (localPiece != null //A piece has been selected
                    //Check that piece color matches players turn's color
                    && localPiece.getColor() == game.getPlayerTurn()
                    ){ 
                //Select Square
                //Add square to selectedSquareList at the corresponding index
                selectedSquareList.set(gameIndex, square.getId());
                //Signify selection with style class
                square.getStyleClass().add("selected");

                //Display all possible moves for the selected piece
                //Get list of possible moves
                ArrayList<String> possibleMoves = game.getMoveListForLocalPiece(square.getId());
                //Add a style class to each valid move
                chessBoard.getChildren().forEach(gridContent -> {
                    if (possibleMoves.contains(gridContent.getId())) {
                        gridContent.getStyleClass().add("possibleMove");
                    }
                });
            }
            //==================================================================
            //Path 2: The user selects a square to move to =====================
            /*Qualifications:
                - A 'mover' piece has been selected
                    (i.e., the corresponding entry in selectedSquareList is set)
            */
            else if (selectedSquareList.get(gameIndex) != null ){ //There is a selected piece
                //Check for promotion move (promotions require an extra prompt for piece selection)
                if (game.getLocalPiece(selectedSquareList.get(gameIndex)) instanceof Pawn pawn //'mover' piece is a pawn
                        && (square.getId().contains("1") || square.getId().contains("8")) //pawn is moving to 1st or 8th rank
                        && validMove //the move is valid (this prevents prompt display for illegal promotion moves)
                        ){
                    //Handle promotion details
                    /*Note: promote calls playerMove(gameIndex, chessBoard, messageBoard, square)
                    just like the else's statement, but it first requires for the
                    selection of a piece.*/
                    promote(pawn, gameIndex, chessBoard, messageBoard, square);
                }
                else playerMove(gameIndex, chessBoard, messageBoard, square);
            }
            //==================================================================
        }
    }
    
    //Handling Player Moves
    public void playerMove(int gameIndex, GridPane chessBoard, VBox messageBoard, BorderPane square){
        //Get current ChessGame
        ChessGame game = gameList.get(gameIndex);
        //Preform gameTurn: gameTurn will return true if it is a valid move
        //If the game move is valid
        if (game.gameTurn(selectedSquareList.get(gameIndex), square.getId())){
            //Update the board display
            updateDisplay(chessBoard, messageBoard);
            //Deselect square
            selectedSquareList.set(gameIndex, null);
        }
        //If the game move is Illegal, output error message
        else{
            messageBoard.getChildren().add(new Label("Illegal move: try again."));
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
            if (promotionChoice[0]!= '0')
                playerMove(gameIndex, chessBoard, messageBoard, square);
        });
                
        //Add form elements to messageBoard
        messageBoard.getChildren().addAll(rbQueen, rbRook, rbBishop, rbKnight, btnSubmit);
        // ---------------------------------------------------------------------
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}