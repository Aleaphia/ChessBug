package chessGame;

import java.io.Serializable;
import java.util.*;

abstract public class Piece implements Serializable{
    static final long serialVersionUID = 4239L;
    
    //Class Attributes==========================================================
    //Constants-----------------------------------------------------------------
    //Color symbols corresponding to back (0) and white (1)
    protected final static char[] colorSymbols = {'b',' '};
    
    //Variables-----------------------------------------------------------------
    //Piece color
    protected boolean color; //white is 1, black is 0
    //Location on board
    protected int row;
    protected int col;
    //If the piece has previously moved in a game; used for determining pawn and king moves
    protected boolean hasMoved = false;
    
    //Methods===================================================================
    //Constructors--------------------------------------------------------------s
    Piece(boolean colorIsWhite, int row, int col){
        this.color = colorIsWhite;
        this.row = row;
        this.col = col;
    }
    //Abstract Methods----------------------------------------------------------
    //Returns a list of squares (int pairs) attacked by a piece
    abstract public ArrayList<int[]> getAttackList(ChessGame game);
    //Returns piece symbol
    abstract public char getSymbol();
    
    //Non-abstract Methods------------------------------------------------------
    //Set methods
    public void setLocation(int toRow, int toCol){
        row = toRow;
        col = toCol;
    }
    public void move(){
        hasMoved = true;
    }
    
    //Get methods
    public boolean getColor(){
        return color;
    }
    public int getRow(){
        return row;
    }
    public int getCol(){
        return col;
    }
    public boolean getHasMoved(){
        return hasMoved;
    }
    
    //Move list methods
    //  Validates that the current piece can legally move to a given square
    public boolean validateMove(ChessGame game, int toRow, int toCol){
        boolean isValid = false;
        //Get a list of all legal moves
        ArrayList<int[]> moveList = getMoveList(game);
        //Check list for input move
        for(int[] square : moveList){
            if (square[0] == toRow && square[1] == toCol){
                isValid = true;
                break;
            }
        }
        return isValid;
    }
    //  Creates a complete list of legal moves for a piece
    public ArrayList<int[]> getMoveList(ChessGame game){
        //Get list of possible attacks (all attacks are potential moves)
        ArrayList<int[]> attackList = getAttackList(game);
        //Remove moves that end with current color player in check
        removeCheckMoves(game, attackList);
        //Return list of all legal moves
        return attackList;
    }
    // Modifies a list to remove illegal moves that result in check
    protected void removeCheckMoves(ChessGame game, ArrayList<int[]> moveList){
        //Check each move in moveList
        for(int i = moveList.size()-1; i >= 0; i--){
            //Get move square
            int toRow = moveList.get(i)[0];
            int toCol = moveList.get(i)[1];
            //Check if move ends with current color player in check
            if(game.checkMoveEndsInCheck(row, col, toRow, toCol)){
                moveList.remove(i);
            }
        }
    }
    
    @Override
    public String toString(){
        //Determine color symbol
        int colorIndex = (color)? 1: 0;
        //Print color symbol with piece symbol
        return "" + colorSymbols[colorIndex] + getSymbol();
    }
}