/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package chessBug;
/*

//Loop to recheck the database for updates
Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), (ActionEvent event) -> {
            //Add repeated database checks here ================================
            updateMsgBoard();
            
            // =================================================================
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
*/

import java.io.InputStream;

import org.json.JSONObject;

import chessBug.game.GameController;
import chessBug.home.HomeController;
import chessBug.network.Client;
import chessBug.network.ClientAuthException;
import chessBug.preferences.PreferencesController;
import chessBug.profile.ProfileController;
import chessBug.login.LoginUI;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class ChessBug extends Application {
    //Global variables
    private Scene mainScene;
    final private StackPane page = new StackPane(); // space to change with page details
    private HBox mainPane = new HBox();
    private GridPane loginPane;
    private Client client;
    
    @Override
    public void start(Stage primaryStage) {
        //Create stage layout
        createLoginPage(); //Set up loginPane
        //Scene and Stage
        primaryStage.setTitle("ChessBug"); //Name for application stage
        mainScene = new Scene(loginPane, 950, 600); //Add loginPane to the mainScene
        primaryStage.setScene(mainScene);//Add mainScene to primaryStage
        
        //Style
        mainScene.getStylesheets().addAll("login.css", "Styles.css");
        HBox.setHgrow(page, Priority.ALWAYS); //Makes page take up all avaiable space
        
        //Display
        primaryStage.show();
    }
    
    private void createLoginPage(){
        loginPane = new GridPane();
        loginPane.getStyleClass().addAll("background", "login");

        // Set up sizing constraints, middle is always 300x480, and everything else grows and shrinks around it
        RowConstraints row = new RowConstraints(0, 0, Double.MAX_VALUE, Priority.ALWAYS, VPos.CENTER, true),
                   rowMain = new RowConstraints(480, 480, 480);
        loginPane.getRowConstraints().addAll(row, rowMain, row);
        ColumnConstraints column = new ColumnConstraints(0, 0, Double.MAX_VALUE, Priority.ALWAYS, HPos.CENTER, true),
                      columnMain = new ColumnConstraints(300, 300, 300);
        loginPane.getColumnConstraints().addAll(column, columnMain, column);

         LoginUI loginUI = new LoginUI(
            (String username, String password) -> { // Handle login
                JSONObject out = new JSONObject();
                try {
                    client = new Client(username, password);
                    out.put("error", false);
                    //Create Menu
                    mainPane.getChildren().clear();
                    mainPane.getChildren().addAll(createSidebar(), page);
                    mainPane.getStyleClass().addAll("background");
                    mainScene.setRoot(mainPane);
                } catch (ClientAuthException e) {
                    e.printStackTrace();
                    out.put("error", true);
                    out.put("response", e.getServerResponse());
                }
                return out;
            },
            (String username, String password) -> { // Handle account creation
                JSONObject out = new JSONObject();
                try {
                    client = Client.createAccount(username, password, "placeholder@email.com");
                    out.put("error", false);
                    //Create Menu
                    mainPane.getChildren().clear();
                    mainPane.getChildren().addAll(createSidebar(), page);
                    mainScene.setRoot(mainPane);
                } catch(ClientAuthException e){
                    e.printStackTrace();
                    out.put("error", true);
                    out.put("response", e.getServerResponse());
                }

                return out;
            }
        );

        // mainPane.getChildren().add(loginUI.getPage());
        loginPane.add(loginUI.getPage(), 1, 1);
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10); // Vertical layout for sidebar
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setStyle("-fx-background-color: #2f3136; -fx-text-fill: white;");
    
        // Add logo or image to the sidebar
        ImageView logo = new ImageView(new Image("file:logo.png")); // Will need to be replaced
        logo.setFitHeight(50);
        logo.setFitWidth(50);
    
        // Add items to the sidebar
        sidebar.getChildren().addAll(
                logo,
                createSideBarButton("Home.png", event -> changePage(new HomeController(client).getPage(), "home")),
                createSideBarButton("Chess.png", event -> changePage(new GameController(client).getPage(), "game")),
                createSideBarButton("Gear.png", event -> changePage(new PreferencesController().getPage())),
                createSideBarButton("User.png", event -> changePage(new ProfileController(client).getPage(), "profile")),
                createSideBarButton("Logout.png", event -> {
                    mainScene.setRoot(loginPane);
                    mainScene.getStylesheets().add("login.css");
                }));
    
        return sidebar;
    }
    private void changePage(Pane newPage){
        //Clear and add new page
        page.getChildren().clear();
        page.getChildren().add(newPage);
        mainScene.getStylesheets().clear();
        mainScene.getStylesheets().add("Styles.css");
    }
     private void changePage(Pane newPage, String stylePage){
        //Clear and add new page
        changePage(newPage);
        mainScene.getStylesheets().add(stylePage + ".css");
    }

    private Button createSideBarButton(String imageFileName, EventHandler<ActionEvent> eventHandler) {
        //Create button
        Button button = new Button();

        //Load the image based on the provided image file name 
        InputStream i = ChessBug.class.getResourceAsStream("/resources/images/"+imageFileName);
        Image image = null;
        if(i != null)
            image = new Image(i);

        if (image == null || image.isError()) {
            System.out.println("Error loading image:" + imageFileName);
        }

        // Create ImageView for graphic
        ImageView imageView = new ImageView(image);

        imageView.setFitWidth(50);
        imageView.setFitHeight(50);
        imageView.setPreserveRatio(true);

        button.setGraphic(imageView);
        
        //Create function
        button.setOnAction(eventHandler);

        button.setStyle("-fx-border-width: 0;");

        return button;
    }    
    public static void main(String[] args) {
        Application.launch(args);
    }
}
