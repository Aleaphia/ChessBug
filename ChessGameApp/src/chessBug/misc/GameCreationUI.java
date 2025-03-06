package chessBug.misc;

import chessBug.network.Friend;
import java.util.Random;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;


public class GameCreationUI {
    private GridPane page = new GridPane();
    private IGameCreationController controller;
    
    public GameCreationUI(IGameCreationController controller){
        this.controller = controller;
        addNewGameButton();
    }
    
    public GridPane getPage(){return page;}
    
    private void addNewGameButton(){
        //Clear page
        page.getChildren().clear();
        
        //New game button
        Button newGame = new Button("New Game");
        VBox buttonBox = new VBox(newGame);
        newGame.setOnMouseClicked(event -> buildGameCreationPrompt());
        page.add(buttonBox, 0, 0);
        
        //Style
        GridPane.setHgrow(buttonBox, Priority.ALWAYS);
        buttonBox.setAlignment(Pos.TOP_RIGHT);
        page.getStyleClass().add("grid");
    }
    
    private void buildGameCreationPrompt(){
        int row = 0;
        //Clear page
        page.getChildren().clear();
        
        //Title
        Label header = new Label("Create New Game");
        VBox headerBox = new VBox(header);
        page.add(headerBox, 0, row++, 4, 1);

        //Select Color ---------------------------------------------------------
        Label label1 = new Label("Select color:");
        VBox labelBox1 = new VBox(label1);
        page.add(labelBox1, 0, row++, 4, 1);
        char[] colorSelection = new char[1];
        colorSelection[0] = '0'; //w for white, b for black, r for random
        ToggleGroup colorOptions = new ToggleGroup();
        String[] colorOptionList = {"white", "black", "random"};

        for (String option : colorOptionList) {
            RadioButton curr = new RadioButton(option);
            curr.setOnAction(event -> colorSelection[0] = option.charAt(0));
            curr.setToggleGroup(colorOptions);
            page.add(curr, 1, row++, 2, 1);
            
            //Style
            curr.getStyleClass().add("label");
        }

        //Challenge friend: list friends in radio buttons ----------------------
        Label label2 = new Label("Challenge friend:");
        VBox labelBox2 = new VBox(label2);
        page.add(labelBox2, 0, row++, 4, 1);
        Friend[] friendSelection = new Friend[1];
        friendSelection[0] = null;
        ToggleGroup friendOptions = new ToggleGroup();
        
        for(Friend friend : controller.getFriendList()){
            RadioButton curr = new RadioButton(friend.getUsername());
            curr.setOnAction(event -> friendSelection[0] = friend);
            curr.setToggleGroup(friendOptions);
            page.add(curr, 1, row++, 2, 1);
            
            //Style
            curr.getStyleClass().add("label");
        }

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
                
                //Reset pannel
                addNewGameButton();
            }
            
        });
        page.add(createGame, 1, row); //Do not increase row to add close button to same row
        
        //Create close menu button
        Button closeNewGameMenu = new Button("Close");
        closeNewGameMenu.setOnMouseClicked(event -> addNewGameButton());
        page.add(closeNewGameMenu, 2 , row++ );
        
        //Style
        header.getStyleClass().add("header");
        label1.getStyleClass().add("header");
        label2.getStyleClass().add("header");
        
        Region leftRegion = new Region();
        Region rightRegion = new Region();
        page.add(leftRegion, 0, 2);
        page.add(rightRegion, 3, 2);
        GridPane.setHgrow(leftRegion, Priority.ALWAYS);
        GridPane.setHgrow(rightRegion, Priority.ALWAYS);
        
        GridPane.setHgrow(headerBox, Priority.ALWAYS);
        GridPane.setHgrow(labelBox1, Priority.ALWAYS);
        GridPane.setHgrow(labelBox2, Priority.ALWAYS);
        
        headerBox.setAlignment(Pos.CENTER);
        labelBox1.setAlignment(Pos.CENTER);
        labelBox2.setAlignment(Pos.CENTER);
    }
}
