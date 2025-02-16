package chessBug.misc;

import chessBug.misc.IGameSelectionController;
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
