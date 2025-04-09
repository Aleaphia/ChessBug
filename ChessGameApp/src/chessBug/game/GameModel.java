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
    private final Boolean playerColor; //true is white; flase is black
    //Promotion variables
    private char[] promotionChoice = new char[1]; //char reference that can be modified by event handling lambda functions
    private final ChessGame game = new ChessGame((PromotionSelection & Serializable) pawn -> {return promotionChoice[0];});
        
    //Constructors
    public GameModel(boolean playerColor) { //New game
        this.playerColor = playerColor; //Determine player color
    }
    
    //Getter/Setter Methods
    public Boolean getGameComplete(){return game.getGameComplete();}
    public Piece getLocalPiece(String square){return game.getLocalPiece(square);}
    public int getTurnNumber(){return turnNum;} //Returns the number of full moves played (white + black = 1 move)
    public ArrayList<String> getMoveListForLocalPiece(String square){return game.getMoveListForLocalPiece(square);}
    public Boolean getPlayerTurn(){return game.getPlayerTurn();}
    public Boolean getPlayerColor(){return playerColor;}
    public String getEndMessage(){return game.checkEnd();}
    public String getPosition(){return game.getPosition();}
    public String getPostion(int index){return game.getPosition(index);}
    public void endGame(){game.endGame();}
   
    //Other Methods
    public void printBoard(){game.printBoard();} //Used for debugging
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
            turnNum++;
            return true;
        }
        else 
           return false;
    }
    
    public String convertToAlgebraic(String notation){
       return ""; 
    }
}
