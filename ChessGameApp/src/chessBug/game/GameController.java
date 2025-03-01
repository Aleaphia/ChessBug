/*
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package chessBug.game;

import chessBug.misc.IGameSelectionController;
import chessGame.*;
import chessBug.network.*;
import java.util.*;
import java.util.stream.Stream;

import javafx.event.ActionEvent;
import javafx.util.Duration;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.scene.layout.BorderPane;

public class GameController implements IGameSelectionController{
    //Database Connection
    private Client client;
    private Match match = null;
    private Chat chat;
    
    //MVC
    private GameModel model;
    private GameView view;
    
    //Constructors
    public GameController(Client player){ //No selected match
        //Connect to database
        client = player;
        
        //Create view
        view = new GameView(this);
    }
    public GameController(Client player, Match match){ //selected match
        this(player);
        internalSelectGame(match);
    }
    
    //Database check loop
    private void continueDatabaseChecks(){
        //Check database
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), (ActionEvent event) -> {
            //Add repeated database checks here ================================
            if(!isThisPlayersTurn()){ //While waiting for other player's move check database and update boardstate
                match.poll(client).forEach((move) -> internalPlayerMove(move));
                view.refresh(client);
            }
            else{ // during this player's turn just refresh chat
                view.refreshMessageBoard(client);
            }
            // =================================================================
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    //Getter Methods ===========================================================
    public BorderPane getPage(){return view.getPage();}
    
    //Database information
    public Stream<Message> getChatMessages(){return chat.poll(client);}
    public Stream<String> getMatchMoves(){return match.poll(client);}
    public List<Friend> getFriendList(){return client.getFriends();}
    
    //Model information
    public Piece getLocalPiece(String square){return model.getLocalPiece(square);}
    public ArrayList<String> getMoveListForLocalPiece(String square){return model.getMoveListForLocalPiece(square);}
    public Boolean isGameComplete(){return model.getGameComplete();}
    public Boolean isThisPlayersTurn(){return (model.getPlayerTurn() && model.getPlayerColor()) ||
            (!model.getPlayerTurn() && !model.getPlayerColor());}
    public Boolean getPlayerTurn(){return model.getPlayerTurn();}
    public Boolean getPlayerColor(){return model.getPlayerColor();}


    //Overriden methods ========================================================
    //IGameSelectionController methods
    @Override public String getUsername(){return client.getOwnUser().getUsername();}
    @Override public List<Match> getOpenMatchList(){return client.getOpenMatches();}
    @Override public List<Match> receiveMatchRequest(){return client.getMatchRequests();}
    @Override public void acceptMatchRequest(Match match){client.setMatchStatus(match, Match.Status.WHITE_TURN.toString());}
    @Override public void denyMatchRequest(Match match){client.denyMatchRequest(match);}
    @Override public void forfitMatch(Match match){ client.setMatchStatus(match, getUsername().equals(match.getWhite().getUsername()) ? 
            Match.Status.BLACK_WIN.toString() : Match.Status.WHITE_TURN.toString());} //If the user who forfit is white -> black wins, otherwise white wins
    @Override public void selectGame(Match newMatch){internalSelectGame(newMatch);}
    
    //Other Methods ============================================================
    /** sendChatMessage - sends chat message to database
     *  @param msg - chat message to send
     */
    public void sendChatMessage(String msg){chat.send(client, msg);}
    
    /** playerMove - makes extra changes needed for user moves (not database moves), e.g., update database, clear selected square from model
     *  Add code here if it should only happen when the move comes from this client/user
     * 
     * @param notation - chess move in coordinate notation (e.g., e2e4 or e7e8Q)
     */
    public void playerMove(String notation){
        //Check for legal move, also performs updates independent of move origin
        if (internalPlayerMove(notation)){
            //Update database
            match.makeMove(client, notation); //Add move
            client.setMatchStatus(match, model.getPlayerTurn() ? //set game status
                    Match.Status.WHITE_TURN.toString() :
                    Match.Status.BLACK_TURN.toString());
            
            //Update view
            view.deselectSquare();
            view.refresh(client);
            
            //Check for game end
            if (model.getGameComplete()){
                //Get end result
                String endMsg = model.getEndMessage();
                if (endMsg.charAt(11) == 'C'){ //Check for "Checkmate" vs "Draw" or "Stalemate"
                    boolean winner = (endMsg.charAt(22) == 'w'); // Check for  "...white" vs "...black"
                    client.setMatchStatus(match, (winner) ? Match.Status.WHITE_WIN.toString(): Match.Status.BLACK_WIN.toString());
                }
                else //If there is no winner, than the game is a draw
                    client.setMatchStatus(match, Match.Status.DRAW.toString());
            }
        }
    }
    
    /** internalPlayerMove - preforms changes to model and view needed by both database and user moves
     *  Add code here if it should happen regardless of move origin
     * 
     *  @param notation - chess move in coordinate notation (e.g., e2e4 or e7e8Q)
     *  @return true if the move was successfully made, false otherwise
     */
    private boolean internalPlayerMove(String notation){
        //Attempt to make player move, will return true on success
        if (model.makePlayerMove(notation)){
            //Add notation to view
            view.addToNotationBoard(notation, !model.getPlayerTurn(), model.getTurnNumber());
            
            //At end of game, display end message
            if (model.getGameComplete()){
                view.displayBotMessage(model.getEndMessage());
            }
            return true; //legal move was made - return true
        }
        return false; //ilegal move, no move made - return false
    }
    
    /** internalSelectGame - guild model and view for a selected match
     * @param newMatch - chess match to display
     */
    private void internalSelectGame(Match newMatch){
        //Cache match and chat
        match = newMatch;
        chat = match.getChat();
                
        //Build model
        boolean playerColor = match.getWhite().equals(client.getOwnUser()); //Assumes player is valid player in match
        model = new GameModel(playerColor);
        
        //Build game page
        view.buildGamePage();
        
        //Update chat/match status
        match.poll(client).forEach((move) -> internalPlayerMove(move));
        view.refresh(client);
        
        //Check database
        continueDatabaseChecks();
    }
    
    /** sendGameRequest - sends new game request to database
     * @param - playerColor : what color is the player requesting
     * @param - opponent: who are they playing against
     */
    public void sendGameRequest(Boolean playerColor, User opponent){
        // Send new match request in database for opponent
        try{
            client.sendMatchRequest(opponent, playerColor);
        }
        catch( Exception e){
            System.out.println("Error: Unable to create new match");
            e.printStackTrace();
        }
    }
}
