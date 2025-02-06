/*
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package chessBug.game;

import chessGame.*;
import chessBug.network.*;
import java.util.*;
import java.util.stream.Stream;

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
    public GameController(Client player){
        //Connect to database
        client = player;
        
        //Create view
        view = new GameView(this);
    }
    
    //Getter/Setter Methods
    public Stream<Message> getChatMessages(){return chat.poll(client);}
    public void sendChatMessage(String msg){chat.send(client, msg);}
    public List<Friend> getFriendList(){return client.getFriends();}
    public Boolean getGameComplete(){return model.getGameComplete();}
    public Piece getLocalPiece(String square){return model.getLocalPiece(square);}
    public List<Match> getMatchList(){return client.getMatches();}
    public Stream<String> getMatchMoves(){return match.poll(client);}
    public ArrayList<String> getMoveListForLocalPiece(String square){return model.getMoveListForLocalPiece(square);}
    public Node getPage(){return view.getPage();}
    public Boolean getPlayerTurn(){return model.getPlayerTurn();}
    public Boolean getPlayerColor(){return model.getPlayerColor();}
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
        //Cache match and chat
        this.match = match;
        chat = match.getChat();
        
        //Create model
        boolean playerColor = match.getWhite().equals(client.getOwnUser()); //Assumes player is valid player in match
        model = new GameModel(playerColor, match.poll(client));
        
        //Check database
        continueDatabaseChecks();
    }
    public void createNewGame(Boolean playerColor, User opponent){
        //Create new matchin database
        try{
            match =  (playerColor)?
                    client.createMatch(client.getOwnUser(), opponent) :
                    client.createMatch(opponent, client.getOwnUser());
        }catch( Exception e){
            System.out.println("Error: Unable to create new match");
        }
        
        //Get chat
        chat = match.getChat();
        
        //Create model
        model = new GameModel(playerColor);
        
        //Start recuring database checks
        continueDatabaseChecks();
    }

}
