package chessBug.misc;

import chessBug.network.Match;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
        
        //Add nodes
        Label header1 = new Label("Game requests");
        Label header2 = new Label("Games in progress");
        
        page.getChildren().addAll(
                header1, gameRequests,
                header2, gamesInProgress
                );
        
        //List Games
        //Game requests
        controller.receiveMatchRequest().forEach(match -> displayMatch(match, gameRequests, true));
        //Games in progress
        controller.getOpenMatchList().forEach(match -> displayMatch(match, gamesInProgress, false));
        
        //Style
        page.setAlignment(Pos.CENTER);
        header1.getStyleClass().add("header");
        header2.getStyleClass().add("header");
    }    
    
    private void displayMatch(Match match, VBox pane, Boolean isRequest){
        //Layout
        HBox hbox = new HBox();
        Button matchButton = new Button(match.toString());
        Button endButton = new Button((isRequest)? "Deny" : "Forfiet");
        
        //Determine current turn
        String currTurn = "";
        switch(match.getStatus()){
            case "WhiteTurn" -> currTurn = match.getWhite().getUsername();
            case "BlackTurn" -> currTurn = match.getBlack().getUsername();
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
        
        //Style
        HBox.setHgrow(matchButton, Priority.ALWAYS);
        matchButton.setPrefWidth(200);
        if (!isRequest && !currTurn.equals(controller.getUsername())){
            matchButton.getStyleClass().add("notYourMove");
            endButton.getStyleClass().add("notYourMove");
        }
            
    }
}
