package chessBug.game;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class GameListUI {
    VBox page = new VBox();
    IGameSelectionController controller;
    
    GameListUI(IGameSelectionController controller){
        this.controller = controller;
        
        buildGameSelectionPrompt();
    }
    
    public VBox getPage(){return page;}
    
    private void buildGameSelectionPrompt() {
        //Clear page
        page.getChildren().clear();

        //New game button
        Button newGame = new Button("New Game");
        newGame.setOnMouseClicked(event -> {
            //TODO
        });
        page.getChildren().add(newGame);

        //List out games
        controller.getOpenMatchList().forEach(match -> {
            Button currMatch = new Button(match.toString());

            currMatch.setOnMouseClicked(event -> {
                //Create loading screen
                //TODO
                page.getChildren().clear();
                page.getChildren().add(new Label("Loading..."));
                //Update controller
                controller.selectGame(match);
                
            });

            page.getChildren().add(currMatch);
        });
    }
}
