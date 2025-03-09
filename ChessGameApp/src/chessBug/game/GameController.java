/*
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package chessBug.game;

import chessBug.misc.*;
import chessGame.*;
import chessBug.network.*;
import java.util.*;
import java.util.stream.Stream;

import javafx.event.ActionEvent;
import javafx.util.Duration;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class GameController implements IGameSelectionController, IGameCreationController{
    //Database Connection
    private Client client;
    private Match match = null;
    private Chat chat;
    //Page
    private HBox page = new HBox();
    //MVC
    private GameModel model;
    private GameView view;
    
    //Constructors
    public GameController(Client player){ //No selected match
        //Connect to database
        client = player;
        
        //Create view
        //Game Prompt Panel
        VBox promptSelectionPanel = new VBox();
        Region leftRegion = new Region();
        Region rightRegion = new Region();
        
        page.getChildren().addAll(leftRegion,promptSelectionPanel,rightRegion);
        page.getStyleClass().add("padding");
        HBox.setHgrow(leftRegion, Priority.ALWAYS);
        HBox.setHgrow(rightRegion, Priority.ALWAYS);
        promptSelectionPanel.getStyleClass().add("section");

        //Add selection panel components
        promptSelectionPanel.getChildren().addAll(new GameCreationUI(this).getPage(), new GameSelectionUI(this).getPage());
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
    public Pane getPage(){return page;}
    
    //Database information
    public Stream<Message> getChatMessages(){return chat.poll(client);}
    public Stream<String> getMatchMoves(){return match.poll(client);}
    
    //Model information
    public Piece getLocalPiece(String square){return model.getLocalPiece(square);}
    public ArrayList<String> getMoveListForLocalPiece(String square){return model.getMoveListForLocalPiece(square);}
    public Boolean isGameComplete(){return model.getGameComplete();}
    public Boolean isThisPlayersTurn(){return (model.getPlayerTurn() && model.getPlayerColor()) ||
            (!model.getPlayerTurn() && !model.getPlayerColor());}
    public Boolean getPlayerTurnBoolean(){return model.getPlayerTurn();}
    public Boolean getPlayerColor(){return model.getPlayerColor();}
    public User getPlayerTurnUser(){return (model.getPlayerTurn())? match.getWhite() : match.getBlack();}


    //Overriden methods ========================================================
    //IGameSelectionController methods
    @Override public String getUsername(){return client.getOwnUser().getUsername();}
    @Override public List<Match> getOpenMatchList(){return client.getOpenMatches();}
    @Override public List<Match> receiveMatchRequest(){return client.getMatchRequests();}
    @Override public void acceptMatchRequest(Match match){client.acceptMatchRequest(match);}
    @Override public void denyMatchRequest(Match match){client.denyMatchRequest(match);}
    @Override public void forfitMatch(Match match){client.forfitMatch(match);}
    @Override public void selectGame(Match newMatch){internalSelectGame(newMatch);}
    
    //IGameCreationController methods
    @Override public List<Friend> getFriendList(){return client.getFriends();}
    @Override public void sendGameRequest(Boolean playerColor, User opponent){
        // Send new match request in database for opponent
        try{
            client.sendMatchRequest(opponent, playerColor);
        }
        catch( Exception e){
            System.out.println("Error: Unable to create new match");
            e.printStackTrace();
        }
    }
    
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
            client.setGameTurn(match, model.getPlayerTurn()); //set game status
            
            //Update view
            view.deselectSquare();
            view.refresh(client);
            
            //Check for game end
            if (model.getGameComplete()){
                //Get end result
                String endMsg = model.getEndMessage();
                if (endMsg.charAt(11) == 'C'){ //Check for "Checkmate" vs "Draw" or "Stalemate"
                    boolean winner = (endMsg.charAt(22) == 'w'); // Check for  "...white" vs "...black"
                    client.setGameWinner(match,winner);
                }
                else //If there is no winner, than the game is a draw
                    client.setGameDraw(match);
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
            
            //Display player turn
            String msg = ((getPlayerTurnBoolean())? "White" : "Black") + "'s turn: " +
                ((isThisPlayersTurn())? "Your move!" : "Waiting on " + getPlayerTurnUser().getUsername());
            view.displayBotMessage(msg);
            
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
    private void internalSelectGame(Match match){
        //Cache match and chat
        this.match = match;
        chat = this.match.getChat();
                        
        //Build model
        boolean playerColor = this.match.getWhite().equals(client.getOwnUser()); //Assumes player is valid player in match
        model = new GameModel(playerColor);
                
        //Build game page
        view = new GameView(this);
        
        page.getChildren().clear();
        Region leftRegion = new Region();
        Region rightRegion = new Region();
        
        page.getChildren().addAll(leftRegion,view.getPage(),rightRegion);
        HBox.setHgrow(leftRegion, Priority.ALWAYS);
        HBox.setHgrow(rightRegion, Priority.ALWAYS);
                
        //Update chat/match status
        this.match.poll(client).forEach((move) -> internalPlayerMove(move));
        view.refresh(client);
        
        //Check database
        continueDatabaseChecks();
    }
}
