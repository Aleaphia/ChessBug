/*
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package chessBug.game;

import chessGame.*;

import java.io.*;
import java.util.*;

public class GameModel {
    //Chess game state
    private int turnNum = 0;
    private Boolean playerColor; //true is white; flase is black
    //Promotion variables
    private char[] promotionChoice = new char[1]; //char reference that can be modified by event handling lambda functions
    private PromotionSelection promotionLambda = (PromotionSelection & Serializable) pawn -> {
        return promotionChoice[0];
    };//Use promotionChoice to determine new piece
    private ChessGame game = new ChessGame(promotionLambda);
        
    //Constructors
    public GameModel(boolean playerColor) { //New game
        this.playerColor = playerColor; //Determine player color
    }
    public GameModel(boolean playerColor, ArrayList<String> moveList){
        this(playerColor);
        //Get moves from database
        loadGame(moveList);
    }
    
    //Getter/Setter Methods
    public Boolean getGameComplete(){return game.getGameComplete();}
    public Piece getLocalPiece(String square){return game.getLocalPiece(square);}
    public int getTurnNumber(){return turnNum;}
    public ArrayList<String> getMoveListForLocalPiece(String square){return game.getMoveListForLocalPiece(square);}
    public Boolean isPlayerTurn(){return (playerColor && game.getPlayerTurn()) || (!playerColor && !game.getPlayerTurn());}
    public Boolean getPlayerTurn(){return game.getPlayerTurn();}
   
    //Other Methods
    /** LoadGame - loads current game state
     * @param moveList - list of previous moves in coordinate notation (e.g., e2e4 or e7e8Q)
     */
    private void loadGame(ArrayList<String> moveList){
        //make previous moves
        for(String move : moveList){
            //Get starting and ending square
            String from = move.substring(0,2);
            String to = move.substring(2,4);
            //System.out.println( "Debug: From: " + from + "\tTo: " + to);
            //Promotion moves
            if (move.length() == 5) // Only promotion moves have a 5th character
                promotionChoice[0] = move.charAt(4);
            
            //Make move
            game.gameTurn(from, to);
        }
    }
    
    /**MakePlayerMove  - updates chess game and model information in response to a move
     * 
     * @param notation - Coordinate notation in the format e2e4 or e2e1Q (promotion)
     * @return - return true when a valid move in input and the game state is updated
     */
    public Boolean makePlayerMove(String notation){
        //Parse notation move
        String startSquare = notation.substring(0,2);
        String endSquare = notation.substring(2,4);
        promotionChoice[0] = (notation.length() == 5)? notation.charAt(4) : '0'; //'0' is used as a null value

        //Preform gameTurn: gameTurn will return true if it is a valid move
        //If the game move is valid
        if (game.gameTurn(startSquare, endSquare)){
            if (!game.getPlayerTurn()) //iterate turn after white moves (i.e., currently black's turn)
                turnNum++;
            return true;
        }
        else 
           return false;
    }

}
