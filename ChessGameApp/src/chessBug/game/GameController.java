/*
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package chessBug.game;

import chessBug.controllerInterfaces.IGameSelectionController;
import chessBug.controllerInterfaces.IGameCreationController;
import chessBug.misc.*;
import chessGame.*;
import chessBug.network.*;
import chessBug.preferences.PreferencesController;
import java.util.*;
import java.util.stream.Stream;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;

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
    private DatabaseCheckList databaseCheckList; //Add recursive database checks here
    //Page
    private HBox page = new HBox();
    //MVC
    private GameModel model;
    private GameView view;
    
    //Constructors
    public GameController(Client player, DatabaseCheckList databaseCheckList){ //No selected match
        //Connect to database
        client = player;
        this.databaseCheckList = databaseCheckList;
        
        //Create view
        //Game Prompt Panel
        VBox promptSelectionPanel = new VBox();
        ArrayList<Region> regionList = new ArrayList<>();
        regionList.add(new Region());
        regionList.add(new Region());
        
        page.getChildren().addAll(regionList.get(regionList.size() - 2),
                promptSelectionPanel,regionList.get(regionList.size() - 1));
        
        //Build promptSelection Pannel
        HBox menu = new HBox();
        VBox gameDisplayNode = new VBox();
        
        //Menu
        Button newGame = new Button("New Games");
        Button currGames = new Button("Current Games");
        Button oldGames = new Button("Old Games");
        regionList.add(new Region());
        regionList.add(new Region());
        menu.getChildren().addAll(newGame, regionList.get(regionList.size() - 2),
                currGames, regionList.get(regionList.size() - 1), oldGames); 
        
        //Menu funtionality
        newGame.setOnAction(event -> {
            gameDisplayNode.getChildren().clear();
            gameDisplayNode.getChildren().addAll(
                    new GameSelectionUI(
                            this, GameSelectionUI.GameStatus.REQUESTED,
                            (() -> client.getMatchRequests())).getPage(),
                    new GameCreationUI(this).getPage()
            );
        });
        
        currGames.setOnAction(event -> {
            gameDisplayNode.getChildren().clear();
            gameDisplayNode.getChildren().add(
                    new GameSelectionUI(
                            this, GameSelectionUI.GameStatus.IN_PROGRESS,
                            (() -> client.getOpenMatches())).getPage()
            );
        });
        
        oldGames.setOnAction(event -> {
            gameDisplayNode.getChildren().clear();
            gameDisplayNode.getChildren().add(
                    new GameSelectionUI(
                            this, GameSelectionUI.GameStatus.COMPLETE,
                            (() -> client.getClosedMatches())).getPage()
            );
        });
        
        
        //gameDisplayNode
        gameDisplayNode.getChildren().add(new GameSelectionUI(
                        this, GameSelectionUI.GameStatus.IN_PROGRESS,
                        (() -> client.getOpenMatches())).getPage()
                );
        
        //Style and format
        page.getStyleClass().add("padding");
        regionList.forEach(region -> HBox.setHgrow(region, Priority.ALWAYS));
        promptSelectionPanel.getStyleClass().add("section");

        //Add selection panel components
        promptSelectionPanel.getChildren().addAll(menu, new Separator(), gameDisplayNode);
    }
    public GameController(Client player, DatabaseCheckList databaseCheckList, Match match){ //selected match
        //Connect to database
        client = player;
        this.databaseCheckList = databaseCheckList;
        
        internalSelectGame(match);
    }
    
    private void databaseChecks(){
        //System.out.println("Debug: GameController DatabaseCheck" );
        try {
            match.poll(client).forEach((move) -> internalPlayerMove(move));
            view.refresh(client);
        } catch (NetworkException ignored) {} // We'll try again in a moment
    }
    
    //Getter Methods ===========================================================
    public Pane getPage(){return page;}
    
    //Database information
    public Stream<Message> getChatMessages() throws NetworkException {return chat.poll(client);}
    public Stream<String> getMatchMoves() throws NetworkException {return match.poll(client);}
    
    //Model information
    public Piece getLocalPiece(String square){return model.getLocalPiece(square);}
    public ArrayList<String> getMoveListForLocalPiece(String square){return model.getMoveListForLocalPiece(square);}
    public Boolean isGameComplete(){return model.getGameComplete();}
    public Boolean isThisPlayersTurn(){return (model.getPlayerTurn() && model.getPlayerColor()) ||
            (!model.getPlayerTurn() && !model.getPlayerColor());}
    public Boolean getPlayerTurnBoolean(){return model.getPlayerTurn();}
    public Boolean getPlayerColor(){return model.getPlayerColor();}
    public User getPlayerTurnUser(){return (model.getPlayerTurn())? match.getWhite() : match.getBlack();}
    public String getPosition(){return model.getPosition();}
    public String getPosition(int index){return model.getPostion(index);}
    public int getTurnNumber(){return model.getTurnNumber();}
    //public int getTurnCount(){return model.getTurnCount();}

    //Overriden methods ========================================================
    //IDatabaseCheckInterface methods
    @Override public void addToDatabaseCheckList(DatabaseCheck item){databaseCheckList.add(item);}
    //IGameSelectionController methods
    @Override public String getUsername(){return client.getOwnUser().getUsername();}
    @Override public void acceptMatchRequest(Match match) throws NetworkException {client.acceptMatchRequest(match);}
    @Override public void denyMatchRequest(Match match) throws NetworkException {client.denyMatchRequest(match);}
    @Override public void forfeitMatch(Match match) throws NetworkException {client.forfeitMatch(match);}
    @Override public void selectGame(Match newMatch) {internalSelectGame(newMatch);}
    
    
    //IGameCreationController methods
    @Override public List<Friend> getFriendList() throws NetworkException {return client.getFriends();}
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
    public void sendChatMessage(String msg) throws NetworkException {chat.send(client, msg);}
    /** forfeit - automatically lose game
     */
    public void forfeitMatch() throws NetworkException {forfeitMatch(match);}
    
    /** playerMove - makes extra changes needed for user moves (not database moves), e.g., update database, clear selected square from model
     *  Add code here if it should only happen when the move comes from this client/user
     * 
     * @param notation - chess move in coordinate notation (e.g., e2e4 or e7e8Q)
     */
    public void playerMove(String notation) throws NetworkException {
        //Check for legal move, also performs updates independent of move origin
        if (PreferencesController.confirmMove() && internalPlayerMove(notation)){
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
        if (notation.equals("end")){
            String endMsg = model.getEndMessage();
            if (endMsg == null){
                model.endGame();
                endMsg = this.match.getStatus().substring(0,5) + " won by forfeit";
            }
            view.displayBotMessage(endMsg);
        }
        //Attempt to make player move, will return true on success
        else if (model.makePlayerMove(notation)){
            //Sound
            PreferencesController.playSound();
            
            //Add notation to view
            view.addToNotationBoard(notation, !model.getPlayerTurn(), (model.getTurnNumber() - 1)/2);
            
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
        try {
            this.match.poll(client).forEach((move) -> internalPlayerMove(move));
        } catch(NetworkException ignored) {} // We'll try again soon
        if (!model.getGameComplete()
                && this.match.getStatus().charAt(5) == 'W'){ //Acount for forfeit wins that wouldn't otherwise get noticed
                model.endGame();
                view.displayBotMessage(this.match.getStatus().substring(0,5) + " won by forfeit");
        }
        view.refresh(client);
        //Check database
        addToDatabaseCheckList(()->databaseChecks());
    }
}
