
package chessGame;

import java.io.*;

public interface PromotionSelection extends Serializable{
    /*selectNewPiece : allows users to select the piece that a pawn will become
    upon promotion. The output of the function should be the notational
    representation of the new piece (e.g., 'Q', 'N', etc.)
    
    Parameters: Pawn pawn: the pawn that is being promoted
    */
    public char selectNewPiece(Pawn pawn);
}
