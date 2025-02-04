/*
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package chessBug.game;

import chessGame.*;
import chessBug.network.*;
import java.util.*;

import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.util.Duration;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;

public class GameController {
    //Database Connection
    private Client client;
    private Match match = null;
    private Chat chat;
    
    //Model
    private GameModel model;
    //Views
    private GameView view;
    
    //Constructors
    public GameController(Client player, User opponent, boolean playerColor) { //New game
        //Connect to database
        client = player;//Save client
        //Create new database
        try{
            match =  (playerColor)?
                    client.createMatch(client.getOwnUser(), opponent) :
                    client.createMatch(opponent, client.getOwnUser());
        }catch( Exception e){
            System.out.println("Error: Unable to create new match");
        }  
        //Get chat
        chat = match.getChat();
        
        //Create model and view
        model = new GameModel(playerColor);
        view = new GameView(playerColor, this);
        
        //Update game state
        view.refresh();
        
        //Start recuring database checks
        continueDatabaseChecks();
    }
    public GameController(Client player, Match match){
        //Connect to database
        client = player;
        this.match = match;
        chat = match.getChat();
        
        //Create model and view
        boolean playerColor = match.getWhite().equals(client.getOwnUser()); //Assumes player is valid player in match
        model = new GameModel(playerColor, match.getAllMoves());
        view = new GameView(playerColor, this);
        
        //Update game state
        view.refresh();
        
        //Check database
        continueDatabaseChecks();
    }
    public GameController(Client player){
        //Connect to database
        client = player;
        
        //Create view
        view = new GameView(this);
    }
    
    //Getter/Setter Methods
    public ArrayList<Message> getChatMessages(){return chat.getAllMessages();}
    public void sendChatMessage(String msg){chat.send(client, msg);}
    public Boolean getGameComplete(){return model.getGameComplete();}
    public Piece getLocalPiece(String square){return model.getLocalPiece(square);}
    public List<Match> getMatchList(){return client.getMatches();}
    public ArrayList<String> getMatchMoves(){return match.getAllMoves();}
    public ArrayList<String> getMoveListForLocalPiece(String square){return model.getMoveListForLocalPiece(square);}
    public Node getPage(){return view.getPage();}
    public Boolean getPlayerTurn(){return model.getPlayerTurn();}
    public String getUserName(){return client.getOwnUser().getUsername();}
    
    //Other Methods
    //loop database check
    private void continueDatabaseChecks(){
        //Check database
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), (ActionEvent event) -> {
            //Add repeated database checks here ================================
            if(!model.isPlayerTurn()){ //While waiting for other player's move check database and update boardstate
                match.poll(client).forEach((move) -> playerMove(move));
                view.refresh();
            }
            else // during this player's turn just refresh chat
                view.refreshMessageBoard();
            
            // =================================================================
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void playerMove(String notation){
        //Attempt to make player move, will return true on success
        if (model.makePlayerMove(notation)){
            //Update the board display
            view.refresh();
            view.addToNotationBoard(notation, !model.getPlayerTurn(), model.getTurnNumber());
            view.deselectSquare();
            //Send move to database
            match.makeMove(client, notation);
        }
        else { //If the game move is Illegal, output error message
            //TODO
        }
    }
    
    public void matchSelection(Match match){
        this.match = match;
        chat = match.getChat();
        
        //Create model and view
        boolean playerColor = match.getWhite().equals(client.getOwnUser()); //Assumes player is valid player in match
        model = new GameModel(playerColor, match.getAllMoves());
        view.reBuildPage();
        
        //Check database
        continueDatabaseChecks();
    }

}
