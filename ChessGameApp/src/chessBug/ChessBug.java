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
import java.util.ArrayList;

import org.json.JSONObject;

import com.warrenstrange.googleauth.GoogleAuthenticator;

import chessBug.game.GameController;
import chessBug.home.HomeController;
import chessBug.login.LoginUI;
import chessBug.network.Client;
import chessBug.network.ClientAuthException;
import chessBug.preferences.PreferencesController;
import chessBug.preferences.PreferencesPage;
import chessBug.profile.ProfileController;
import chessBug.network.DatabaseCheckList;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ChessBug extends Application {
    //Global variables
    private Scene mainScene;
    final private StackPane page = new StackPane(); // space to change with page details
    private HBox mainPane = new HBox();
    private GridPane loginPane;
    private Client client;
    private DatabaseCheckList databaseCheckList = new DatabaseCheckList(); 
    private String settingsFile = "settings.dat";
    
    @Override
    public void start(Stage primaryStage) {
        LoginUI loginUI = createLoginPage(); //Set up loginPane
        
        //Scene and Stage
        primaryStage.setTitle("ChessBug"); //Name for application stage
        mainScene = new Scene(loginPane, 1600, 800); //Add loginPane to the mainScene
        primaryStage.setScene(mainScene);//Add mainScene to primaryStage
        
        //Style
        PreferencesController.applyStyles(mainScene, "Styles", "Login");
        HBox.setHgrow(page, Priority.ALWAYS); //Makes page take up all avaiable space
        
        if (PreferencesController.isStayLoggedIn()){
            //Check for credentials
            try{loginUI.savedLogin();}
            catch(Exception e){}
        }
        
        
        //Display
        primaryStage.show();
        
        continueDatabaseChecks();
    }
    
    @Override
    public void stop(){
        //Save credientials
        if (PreferencesController.isStayLoggedIn()){
            PreferencesController.setLogginCredentials(client.getProfile().getUsername(), client.getProfile().getPassword());
        }
        else{
            PreferencesController.setLogginCredentials("", "");
        }
    }
    
    private void continueDatabaseChecks(){
        //Check database
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), (ActionEvent event) -> {
            //System.out.println("DEBUG: " + databaseCheckList.size() + "--------------" + databaseCheckList);
            databaseCheckList.preformChecks();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    private LoginUI createLoginPage() {
    loginPane = new GridPane();
    loginPane.getStyleClass().addAll("background", "login");

    // Set up sizing constraints, middle is always 300x480, and everything else grows and shrinks around it
    RowConstraints row = new RowConstraints(0, 0, Double.MAX_VALUE, Priority.ALWAYS, VPos.CENTER, true),
                    rowMain = new RowConstraints(480, 480, 480);
    loginPane.getRowConstraints().addAll(row, rowMain, row);
    ColumnConstraints column = new ColumnConstraints(0, 0, Double.MAX_VALUE, Priority.ALWAYS, HPos.CENTER, true),
                          columnMain = new ColumnConstraints(300, 300, 300);
    loginPane.getColumnConstraints().addAll(column, columnMain, column);

    // Create LoginUI instance with login, account creation, and saved login handlers
    LoginUI loginUI = new LoginUI(
        (String username, String password) -> { // Handle login
            JSONObject out = new JSONObject();
            try {
                client = new Client(username, password);
                boolean is2FAEnabled = client.is2FAEnabled();  // Check if 2FA is enabled for the user
                if (is2FAEnabled) {
                    String secretKey = client.get2FASecretKey();  // Get 2FA secret key
                    out.put("error", false);
                    out.put("2fa", true);        // 2FA enabled
                    out.put("secretKey", secretKey);  // Include secret key in the response
                } else {
                    out.put("error", false);
                    out.put("2fa", false);       // 2FA not enabled
                }
                successfulLogin();
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
                successfulLogin();
            } catch(ClientAuthException e){
                e.printStackTrace();
                out.put("error", true);
                out.put("response", e.getServerResponse());
            }

            return out;
        },
        (String username, String password) -> { // Handle saved login (pre-hashed)
            JSONObject out = new JSONObject();
            try {
                client = Client.loginPreHashed(username, password);
                out.put("error", false);
                successfulLogin();
            } catch (ClientAuthException e) {
                e.printStackTrace();
                out.put("error", true);
                out.put("response", e.getServerResponse());
            }
            return out;
        }
    );

    loginPane.add((Node) loginUI.getPage(), 1, 1);

    // Add 2FA logic handling when 2FA is enabled
    TextField otpField = new TextField();
    otpField.setPromptText("Enter 2FA Code");
    otpField.setVisible(false); // Hidden by default

    Button verifyOTPButton = new Button("Verify 2FA");
    verifyOTPButton.setStyle("-fx-background-color: #5865F2; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5px;");
    verifyOTPButton.setVisible(false); // Hidden by default

    verifyOTPButton.setOnAction(event -> {
        // Assuming the user has entered an OTP
        String otpText = otpField.getText().trim();
        if (otpText.isEmpty()) {
            // Show error message if OTP field is empty
            showError("OTP is required", "Please enter your 2FA code.");
            return;
        }

        try {
            int otp = Integer.parseInt(otpText);
            GoogleAuthenticator gAuth = new GoogleAuthenticator();
            String secretKey = client.get2FASecretKey();  // Get the secret key from the server
            boolean isValid = gAuth.authorize(secretKey, otp);

            if (isValid) {
                // If OTP is valid, proceed with successful login
                successfulLogin();
            } else {
                // Show error if OTP is invalid
                showError("Invalid 2FA Code", "Please enter the correct 2FA code.");
            }
        } catch (NumberFormatException e) {
            // Show error if OTP is not a valid number
            showError("Invalid Code", "2FA code must be a number.");
        }
    });

    // Add OTP field and verify button to the login pane
    loginPane.add(otpField, 1, 2);  // Add OTP input field
    loginPane.add(verifyOTPButton, 1, 3);  // Add verify OTP button

    return loginUI;
}

private void showError(String title, String description) {
    // Show error messages in the UI
    System.out.println(title + ": " + description);  // Or implement custom error display logic
    // You can add error handling in your UI as needed
}

private void successfulLogin() {
    // Create Menu
    mainPane.getChildren().clear();
    mainPane.getChildren().addAll(createSidebar(), page);
    mainPane.getStyleClass().addAll("background");
    mainScene.setRoot(mainPane);
    // Open page
    changePage(new HomeController(client, databaseCheckList).getPage(), "Styles", "Menu", "HomeView", "Game");
}

    private VBox createSidebar() {
        VBox sidebar = new VBox(10); // Vertical layout for sidebar
        sidebar.setPadding(new Insets(10, 10, 10, 10));        
        // sidebar.getStylesheets().add(getClass().getResource("/menu.css").toExternalForm());
        sidebar.getStyleClass().add("sideBar");

    
        // Add logo or image to the sidebar
        ImageView logo = new ImageView(new Image(ChessBug.class.getResourceAsStream("/resources/images/GoldCrown.png"))); // Will need to be replaced
        logo.setFitHeight(53);
        logo.setFitWidth(60);
        StackPane logoHolder = new StackPane(logo);
        logoHolder.getStyleClass().add("logoImage");

        // Add items to the sidebar
        sidebar.getChildren().addAll(
                logoHolder,
                createSideBarButton("home", event -> {
                    databaseCheckList.clear();
                    changePage(new HomeController(client, databaseCheckList).getPage(), "Styles", "Menu", "HomeView", "Game");
                }),
                createSideBarButton("chess", event -> {
                    databaseCheckList.clear();
                    changePage(new GameController(client, databaseCheckList).getPage(), "Styles", "Menu", "Game");
                }),
                createSideBarButton("gear", event -> {
                    databaseCheckList.clear();
                    changePage(new PreferencesPage(client).getPage(), "Styles", "Menu");
                }),
                createSideBarButton("user", event -> {
                    databaseCheckList.clear();
                    changePage(new ProfileController(client).getPage(), "Styles", "Menu", "Profile");
                }),
                createSideBarButton("logout", event -> {
                    databaseCheckList.clear();
                    mainScene.setRoot(loginPane);
                    PreferencesController.applyStyles(mainScene, "Styles", "Menu", "Login");
                }));
    
        return sidebar;
    }
     private void changePage(Pane newPage, String... stylePage){
        //Clear and add new page
        page.getChildren().clear();
        page.getChildren().add(newPage);
        PreferencesController.applyStyles(mainScene, stylePage); 
    }

    private Image[] loadAnimation(String animDirectory) {
        ArrayList<Image> list = new ArrayList<>();

        for(int i = 0; true; i++) {
            InputStream s = getClass().getResourceAsStream(animDirectory + i + ".png");
            if(s == null)
                break;
            list.add(new Image(s));
        }

        Image[] out = new Image[list.size()];
        if(list.size() == 0)
            return new Image[]{new Image(getClass().getResourceAsStream("/resources/images/GoldCrown.png"))};
        return list.toArray(out);
    }

    private Button createSideBarButton(String iconClass, EventHandler<ActionEvent> eventHandler) {
        //Create button
        Button button = new Button();

        // Load all necessary images, and give the button an imageview
        Image[] images = loadAnimation("/resources/images/icons/" + iconClass + "/");
        ImageView imageView = new ImageView(images[0]);
        button.graphicProperty().set(imageView);

        // Create an animation that loops through all the animation images for this icon
        AnimationTimer animateHover = new AnimationTimer() {
            long prev = 0;
            int frame = 0;
            
            @Override
            public void start() {
                prev = 0;
                frame = 0;
                super.start();
            }

            public void handle(long now) {
                if((now - prev) > 50_000_000l) {
                    prev = now;
                    imageView.setImage(images[frame]);
                    frame++;
                    if(frame >= images.length)
                        stop();
                }
            }
        };

        //Add style classes and animation handling
        button.getStyleClass().addAll("buttonIcon", iconClass);
        button.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> { 
            animateHover.start();
        });
        button.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            animateHover.stop();
            imageView.setImage(images[0]);
        });

        button.setOnAction(eventHandler);

        return button;
    }    
    
    public static void main(String[] args) {
        Application.launch(args);
    }
}
