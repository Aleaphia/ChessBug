package chessGame;

import java.util.*;

public class Queen extends Piece {
    //Methods===================================================================
    //Constructors--------------------------------------------------------------
    Queen(boolean colorIsWhite, int row, int col){
        super(colorIsWhite, row, col);
    }
            
    //Overridden Abstract Methods-----------------------------------------------    
   @Override
    public ArrayList<int[]> getAttackList(ChessGame game){
        ArrayList<int[]> output = new ArrayList<>();
        //Add rook like moves
        output.addAll(Rook.getAttackList(game, row, col, color));
        //Add bishop like moves
        output.addAll(Bishop.getAttackList(game, row, col, color));
        
        return output;
    }
    @Override
    public char getSymbol(){
        return 'Q';
    }
}
