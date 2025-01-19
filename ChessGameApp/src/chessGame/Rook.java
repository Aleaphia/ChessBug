package chessGame;

import java.util.*;

public class Rook extends Piece {
    //Methods===================================================================
    //Constructors--------------------------------------------------------------
    Rook(boolean colorIsWhite, int row, int col){
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
        return 'R';
    }

    //Overloaded Methods--------------------------------------------------------
    //Static version of getAttackList to use in Queen
    public static ArrayList<int[]> getAttackList(ChessGame game, int row, int col, boolean color){
        ArrayList<int[]> output = new ArrayList<>();
        
        //Four directions for possible moves (2^2)
        for (int rowOrCol = 0; rowOrCol < 2; rowOrCol++){
            for (int direction = -1; direction < 2; direction += 2){
                //Store 'from' and 'to' square coordinates in format that allows interaction with for loop integer
                int[] fromSquare = {row, col};
                int[] toSquare = {row, col};
                int distance = 1; //track how far down the line we are looking
                //Check rank moves
                while(true){
                    toSquare[rowOrCol] = fromSquare[rowOrCol] + (distance * direction);
                    //Stop while loop if square is not on the board
                    if (!game.checkSquare(toSquare[0], toSquare[1])){
                        break;
                    }
                    //Add to list and continue if square is not occupied
                    else if (game.getLocalPiece(toSquare[0], toSquare[1]) == null){
                        int[] curr = {toSquare[0], toSquare[1]};
                        output.add(curr);
                    }
                    //Add to list and break if occupent is opponent
                    else if (game.getLocalPiece(toSquare[0], toSquare[1]).getColor() != color){
                        int[] curr = {toSquare[0], toSquare[1]};
                        output.add(curr);
                        break;
                    }
                    else{
                        break;
                    }
                    //interate distance
                    distance++;
                }
            }
        }
        return output;
    }
}