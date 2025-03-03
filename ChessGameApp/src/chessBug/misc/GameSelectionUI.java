package chessBug.misc;

import chessBug.network.Match;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

public class GameSelectionUI {
    private VBox page = new VBox();
    private VBox gameRequests = new VBox();
    private VBox gamesInProgress = new VBox();
    private IGameSelectionController controller;
    
    public GameSelectionUI(IGameSelectionController controller){
        this.controller = controller;
        
        buildGameSelectionPrompt();
        continueDatabaseChecks();
    }
    
    private void continueDatabaseChecks(){
        //Check database
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), (ActionEvent event) -> {
            //Add repeated database checks here ================================
            //Reload game info
            //Game requests
            gameRequests.getChildren().clear();
            controller.receiveMatchRequest().forEach(match -> displayMatch(match, gameRequests, true));
            //Games in progress
            gamesInProgress.getChildren().clear();
            controller.getOpenMatchList().forEach(match -> displayMatch(match, gamesInProgress, false));
            // =================================================================
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    public VBox getPage(){return page;}
    
    private void buildGameSelectionPrompt() {
        //Clear page
        page.getChildren().clear();
        page.getChildren().addAll(
                new Label("Game requests"), gameRequests,
                new Label("Games in progress"), gamesInProgress
                );
        
        //List Games
        //Game requests
        controller.receiveMatchRequest().forEach(match -> displayMatch(match, gameRequests, true));
        //Games in progress
        controller.getOpenMatchList().forEach(match -> displayMatch(match, gamesInProgress, false));
    }    
    
    private void displayMatch(Match match, VBox pane, Boolean isRequest){
        //Layout
        HBox hbox = new HBox();
        Button matchButton = new Button(match.toString());
        Button endButton = new Button((isRequest)? "Forfiet" : "Deny");
        
        //Determine current turn
        String currTurn = "";
        switch(match.getStatus()){
            case "WhiteTurn" -> currTurn = match.getWhite().getUsername();
            case "BlackTurn" -> currTurn = match.getBlack().getUsername();
        }
        if (!isRequest && currTurn.equals(controller.getUsername())){
            matchButton.getStyleClass().add("yourMove");
        }
        
        hbox.getChildren().addAll(matchButton,endButton);
        pane.getChildren().add(hbox);
        
        matchButton.setOnAction(event -> {
            //If the match is requested, accept the Match
            if(isRequest)
                controller.acceptMatchRequest(match);
                            
            //Create loading screen
            //TODO
            page.getChildren().clear();
            page.getChildren().add(new Label("Loading..."));
            //Update controller
            controller.selectGame(match);
        });
        
        endButton.setOnAction(event -> {
            if(isRequest)
                controller.denyMatchRequest(match);
            else
                controller.forfitMatch(match);
        });
        

            
    }
}
