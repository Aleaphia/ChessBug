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
import javafx.scene.layout.Pane;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.util.Duration;
import javafx.scene.layout.Region;

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
            controller.receiveMatchRequest().forEach(match -> displayMatch(match, true));
            //Games in progress
            gamesInProgress.getChildren().clear();
            controller.getOpenMatchList().forEach(match -> displayMatch(match, false));
            // =================================================================
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    public Pane getPage(){return page;}
    
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
        
        //Style
        page.setAlignment(Pos.CENTER);
        page.setPrefWidth(300);
        gameRequests.getStyleClass().add("scrollBackground");
        gamesInProgress.getStyleClass().add("scrollBackground");
        header1.getStyleClass().add("header");
        header2.getStyleClass().add("header");
    }    
    
    private void displayMatch(Match match, Boolean isRequest){
        //Layout
        HBox hbox = new HBox();
        Button matchButton = new Button(match.toString());
        Button endButton = new Button((isRequest)? "Deny" : "Forfiet");
        
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
        matchButton.setPrefWidth(200);
        HBox.setHgrow(endButton, Priority.ALWAYS);
        hbox.setStyle("-fx-background-color: rgba(54, 57, 63, 0);");
        if (!isRequest && !currTurn.equals(controller.getUsername())){
            matchButton.getStyleClass().add("notYourMove");
            endButton.getStyleClass().add("notYourMove");
        }
            
    }
}
