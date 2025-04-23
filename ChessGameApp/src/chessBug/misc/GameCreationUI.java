package chessBug.misc;

import chessBug.controllerInterfaces.IGameCreationController;
import chessBug.network.Friend;
import chessBug.network.NetworkException;
import chessBug.preferences.PreferencesController;

import java.util.Random;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;


public class GameCreationUI {
    private final GridPane page = new GridPane();
    private final IGameCreationController controller;
    
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
        buttonBox.setAlignment(Pos.CENTER);
        page.getStyleClass().add("grid");
    }
    
    private void buildGameCreationPrompt(){
        int row = 0;
        //Clear page
        page.getChildren().clear();
        
        //Title layout
        Label header = new Label("Create New Game");
        VBox headerBox = new VBox(header);
        page.add(headerBox, 0, row++, 4, 1);
        
        //Select Color ---------------------------------------------------------
        //Hold color info
        char[] colorSelection = new char[1];
        colorSelection[0] = '0'; //w for white, b for black, r for random
        
        //Layout
        Label label1 = new Label("Select color:");
        VBox labelBox1 = new VBox(label1);
        page.add(labelBox1, 0, row++, 4, 1);
        
        ToggleGroup colorOptions = new ToggleGroup();
        String[] colorOptionList = {"white", "black", "random"};
        for (String option : colorOptionList) {
            RadioButton curr = new RadioButton(option);
            curr.setOnAction(event -> {
                PreferencesController.playButtonSound();
                colorSelection[0] = option.charAt(0);
            });
            curr.setToggleGroup(colorOptions);
            page.add(curr, 1, row++, 2, 1);
            
            //Style
            curr.getStyleClass().add("label");
        }

        //Challenge friend: list friends in radio buttons ----------------------
        Friend[] friendSelection = new Friend[1];
        friendSelection[0] = null;
        
        //Layout
        Label label2 = new Label("Challenge friend:");
        VBox labelBox2 = new VBox(label2);
        page.add(labelBox2, 0, row++, 4, 1);
        
        ToggleGroup friendOptions = new ToggleGroup();
        VBox friendBox = new VBox();
        ScrollPane scroll = new ScrollPane(friendBox);
        
        try {
            for(Friend friend : controller.getFriendList()){
                RadioButton curr = new RadioButton(friend.getUsername());
                curr.setOnAction(event -> {
                    PreferencesController.playButtonSound();
                    friendSelection[0] = friend;
                });
                curr.setToggleGroup(friendOptions);
                friendBox.getChildren().add(curr);//add(curr, 1, row++, 2, 1);
                
                //Style
                curr.getStyleClass().add("label");
            }
        } catch (NetworkException ignored) {} // We'll try again soon
        page.add(scroll, 1, row++, 2 ,1);
        
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
                try {
                    controller.sendGameRequest(playerColor, friendSelection[0]);
                    
                    //Reset pannel
                    addNewGameButton();
                } catch (NetworkException e) {
                    System.err.println("Couldn't send game request!");
                    e.printStackTrace();
                }
            }
            
        });
        page.add(createGame, 1, row); //Do not increase row to add close button to same row
        
        //Create close menu button
        Button closeNewGameMenu = new Button("Close");
        closeNewGameMenu.setOnMouseClicked(event -> addNewGameButton());
        page.add(closeNewGameMenu, 2 , row++ );
        
        //Style
        header.getStyleClass().add("h1");
        label1.getStyleClass().add("h2");
        label2.getStyleClass().add("h2");
        friendBox.getStyleClass().add("scrollBackground");
        
        //Layout - center questions and headers
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
