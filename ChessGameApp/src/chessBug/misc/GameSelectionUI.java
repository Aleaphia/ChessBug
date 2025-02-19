package chessBug.misc;

import chessBug.network.Match;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

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
        Button matchButton = new Button(match.toString());
//        if(match.getAllMoves().size()%2 == 0){ //TODO - fix
//            matchButton.getStyleClass().add("whiteTurn");
//        }

            matchButton.setOnMouseClicked(event -> {
                if(!match.getStatus().equals("InProgress"))
                    controller.acceptMatchRequest(match);
                //Create loading screen
                //TODO
                page.getChildren().clear();
                page.getChildren().add(new Label("Loading..."));
                //Update controller
                controller.selectGame(match);
                
            });

            page.getChildren().add(matchButton);
    }
}
