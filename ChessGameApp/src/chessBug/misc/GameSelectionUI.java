package chessBug.misc;

import chessBug.controllerInterfaces.IGameSelectionController;
import chessBug.network.Match;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;

public class GameSelectionUI {
    private VBox page = new VBox();
    private VBox gameRequests = new VBox();
    private VBox gamesInProgress = new VBox();
    private IGameSelectionController controller;
    
    public GameSelectionUI(IGameSelectionController controller){
        this.controller = controller;
        buildGameSelectionPrompt();
        
        //Add database checks
        controller.addToDatabaseCheckList(()->databaseChecks());
    }
    
    public Pane getPage(){return page;}
    
    public void databaseChecks(){
        //System.out.println("Debug: GameSelectionUI DatabaseCheck" );
        //Game requests
        gameRequests.getChildren().clear();
        controller.receiveMatchRequest().forEach(match -> displayMatch(match, true));
        if(gameRequests.getChildren().isEmpty())
            gameRequests.getChildren().add(new Label("No pending game requests"));
        //Games in progress
        gamesInProgress.getChildren().clear();
        controller.getOpenMatchList().forEach(match -> displayMatch(match, false));
        if(gamesInProgress.getChildren().isEmpty())
            gamesInProgress.getChildren().add(new Label("No current games"));
    }
    
    private void buildGameSelectionPrompt() {
        //Clear page
        page.getChildren().clear();
        
        //Add nodes
        Label header1 = new Label("Game requests");
        Label header2 = new Label("Games in progress");
        ScrollPane scroll1 = new ScrollPane(gameRequests);
        ScrollPane scroll2 = new ScrollPane(gamesInProgress);
        
        page.getChildren().addAll(
                header1, scroll1,
                header2, scroll2
                );
        
        //List Games
        //Game requests
        controller.receiveMatchRequest().forEach(match -> displayMatch(match, true));
        //Games in progress
        controller.getOpenMatchList().forEach(match -> displayMatch(match, false));
        
        if(gameRequests.getChildren().isEmpty())
            gameRequests.getChildren().add(new Label("No pending game requests"));
        if(gamesInProgress.getChildren().isEmpty())
            gamesInProgress.getChildren().add(new Label("No current games"));
        
        //Style
        page.setAlignment(Pos.CENTER);
        page.setPrefWidth(300);
        gameRequests.getStyleClass().add("scrollBackground");
        gamesInProgress.getStyleClass().add("scrollBackground");
        header1.getStyleClass().add("h2");
        header2.getStyleClass().add("h2");
    }    
    
    private void displayMatch(Match match, Boolean isRequest){
        //Layout
        HBox hbox = new HBox();
        Button matchButton = new Button(match.toString());
        Button endButton = new Button((isRequest)? "Deny" : "Forfeit");
        
        hbox.getChildren().addAll(matchButton, endButton);
        ((isRequest)? gameRequests : gamesInProgress).getChildren().add(hbox);
        
        //Determine current turn
        String currTurn = "";
        switch(match.getStatus()){
            case "WhiteTurn" -> currTurn = match.getWhite().getUsername();
            case "BlackTurn" -> currTurn = match.getBlack().getUsername();
        }
        
        matchButton.setOnAction(event -> {
            //If the match is requested, accept the Match
            if(isRequest)
                controller.acceptMatchRequest(match);
            
            page.getChildren().clear();
            //Update controller
            controller.selectGame(match);
        });
        
        endButton.setOnAction(event -> {
            if(isRequest)
                controller.denyMatchRequest(match);
            else
                controller.forfitMatch(match);
        });
        
        //Style
        hbox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(matchButton, Priority.ALWAYS);
        HBox.setHgrow(endButton, Priority.ALWAYS);
        matchButton.setMaxWidth(Double.MAX_VALUE);
        endButton.setMaxWidth(Double.MAX_VALUE);
        matchButton.setPrefWidth(200);   
        if (!isRequest && !currTurn.equals(controller.getUsername())){
            matchButton.getStyleClass().add("notYourMove");
            endButton.getStyleClass().add("notYourMove");
        }
            
    }
}
