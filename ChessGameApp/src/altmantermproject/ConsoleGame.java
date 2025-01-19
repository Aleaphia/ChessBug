package altmantermproject;

import chessGame.*;
import java.io.*;
import java.util.*;

public class ConsoleGame {
    public static boolean playAgain(Scanner scnr){
        while (true){
            //Prompt user
            System.out.print("Would you like to play another game? (y/n) ");
            //Handle input
            switch (scnr.nextLine().charAt(0)) {
                case 'y' -> {return true;}
                case 'n' -> {return false;}
                default -> System.out.print("Error: ");
            }
        }
    }
    public static char promote(Scanner scnr, Pawn pawn){
        //Location for new piece
        char output = 'Q';
        boolean validInput = false;
        
        while(!validInput){
            //Prompt user to input new piece type
            System.out.println( ((pawn.getColor())? "White" : "Black") + "'s pawn on "
                    + ChessGame.encodeNotation(pawn.getRow(), pawn.getCol()) + " has promoted.");
            System.out.print("Select a new piece: ");
            //Get input
            String input = scnr.next().toLowerCase();
            scnr.nextLine();
            //Read and validate input
            validInput = true;
            switch (input){
                case ("queen"), ("1"), ("q") -> output = 'Q';
                case ("rook"), ("2"), ("r") -> output = 'R';
                case ("bishop"), ("3"), ("b") -> output = 'B';
                case ("knight"), ("4"), ("n") -> output = 'N';
                //Input does not match valid cases
                default -> {
                    //Output error message
                    System.out.println("""
                                                       Invalid input, pawns can become...
                                                       1) Queen
                                                       2) Rook
                                                       3) Bishop
                                                       4) Kngiht""");
                    validInput = false;
                }
            }
        }
        return output;
    }
    
    public static void main(String[] args) {
        //Initialize scanner object
        Scanner terminalScnr = new Scanner(System.in);
        //Loop through games
        boolean playGame = true;
        while (playGame){
            //This section is for the sake of game demonstration ========================
            //For the sake of demonstration allow user to choose input
            String filename = "";
            /*DEMONSTRATION CODE: COMMENT OUT FOR NORMAL FUNCTION ----------------------
            System.out.print("Enter file name or enter 'text' to play your own game: ");
            //Initialize game input stream
            filename = terminalScnr.nextLine().trim();
            ---------------------------------------------------------------------------*/
            FileInputStream  fileStream;
            Scanner gameScnr;
            
            //Try to open specified file
            try{
                fileStream = new FileInputStream (filename);//Open file
                gameScnr = new Scanner(fileStream);
            }
            //Otherwise use terminal
            catch(Exception e){
                gameScnr = terminalScnr;
            }
            //===========================================================================
            
            //Start new game
            ChessGame currentGame = new ChessGame(pawn -> promote(terminalScnr, pawn));
            //Cycle through player turns
            currentGame.gameCycle(gameScnr);
            //Ask user if they would like to play again
            playGame = playAgain(terminalScnr);
        }
    }
}