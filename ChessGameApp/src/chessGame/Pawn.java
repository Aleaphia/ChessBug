package chessGame;

import java.util.*;

public class Pawn extends Piece {
    //Methods===================================================================
    //Constructors--------------------------------------------------------------
    Pawn(boolean colorIsWhite, int row, int col){
        super(colorIsWhite, row, col);
    }
    
    //Overridden Abstract Methods-----------------------------------------------    
    //  Returns list of squares a pawn is attacking
    @Override
    public ArrayList<int[]> getAttackList(ChessGame game){
        //List to store moves
        ArrayList<int[]> output = new ArrayList<>();
        //Pawns always move forward
        int toRow = row + getDirection();
        //Check for left or right attack
        for (int leftOrRight = -1; leftOrRight < 2; leftOrRight += 2){
            int toCol = col + leftOrRight;
            if (game.checkSquare(toRow, toCol) //square is on board
                    //Check that one of the capture types is true
                    &&(
                    //Check for regular captures
                    (game.getLocalPiece(toRow, toCol) != null //square not empty
                    && game.getLocalPiece(toRow, toCol).getColor() != this.color)//opposite color
                    //Or check for en passant
                    || game.checkGhostPawn(toRow, toCol)//ghost pawn matches attack square
                    )){
                //Add square to list
                int[] currSquare = {toRow, toCol};
                output.add(currSquare);
            }
        }
         
        return output;
    }
    //  Return pawn symbol
    @Override
    public char getSymbol(){
        return 'p';
    }
    
    //Overridden Non-abstract Methods-------------------------------------------
    //  Override getMoveList because pawns do not attack and move in the same pattern
    @Override
    public ArrayList<int[]> getMoveList(ChessGame game){
        //List to store moves
        ArrayList<int[]> output = new ArrayList<>();
        //Get attack moves
        output.addAll(getAttackList(game));
        
        //Generate non-attack moves
        //Check which direction the pawn is moving
        int forward = getDirection();
        //Check square the pawn is moving to
        int toRow = row + forward;
        if (game.checkSquare(toRow, col) //Square exists
                && game.getLocalPiece(toRow, col) == null //Square is empty
                ){
            //Insert current square into list
            int[] currSquare = {toRow, col};
            output.add(currSquare);
            //check for possible double move
            toRow += forward;
            if (!hasMoved //Pawn has not moved
                    && game.checkSquare(toRow, col) //Square exists
                    && game.getLocalPiece(toRow, col) == null //Square is empty
                    ){
                //Insert current square into list
                int[] currSquare2 = {toRow, col};
                output.add(currSquare2);
            }
        }
        
        //Remove moves that result in being in check
        removeCheckMoves(game, output);
        
        return output;
    }
    //  When pawns get to the other side of the boark they turn into N,B,Q,R
    public Piece promote(char newPiece){
        //Location for new piece
        Piece output;
        //Create new piece
        switch (newPiece){
            case ('Q') -> output = new Queen(color, row, col);
            case ('R') -> output = new Rook(color, row, col);
            case ('B') -> output = new Bishop(color, row, col);
            case ('N') -> output = new Knight(color, row, col);
            default -> output = new Queen(color, row, col);
        }
        //New piece 'hasMoved' for sake of move legality: update hasMoved
        output.move();
        //Return new piece
        return output;
    }
    
    //Get methods---------------------------------------------------------------
    //  Get the row direction a pawn in moving
    private int getDirection(){
        /*Note: Pawns are the only piece that cannot move backwards.
        To account for this behavior, the method getDirection is used to normalize
        pawn movements between players.*/
        return (color)? 1: -1;
    }
}