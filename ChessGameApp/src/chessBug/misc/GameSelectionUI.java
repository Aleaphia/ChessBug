package chessBug.misc;

import chessBug.network.Match;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

public class GameSelectionUI {
    private VBox page = new VBox();
    private IGameSelectionController controller;
    
    public GameSelectionUI(IGameSelectionController controller){
        this.controller = controller;
        
        buildGameSelectionPrompt();
    }
    
    public VBox getPage(){return page;}
    
    private void buildGameSelectionPrompt() {
        //Clear page
        page.getChildren().clear();
        
        //List Games
        page.getChildren().add(new Label("Game requests"));
        controller.receiveMatchRequest().forEach(match -> displayMatch(match));
        
        page.getChildren().add(new Label("Games in progress"));
        controller.getOpenMatchList().forEach(match -> displayMatch(match));
    }
    
    private void displayMatch(Match match){
        HBox hbox = new HBox();
        Button matchButton = new Button(match.toString());
        Button endButton = new Button((match.getStatus().substring(5).equals("Turn"))? "Forfiet" : "Deny");
        
        String currTurn = "";
        switch(match.getStatus()){
            case "WhiteTurn" -> currTurn = match.getWhite().getUsername();
            case "BlackTurn" -> currTurn = match.getBlack().getUsername();
        }
        System.out.println(currTurn + ".vs " + controller.getUsername());
        if (currTurn.equals(controller.getUsername())){
            matchButton.getStyleClass().add("yourMove");
        }
            
        
        
        hbox.getChildren().addAll(matchButton,endButton);
        page.getChildren().add(hbox);
        
        matchButton.setOnAction(event -> {
            if(!match.getStatus().equals("InProgress"))
                controller.acceptMatchRequest(match);
            //Create loading screen
            //TODO
            page.getChildren().clear();
            page.getChildren().add(new Label("Loading..."));
            //Update controller
            controller.selectGame(match);
        });
        
        endButton.setOnAction(event -> {
            if(!match.getStatus().equals("InProgress"))
                controller.denyMatchRequest(match);
            else{}
                //TODO
        });
        

            
    }
}
