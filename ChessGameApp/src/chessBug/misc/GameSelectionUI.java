package chessBug.misc;

import chessBug.controllerInterfaces.IGameSelectionController;
import chessBug.network.Match;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;

public class GameSelectionUI {
    public enum GameStatus { REQUESTED, IN_PROGRESS, COMPLETE};
    
    private VBox page = new VBox();
    private VBox games = new VBox();
    private IGameSelectionController controller;
    private GameStatus status;
    private GameList gameList;
    
    public GameSelectionUI(IGameSelectionController controller, GameStatus status, GameList gameList){
        this.controller = controller;
        
        //Determine status
        this.status = status;
        this.gameList = gameList;

        buildGameSelectionPrompt();
        
        //Add database checks
        controller.addToDatabaseCheckList(()->databaseChecks());
    }
    
    public Pane getPage(){return page;}
    
    public void databaseChecks(){
        //System.out.println("Debug: GameSelectionUI DatabaseCheck" );
        //Games in progress
        games.getChildren().clear();
        
        gameList.getGameList().forEach(match -> displayMatch(match));
        if(games.getChildren().isEmpty())
            games.getChildren().add(new Label("No current games"));
    }
    
    private void buildGameSelectionPrompt() {
        //Clear page
        page.getChildren().clear();
        
        //Add nodes
        Label header = new Label(
                ((status == GameStatus.REQUESTED)? "Requested games" :
                        ((status == GameStatus.IN_PROGRESS)? "Games in progress":
                                "Completed games")) //status == GameStatus.COMPLETED
        );
        ScrollPane scroll = new ScrollPane(games);
        
        page.getChildren().addAll(header, scroll);
        
        //List Games
        gameList.getGameList().forEach(match -> displayMatch(match));
       
        if(games.getChildren().isEmpty())
            games.getChildren().add(new Label("No current games"));
        
        //Style
        page.setAlignment(Pos.CENTER);
        page.setPrefWidth(400);
        games.getStyleClass().add("scrollBackground");
        header.getStyleClass().add("h2");
    }    
    
    private void displayMatch(Match match){
        //Layout
        HBox hbox = new HBox();
        Button matchButton = new Button(match.toString());
        hbox.getChildren().add(matchButton);
        
         matchButton.setOnAction(event -> {
            //If the match is requested, accept the Match
            if(status == GameStatus.REQUESTED)
                controller.acceptMatchRequest(match);
            
            page.getChildren().clear();
            //Update controller
            controller.selectGame(match);
        });
        
        
        
        //Noncomplete games have options for ending the game immediately
        if (status != GameStatus.COMPLETE){
            Button endButton = new Button((status == GameStatus.REQUESTED)? "Deny" : "Forfeit");
            hbox.getChildren().add(endButton);
            
            //Function
            endButton.setOnAction(event -> {
                if(status == GameStatus.REQUESTED)
                    controller.denyMatchRequest(match);
                else
                    controller.forfeitMatch(match);
            });
            
            //Style
            HBox.setHgrow(endButton, Priority.ALWAYS);
            endButton.setMaxWidth(Double.MAX_VALUE);
            if (status == GameStatus.IN_PROGRESS){
                //Determine current turn
                String currTurn = "";
                switch(match.getStatus()){
                    case "WhiteTurn" -> currTurn = match.getWhite().getUsername();
                    case "BlackTurn" -> currTurn = match.getBlack().getUsername();
                }
                if (!currTurn.equals(controller.getUsername())){
                    matchButton.getStyleClass().add("notYourMove");
                    endButton.getStyleClass().add("notYourMove");
                }
            }
        }
        
        games.getChildren().add(hbox);
                
        //Style
        hbox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(matchButton, Priority.ALWAYS);
        matchButton.setMaxWidth(Double.MAX_VALUE);
        matchButton.setPrefWidth(200);       
    }
    
    public interface GameList{
        public List<Match> getGameList();
    }

}
