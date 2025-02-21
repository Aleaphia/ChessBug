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
import java.util.concurrent.CompletableFuture;

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
    
    //Model
    private GameModel model;
    //Views
    private GameView view;
    
    //Constructors
    public GameController(Client player){
        //Connect to database
        client = player;
        
        //Create view
        view = new GameView(this);
    }
    public GameController(Client player, Match match){
        this(player);
        internalSelectGame(match);
    }
    
    //Getter/Setter Methods
    public Stream<Message> getChatMessages(){return chat.poll(client);}
    public void sendChatMessage(String msg){chat.send(client, msg);}
    public List<Friend> getFriendList(){return client.getFriends();}
    public Boolean getGameComplete(){return model.getGameComplete();}
    public Piece getLocalPiece(String square){return model.getLocalPiece(square);}
    public Stream<String> getMatchMoves(){return match.poll(client);}
    public ArrayList<String> getMoveListForLocalPiece(String square){return model.getMoveListForLocalPiece(square);}
    public BorderPane getPage(){return view.getPage();}
    public Boolean getPlayerTurn(){return model.getPlayerTurn();}
    public Boolean getPlayerColor(){return model.getPlayerColor();}
    public Boolean isThisPlayersTurn(){return (model.getPlayerTurn() && model.getPlayerColor()) ||
            (!model.getPlayerTurn() && !model.getPlayerColor());}
    public String getUserName(){return client.getOwnUser().getUsername();}
    
    //Overriden methods
    //IGameSelectionController methods
    @Override public String getUsername(){return client.getOwnUser().getUsername();}
    @Override public List<Match> getOpenMatchList(){return client.getOpenMatches();}
    @Override public List<Match> receiveMatchRequest(){return client.getMatchRequests();}
    @Override public void acceptMatchRequest(Match match){client.acceptMatchRequest(match);}
    @Override public void denyMatchRequest(Match match){client.denyMatchRequest(match);}
    
    //Other Methods
    //loop database check
    private void continueDatabaseChecks(){
        //Check database
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), (ActionEvent event) -> {
            //Add repeated database checks here ================================
            if(!isThisPlayersTurn()){ //While waiting for other player's move check database and update boardstate
                match.poll(client).forEach((move) -> {
                    System.out.println(move);
                    internalPlayerMove(move);
                    
                        });
                view.refresh();
            }
            else{ // during this player's turn just refresh chat
                view.refreshMessageBoard();
            }
            
            
            // =================================================================
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void playerMove(String notation){
        if (internalPlayerMove(notation)){
            match.makeMove(client, notation);
            view.deselectSquare();
            view.refresh();
            if (model.getGameComplete()){
                String endMsg = "Game over: Checkmate, black wins!";//model.getEndMessage();
                view.displayMessage(endMsg);
                
                //TODO - Update database with game result
                if (endMsg.charAt(11) == 'C'){ //Check for "Checkmate" vs "Draw" or "Stalemate"
                    boolean winner = (endMsg.charAt(22) == 'w');
                }
            }
        }
    }
    
    private boolean internalPlayerMove(String notation){
        //Attempt to make player move, will return true on success
        if (model.makePlayerMove(notation)){
            //Add notation
            view.addToNotationBoard(notation, !model.getPlayerTurn(), model.getTurnNumber());
            client.setMatchStatus(match, model.getPlayerTurn() ? Match.WHITE_TURN : Match.BLACK_TURN);
            return true;
        }
        else { //If the game move is Illegal, output error message
            //TODO
            
        }
        return false;
    }
    
    @Override public void selectGame(Match newMatch){
        internalSelectGame(newMatch);
    }
    private void internalSelectGame(Match newMatch){
        //Cache match and chat
        match = newMatch;
        chat = match.getChat();
                
        //Create model
        boolean playerColor = match.getWhite().equals(client.getOwnUser()); //Assumes player is valid player in match
        model = new GameModel(playerColor);
        
        //Build game page
        view.buildGamePage();
        
        //Update chat/match status
        match.poll(client).forEach((move) -> internalPlayerMove(move));
        view.refresh();
        
        //Check database
        continueDatabaseChecks();
    }
    public void sendGameRequest(Boolean playerColor, User opponent){
        // Send new match request in database for opponent
        try{
            client.sendMatchRequest(opponent, playerColor);
        }catch( Exception e){
            System.out.println("Error: Unable to create new match");
            e.printStackTrace();
        }
        return;
        /*
        //Get chat
        chat = match.getChat();
        
        //Create model
        model = new GameModel(playerColor);
        
        //Build game page
        view.buildGamePage();
        view.refresh();
        
        //Start recuring database checks
        continueDatabaseChecks();
        */
    }

}
