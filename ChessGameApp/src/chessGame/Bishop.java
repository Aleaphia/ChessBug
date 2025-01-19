package chessGame;

import java.util.*;

public class Bishop extends Piece{
    //Methods===================================================================
    //Constructors--------------------------------------------------------------
    Bishop(boolean colorIsWhite, int row, int col){
        super(colorIsWhite, row, col);
    }
            
    //Overridden Abstract Methods-----------------------------------------------    
    @Override
    public ArrayList<int[]> getAttackList(ChessGame game){
        //Use static version of method to reduce redundancy
        ArrayList<int[]> output = getAttackList(game, row, col, color);
        return output;
    }
    @Override
    public char getSymbol(){
        return 'B';
    }
    
    //Overloaded Methods--------------------------------------------------------
    //Static version of getAttackList to use in Queen
    public static ArrayList<int[]> getAttackList(ChessGame game, int row, int col, boolean color){
        ArrayList<int[]> output = new ArrayList<>();
        
        //Four directions for possible moves (2^2)
        for (int upOrDown = -1; upOrDown < 2; upOrDown+=2){
            for (int leftOrRight = -1; leftOrRight < 2; leftOrRight+=2){
                int distance = 1; //track how far down the diagonal we are looking
                while(true){
                    int toRow = row + (distance * upOrDown);
                    int toCol = col + (distance * leftOrRight);
                    //Stop while loop if square is not on the board
                    if (!game.checkSquare(toRow, toCol)){
                        break;
                    }
                    //Add to list and continue if square is not occupied
                    else if (game.getLocalPiece(toRow, toCol) == null){
                        int[] curr = {toRow, toCol};
                        output.add(curr);
                    }
                    //Add to list and break if occupant is opponent
                    else if (game.getLocalPiece(toRow, toCol).getColor() != color){
                        int[] curr = {toRow, toCol};
                        output.add(curr);
                        break;
                    }
                    else{
                        break;
                    }
                    //iterate distance
                    distance++;
                }
            }
        }
        
        return output;
    }
}