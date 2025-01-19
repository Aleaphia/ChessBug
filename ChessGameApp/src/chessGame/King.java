package chessGame;

import java.util.*;

public class King extends Piece {
    //Methods===================================================================
    //Constructors--------------------------------------------------------------
    King(boolean colorIsWhite, int row, int col){
        super(colorIsWhite, row, col);
    }
            
    //Overridden Abstract Methods-----------------------------------------------    
    @Override
    public ArrayList<int[]> getAttackList(ChessGame game){
        ArrayList<int[]> output = new ArrayList<>();
        
        //Kings can move one square in any direction
        /*Note that the following for loops will check 9 squares (3*3),
        including the square occupied by the king. However, that middle square is
        skipped using an if statement.
        */
        for (int upOrDown = -1; upOrDown < 2; upOrDown++){
            for (int leftOrRight = -1; leftOrRight < 2; leftOrRight ++){
                //Skip middle square
                if (upOrDown == 0 && leftOrRight == 0){
                    leftOrRight++;
                }
                //Get cordiates for 'to' square
                int toRow = row + upOrDown;
                int toCol = col + leftOrRight;
                
                //check location exists
                if (game.checkSquare(toRow, toCol)){
                    //check is empty
                    if (game.getLocalPiece(toRow, toCol) == null){
                        int[] curr = {toRow, toCol};
                        output.add(curr);
                    }
                    //check if inhabitant is an enemy
                    else if (game.getLocalPiece(toRow, toCol).getColor() != color){
                        int[] curr = {toRow, toCol};
                        output.add(curr);
                    }
                }
            }
        }
        
        return output;
    }
    @Override
    public char getSymbol(){
        return 'K';
    }
    
    //Overriden Non-abstract Methods--------------------------------------------
    //  Override getMoveList to add castle moves
    @Override
    public ArrayList<int[]> getMoveList(ChessGame game){
        ArrayList<int[]> output = new ArrayList<>();
        //Get attack moves
        output.addAll(getAttackList(game));
        
        //Check for castle moves
        if (!hasMoved //King has not moved
                && !game.checkAttack() //King is not in check
                ){
            //Two possible directions for castling
            for (int leftOrRight = -1; leftOrRight < 2; leftOrRight+=2){
                //Check if the castle row is empty (castle is not valid it there is a piece in the way)
                boolean emptyRow = true;
                int checkCol;
                //Check each square in row between king and rook
                for (checkCol = col + leftOrRight; checkCol > 0 && checkCol < 7; checkCol += leftOrRight){
                    //Check if square is empty
                    if (game.getLocalPiece(row, checkCol) != null){
                        emptyRow = false;
                        break;
                    }
                }
                //Validate castle move
                if (emptyRow //Row is empty
                        && game.getLocalPiece(row, checkCol) != null // The rook square is not empty
                        && !game.getLocalPiece(row, checkCol).getHasMoved() //The rook has not moved
                        && !game.checkAttack(row, col + leftOrRight, !color) //There is no attacks on the passed over square
                        ){
                    //Add castle moves
                    int[] curr = {row, col + (2 * leftOrRight)};
                    output.add(curr);
                }
            }
        }
        
        //Remove moves that result in being in check
        removeCheckMoves(game, output);
        
        return output;
    }
}