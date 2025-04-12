package chessGame;

import java.util.*;

import chessBug.network.Client;
import chessBug.network.Match;
import chessBug.network.NetworkException;
import chessBug.network.User;

import java.io.Serializable;

public class ChessGame implements Serializable{
    static final long serialVersionUID = 4232L;
    
    //Attributes================================================================
    //Constants
    private final int BOARD_LENGTH = 8;
    //Variables
    private final Piece[][] board = new Piece[BOARD_LENGTH][BOARD_LENGTH];
    private boolean playerTurn; //true is white; false is black
    // Tracking variables for special rules
    private final int[] ghostPawn = {-1,-1}; //refers to a square jumped over by a pawn; used to determine legality of en passant.
    private int fiftyMoveRuleCounter = 0; // Move counter that ends game if it reaches 100 (50 moves per player)
    private HashMap<String, Integer> positionCounter = new HashMap<>(); //Counts the number of times a position has occurred
    private boolean gameComplete = false; //Game completion status
    private PromotionSelection promotionMethod; //Determines how promotions should be dealt with
    
    //Track kings
    private final Piece[] kings = new Piece[2];
    
    //Methods===================================================================
    //Constructors
    public ChessGame(PromotionSelection promotionMethod){
        //Create Starting position
        this.setStartingState();
        //Set player turn to white
        playerTurn = true;
        //Save kings
        kings[0]= board[7][4];
        kings[1]= board[0][4];
        
        //Save promotion method
        this.promotionMethod = promotionMethod;
        
        //Start position counter with starting position
        updatePositionCounter();
    }
            
    //SetUp---------------------------------------------------------------------
    private void setStartingState(){
        for (int col = 0; col < BOARD_LENGTH; col++){
            for (int blackOrWhite = 0; blackOrWhite < 2; blackOrWhite++){
                boolean color = blackOrWhite == 1;
                int pawnRank = (color)? 1 : 6;
                int backRank = (color)? 0 : 7;
                
                //Create pawns on every collumn
                board[pawnRank][col] = new Pawn(color, pawnRank, col);
                //Add back rank piece
                switch (col){
                    case 0, 7 -> { board[backRank][col] = new Rook(color, backRank, col);}
                    case 1, 6 -> {board[backRank][col] = new Knight(color, backRank, col);}
                    case 2, 5 -> {board[backRank][col] = new Bishop(color, backRank, col);}
                    case 3 -> {board[backRank][col] = new Queen(color, backRank, col);}
                    case 4 -> {board[backRank][col] = new King(color, backRank, col);}
                }
            }
        }
    }
    
    //Input---------------------------------------------------------------------
    //  Follows cycle of game
    public void gameCycle(Scanner scnr){
        //Print starting state
        printBoard();
        
        //Cycle of player turn
        while (!gameComplete){
            //Get player move
            String[] input = inputMove(scnr);
            //Update game
            if (gameTurn(input[0], input[1]))
                printBoard(); //Only need to print board if the game turn was valid
        } 
    }
    public boolean gameTurn(String stFrom, String stTo){
        if (!gameComplete){
            //Locations to save squares
            int[] from = ChessGame.decodeNotation(stFrom);
            int[] to = ChessGame.decodeNotation(stTo);
            //Check that move is legal
            if (checkValidMove(from[0], from[1], to[0], to[1])){
                //Update board conditions
                //Make move
                makeFinalMove(from[0], from[1], to[0], to[1]);
                //Check for promotion: pawn must be replaced by a new piece
                if(board[to[0]][to[1]] instanceof Pawn //moved piece is a pawn
                        && (to[0] == 7 || to[0] == 0) //pawn moved to back rank
                        ){
                    //Conosle selction for new piece
                    char newPiece = promotionMethod.selectNewPiece((Pawn)board[to[0]][to[1]]);
                    board[to[0]][to[1]] = ((Pawn)board[to[0]][to[1]]).promote(newPiece);
                }
                //Switch player turn
                playerTurn = !playerTurn;
                //Update positionCounter with board position
                updatePositionCounter();
                /*
                System.out.println("DEBUG");
                positionCounter.forEach((x,y)-> System.out.println(x + ": " + y));
                */
                //Check end states
                gameComplete = checkEnd()!= null; //When checkEnd() returns a non-null value, the game has ended
                if (!gameComplete && checkAttack()){
                    System.out.println("Check!");
                }
                return true; //successful turn
            }
        }
        System.out.println(false);
        return false; //unsuccessful turn
    }
    //  Validate move input
    private String[] inputMove(Scanner scnr){
        String[] output = new String[2];
        String player = (playerTurn)? "White" : "Black";
        
        while (true){
            //Promt user input
            System.out.print( player + " player, enter your move: ");
            String input = scnr.next().trim();
            scnr.nextLine();
            
            //Validate input
            if (input.length() == 5){ //XX-XX format requries five letters
                //Get decoded notation
                output[0] = input.substring(0,2);
                output[1] = input.substring(3);

                //Check that squares are valid
                if (checkSquare(output[0])
                        && checkSquare(output[1])){
                    return output;
                }
            }        
            else{
                System.out.println("Invalid input. Please enter move in cordinates notation (ex. e2-e4).");
            }
        }
    }
    
    //Internal------------------------------------------------------------------
    //Set methods
    //  Set location of Ghost Pawn (en Passant)
    private void setGhostPawn(int row, int col){
        ghostPawn[0] = row;
        ghostPawn[1] = col;
    }
    //  Change location of piece in board and return a list of moved pieces
    private HashMap<String, Piece> movePiece(int fromRow, int fromCol, int toRow, int toCol){
        //Location to store affected pieces for output
       HashMap<String, Piece> affectedSquares = new HashMap<>();

        //Add 'from' and 'to' squares to affectedSquares
        affectedSquares.put(encodeNotation(fromRow,fromCol), board[fromRow][fromCol]);
        affectedSquares.put(encodeNotation(toRow,toCol), board[toRow][toCol]);
        
        //Identify moving piece
        Piece mover = board[fromRow][fromCol];
        //Move piece on the board
        board[toRow][toCol] = mover;
        board[fromRow][fromCol] = null;
        //Update mover's internal location
        mover.setLocation(toRow, toCol);
        
        //Check for special moves that require extra board changes
        //  En passant; requires removal of captured pawn
        if (mover.getClass()== Pawn.class //pawn move
                && toRow == ghostPawn[0] && toCol == ghostPawn[1]){ //'to' square matches ghostPawn
            //Get location of captured pawn
            int ghostPawnOffset = (playerTurn)? -1:1;
            //Add captured pawn to affectedSquares
            affectedSquares.put(encodeNotation(toRow + ghostPawnOffset, toCol), board[toRow + ghostPawnOffset][toCol]);
            //Remove captured pawn from board
            board[toRow+ ghostPawnOffset][toCol] = null;
        }
        //  Castle; requires extra rook move
        if (mover.getClass()== King.class //king move
                && Math.abs(toCol- fromCol) == 2 //king moved two squares
                ){
            //Determine direction of castle
            boolean isKingSideCastle = toCol - fromCol > 0;
            //Get rook 'from' and 'to' collumns
            int rookFromCol = (isKingSideCastle)? 7 : 0 ;
            int rookToCol = (fromCol+toCol)/2;
            affectedSquares.putAll(movePiece(fromRow, rookFromCol, toRow, rookToCol));
        }
        
        return affectedSquares;
    }
    //  Make final move for player turn
    private void makeFinalMove(int fromRow, int fromCol, int toRow, int toCol){
        //Identify moving piece
        Piece mover = board[fromRow][fromCol];
        
        //Check for capture
        boolean isCapture = board[toRow][toCol] != null;
        
        //Make move and get list of moved pieces
        HashMap<String, Piece> affectedSquares = movePiece(fromRow, fromCol, toRow, toCol);
        //mark any affected pieces as having moved
        affectedSquares.forEach((square, piece) -> {if(piece != null) piece.move();});
        //Increase 50 move rule counter
        fiftyMoveRuleCounter++;
        //Reset ghost pawn
        setGhostPawn(-1,-1);
        
        //Check for pawn moves that require extra board updates
        if (mover.getClass()== Pawn.class){ //pawn move
            //Restart fifty move rule coutner whenever a pawn is moved
            fiftyMoveRuleCounter = 0;
            //Double jump: must set ghost pawn to jumped over square
            if (Math.abs(toRow-fromRow) == 2){ //pawn moves two squares
                setGhostPawn((toRow+fromRow)/2, fromCol);
            }
            /*//Promotion: pawn must be replaced by a new piece
            else if (toRow == 7 || toRow == 0){
                board[toRow][toCol] = ((Pawn)mover).promote(scnr);
            }*/
        }
        //Restart fifty move rule counter whenever a piece is captured
        if (isCapture) fiftyMoveRuleCounter = 0;
    }
    // Add current position to threeMoveRepCounter
    private void updatePositionCounter(){
        //Create string that represents board position
        //Include player turn
        String currPosition = getPosition();
        
        //Add or update positionCounter with current position
        int occurrences = 1;
        if (positionCounter.containsKey(currPosition)){
            occurrences = positionCounter.get(currPosition) + 1;
        }
        positionCounter.put(currPosition, occurrences);
    }
    public String getPosition(){
        //Include player turn
        String currPosition = (playerTurn)? "w" : "b";
        //all board square contents
        for(int row = 0 ; row < BOARD_LENGTH; row++){
            for(int col = 0; col < BOARD_LENGTH; col++){
                currPosition += (board[row][col] != null)? board[row][col]: "_";
            }
        }
        
        return currPosition;
    }
    
    //Converting notation format
    public static int[] decodeNotation(String square) {
        int row = square.charAt(1)-'1';
        int col = square.charAt(0)-'a';
        int[] output = {row, col};
        return output;
    }
    public static String encodeNotation(int row, int col){
        String output = "";
        output = output.concat("" + (char)('a' + col));
        output = output.concat("" + (row + 1));
        return output;
    }
     
    //Output--------------------------------------------------------------------
    //Print method
    public void printBoard(){
        System.out.println("\n  ----------------------------------------"); //Top of board
        for(int row = BOARD_LENGTH - 1; row >= 0; row--){
            // Print row numbers along left side of board
            System.out.print(row+1 + " | ");
            // Print out contents of rows
            for(Piece square : board[row]){
                String output = (square == null)? "  " : square.toString();
                System.out.print(output + " | ");
            }
            System.out.println("\n  ----------------------------------------"); //Bottom of board
        }
        // Print collumn letters along bottom of board
        System.out.println("    a    b    c    d    e    f    g    h");
 
    }
    
    //Get methods
    public Piece getLocalPiece(int row, int col){
        return board[row][col];
    }
    public Piece getLocalPiece(String square){
        int[] cordinates = ChessGame.decodeNotation(square);
        return getLocalPiece(cordinates[0], cordinates[1]);
    }
    public ArrayList<String> getMoveListForLocalPiece(String square){
        //Get local piece
        Piece localPiece = getLocalPiece(square);
        
        if(gameComplete)
            return new ArrayList<>();
        
        //Create lcoation to store notation encoded moves
        ArrayList<String> moveListDecoded = new ArrayList<>();
        //Get move list and convert to notaiton encoding
        localPiece.getMoveList(this).forEach( move -> {
            moveListDecoded.add(encodeNotation(move[0], move[1]));
        });
        
        return moveListDecoded;
    }
    public boolean getPlayerTurn(){
        return playerTurn;
    }
    public boolean getGameComplete(){
        return gameComplete;
    } 
    //Set methods
    public void setPromotionMethod(PromotionSelection promotionMethod){
        this.promotionMethod = promotionMethod;
    }
    public void endGame(){gameComplete = true;}
    
    //Boolean methods that check to Board states
    //  Check that a square exists
    public boolean checkSquare(int row, int col){
        //Checks if a square is within the board
        return row >= 0 && row < BOARD_LENGTH && col >= 0 &&  col < BOARD_LENGTH;
    }
    public boolean checkSquare(String square){
        return square.length() == 2 //square names are length 2
                && (square.charAt(0) >= 'a' && square.charAt(0) <= 'h') //first char is between a-h
                && (square.charAt(1) >= '1' && square.charAt(1) <= '8') //second char is between 1-8
                ;
    }
    //  Check location of Ghost Pawn (en passant)
    public boolean checkGhostPawn(int row, int col){
        return ghostPawn[0] == row && ghostPawn[1] == col;
    }
    //  Check input is a valid move
    public boolean checkValidMove(int fromRow, int fromCol, int toRow, int toCol){
        //Check that a move occured
        Piece mover = board[fromRow][fromCol];
        try{
            //A piece of the correct color must be selected
            if (mover == null || mover.getColor()!= playerTurn){
                throw new Exception("Illegal move: no "
                    +((playerTurn)? "white" : "black") //print player color
                    + " piece selected");
            }
            //The piece must be able to legally move 'to' square
            if (!mover.validateMove(this, toRow, toCol)){
                throw new Exception("Illegal move: " + mover + " on "+
                    encodeNotation(fromRow,fromCol) +" cannot move to " +
                    encodeNotation(toRow,toCol));
            }
        }
        //Print out error messages for invalid moves
        catch (Exception e){
            System.out.println(e.getMessage());
            //If illegal move was made, list legal moves for mover
            if (mover != null && mover.getColor() == playerTurn){
                //Get list
                ArrayList<int[]> moveList = mover.getMoveList(this);
                //Print possible moves
                System.out.println("Possible moves for " + mover + " on " +
                        encodeNotation(mover.getRow(),mover.getCol()) + ", " + moveList.size() + " options");
                moveList.forEach((x)->{System.out.print(ChessGame.encodeNotation(x[0], x[1]) + " ");});
                System.out.println();
            }
            return false;
        }
        //Otherwise return true
        return true;
    }
    //  Check that a move does not end in check
    public boolean checkMoveEndsInCheck(int fromRow, int fromCol, int toRow, int toCol){
        boolean output = false;
        //Make move and get list of affected sqares
        HashMap<String, Piece> affectedSquares = movePiece(fromRow, fromCol, toRow, toCol);
        //Check for check
        if (checkAttack()){
            output = true;
        }
        affectedSquares.forEach((square, piece) -> {
            //Get row and col integers for square
            int[] squareCoordinates = decodeNotation(square);
            //Set the board square element to the piece
            board[squareCoordinates[0]][squareCoordinates[1]] = piece;
            //Set the piece's internal location to the square's row and col
            if (piece != null)
                piece.setLocation(squareCoordinates[0], squareCoordinates[1]);
        });

        return output;
    }
    //End conditions
    //  Check end state
    public String checkEnd(){
        /* Chess end states
        Checkmate: player has no moves and is in check
        Draws:
            Stalemate: player has no moves and is not in check
            Insufficient material: both players lack the material to checkmate
            Fifty Move Rule: no pawn has been pushed or piece captured for the last 50 moves
            Three Move Repetition: The same position has occured three times throughout the game
        */
        String outputMsg = null;
        String drawType = null;
        if (!checkMoves()){ //Player has no moves -> either checkmate or stalemate
            if (checkAttack()){ //Player is in check -> checkmate
                outputMsg = ("Game over: Checkmate, "
                    +((playerTurn)? "black" : "white")
                    + " wins!");
            }
            else{// Player is not in check -> stalemate
                drawType = "Stalemate";
            }
        }
        //Other draw conditions
        else if (!checkMaterial(playerTurn) && !checkMaterial(!playerTurn))
            drawType = "insufficient material";
        else if (fiftyMoveRuleCounter >= 100)
            drawType = "fifty move rule";
        else if (positionCounter.containsValue(3))
            drawType = "three move repetition";        
        //Output draw message
        if (drawType != null){
            outputMsg = "Game over: Draw by " + drawType;
        }
        
        //No end condition found
        return outputMsg;
    }
    //  Check if square is under attack
    //      Without parameters, default to check if player's king is under attack (check)
    public boolean checkAttack(){
        //Convert boolean to int
        int currPlayer = (playerTurn)? 1 : 0;
        //Check attack on the king's location 
        return checkAttack(kings[currPlayer].getRow(), kings[currPlayer].getCol(), !playerTurn);
    }
    public boolean checkAttack(int targetRow, int targetCol, boolean attackerColor){
        //Look through every square
        for(int row = 0 ; row < BOARD_LENGTH; row++){
            for(int col = 0; col < BOARD_LENGTH; col++){
                //Find squares with attacker pieces
                if (board[row][col]!= null //square is inhabited
                        && board[row][col].color == attackerColor //piece is attacker's color
                        ){
                    //Check if target square is in attack list
                    ArrayList<int[]> attackList = board[row][col].getAttackList(this);
                    for(int[] attack : attackList){
                        if (targetRow == attack[0] &&  targetCol == attack[1]){
                            return true;
                        }
                    }
                }
            }
        }
        //Target square is not under attack from any of attacker's pieces
        return false;
    }
    //  Check for sufficient mating material
    private boolean checkMaterial(boolean colorCheck){
        /*Sufficient mating material: minimum pieces combo to force mate
            1 Queen
            1 Rook
            1 Pawn (can promote into a queen or rook)
            2 Bishops
            1 Bishop + 1 Knight
            3 Knights
        */
        
        //Track Bishop and Knight counts
        int bishopCount = 0;
        int knightCount = 0;
        
        //Go through every square
        for (int row = 0; row < BOARD_LENGTH; row++){
            for (int col = 0; col < BOARD_LENGTH; col++){
                //Check inhabitant
                if (board[row][col]!= null // Square has a piece
                        && board[row][col].getColor() == colorCheck //Piece belongs to correct color
                        ){ 
                    //Check the piece's class
                    switch (board[row][col].getClass().getSimpleName()){
                        //If there is  a pawn, queen, or rook, player has sufficent material
                        case ("Pawn"), ("Queen"), ("Rook") -> {return true;}
                        // Iterate bishop or knight count
                        case ("Bishop") -> bishopCount++;
                        case ("Knight") -> knightCount++;
                    }
                }
            }
        }
        
        //Check for bishop/knight mating combos
        return (bishopCount >= 2) // 2 bishops
                || (knightCount >= 3) // 3 knights
                || (bishopCount >= 1 && knightCount >= 1); // 1 bishop and 1 knight
    }
    //  Checks if a player has moves left
    private boolean checkMoves(){
        //Look through every square
        for (int row = 0; row < BOARD_LENGTH; row++){
            for (int col = 0; col < BOARD_LENGTH; col++){
                //Check for a piece of the player's color that has legal moves
                if (board[row][col]!= null //Square has piece
                        && board[row][col].getColor()== playerTurn // piece is the player's color
                        && !board[row][col].getMoveList(this).isEmpty() //piece has moves
                        ){ 
                    return true;
                }
            }
        }
        return false;
    }
}
