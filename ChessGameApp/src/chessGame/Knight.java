package chessGame;

import java.util.*;

public class Knight extends Piece {
    //Methods===================================================================
    //Constructors--------------------------------------------------------------
    Knight(boolean colorIsWhite, int row, int col){
        super(colorIsWhite, row, col);
    }
            
    //Overridden Abstract Methods-----------------------------------------------    
    @Override
    public ArrayList<int[]> getAttackList(ChessGame game){
        //List to store moves
        ArrayList<int[]> output = new ArrayList<>();
        
        //Knights have 8 possible moves from 2^3 combinations
        for (int upOrDown = -1; upOrDown < 2; upOrDown += 2){ //will run through twice (-1,1)
            for (int leftOrRight = -1; leftOrRight < 2; leftOrRight += 2){ //will run through twice (-1,1)
                for (int oneOrTwo = 1; oneOrTwo <= 2; oneOrTwo++ ){
                    //Generate possible move
                    int toRow = row + (oneOrTwo * upOrDown);
                    int toCol = col + ((2 - (oneOrTwo + 1)%2) * leftOrRight);
                    
                    //check of both integers were within chess board range
                    if (game.checkSquare(toRow, toCol)
                            &&(
                            //Check if square is open
                            game.getLocalPiece(toRow, toCol) == null
                            //Or check if piece is opposite color
                            || game.getLocalPiece(toRow, toCol).getColor() != this.color
                            )){
                        //Add square to list
                        int[] currSquare = {toRow, toCol};
                        output.add(currSquare);
                    }
                }//end of oneOrTwo
            }//end of leftOrRight
        }//end of upOrDown
        return output;
    }
    @Override
    public char getSymbol(){
        return 'N';
    }
}