package chessBug.misc;

import chessBug.network.Friend;
import java.util.Random;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

public class GameCreationUI {
    private VBox page = new VBox();
    private IGameCreationController controller;
    
    public GameCreationUI(IGameCreationController controller){
        this.controller = controller;
        addNewGameButton();
    }
    
    public VBox getPage(){return page;}
    
    private void addNewGameButton(){
        //New game button
        Button newGame = new Button("New Game");
        newGame.setOnMouseClicked(event -> buildGameCreationPrompt());
        page.getChildren().add(newGame);
    }
    
    private void buildGameCreationPrompt(){
        //Clear page
        page.getChildren().clear();

        //Select Color
        page.getChildren().add(new Label("Select color:"));
        char[] colorSelection = new char[1];
        colorSelection[0] = '0'; //w for white, b for black, r for random
        ToggleGroup colorOptions = new ToggleGroup();
        String[] colorOptionList = {"white", "black", "random"};

        for (String option : colorOptionList) {
            RadioButton curr = new RadioButton(option);
            curr.setOnAction(event -> colorSelection[0] = option.charAt(0));
            curr.setToggleGroup(colorOptions);
            page.getChildren().add(curr);
        }

        //Challenge friend: list friends in radio buttons
        page.getChildren().add(new Label("Challenge friend:"));
        Friend[] friendSelection = new Friend[1];
        friendSelection[0] = null;
        ToggleGroup friendOptions = new ToggleGroup();
        
        controller.getFriendList().forEach(friend -> {
            RadioButton curr = new RadioButton(friend.getUsername());
            curr.setOnAction(event -> friendSelection[0] = friend);
            curr.setToggleGroup(friendOptions);
            page.getChildren().add(curr);
        });

        //Create game button
        Button createGame = new Button("Request Game");
        createGame.setOnMouseClicked(event -> {
            if (colorSelection[0] != '0' && friendSelection[0] != null){
                //Determine color
                boolean playerColor;
                switch (colorSelection[0]) {
                    case 'w' -> playerColor = true; //white
                    case 'b' -> playerColor = false; // black
                    default -> playerColor = new Random().nextBoolean(); //random
                }

                //Create new game
                controller.sendGameRequest(playerColor, friendSelection[0]);
                page.getChildren().clear();
                addNewGameButton();
            }
            
        });
        page.getChildren().add(createGame);
    }
}
