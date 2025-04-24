/**  ___  _                    _
 *  / _ \| |    ___   __   __  ||_   _ _   __-,
 * | |__,| = \ //_\\ //_\ //_\ |[]\ ||_|| //\ |
 *  \___/|_||| \\_// \_// \_// |[]/  \_/  \\/_|
 *                                       \\_//
 *
 *    |\  /\  /\  /|
 *    | \/  \/  \/ |
 *    |            |
 *    | _.------._ |
 *   ./^ O  ||   0^\.
 * ./    _.----._    \.
 * | O  / _    _ \   C|
 *  \__|_/_\__/_\_|__/
 *  /  /  |    |  \  \
 * _| -   ^    ^   - |_
 *
 * A project by Shoshana Altman, Robert Epps, and Zander Gall
 */

package chessBug;

import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONObject;

import chessBug.game.GameController;
import chessBug.home.HomeController;
import chessBug.login.LoginUI;
import chessBug.network.Client;
import chessBug.network.ClientAuthException;
import chessBug.network.DatabaseCheckList;
import chessBug.preferences.PreferencesController;
import chessBug.preferences.PreferencesPage;
import chessBug.profile.ProfileController;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ChessBug extends Application {

    private static final int DATABASE_CHECK_INTERVAL = 1;

    //View
    private Scene mainScene;
    final private StackPane mainPage = new StackPane();
    private HBox mainPane = new HBox();
    private GridPane loginPane;

    //Database Connections
    private Client client;

    // A list of database checking tasks to run in the background every once in a while
    private DatabaseCheckList databaseCheckList = new DatabaseCheckList();

    // Determine whether currently logged in, for "stay logged in"
    private boolean isLoggedIn = false;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.getIcons().add(new Image(ChessBug.class.getResourceAsStream("/resources/images/Crown.png")));

        LoginUI loginUI = createLoginPage(); //Set up loginPane

        //Scene and Stage
        primaryStage.setTitle("ChessBug"); //Name for application stage
        primaryStage.setMaximized(true); //Makes the stage full screne while leaving the exit buttons
        mainScene = new Scene(loginPane, 1200, 600); //Add loginPane to the mainScene
        primaryStage.setScene(mainScene);//Add mainScene to primaryStage

        //Style
        PreferencesController.applyStyles(mainScene, "Styles", "Login");
        HBox.setHgrow(mainPage, Priority.ALWAYS); //Makes page take up all avaiable space
        PreferencesController.setVolume();

        if (PreferencesController.isStayLoggedIn()){
            //Check for credentials
            try{loginUI.savedLogin();}
            catch(Exception e){}
        }

        //Display
        primaryStage.show();

        startDatabaseChecks();
    }

    @Override
    public void stop(){
        //Save credientials
        if (isLoggedIn && PreferencesController.isStayLoggedIn()) {
            PreferencesController.setLoginCredentials(client.getProfile().getUsername(), client.getProfile().getPassword());
        } else {
            PreferencesController.setLoginCredentials("", "");
        }
    }

    // Run a series of server calls to ensure the client has up to date information
    private void startDatabaseChecks(){
        //Check database
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(DATABASE_CHECK_INTERVAL), (e) -> databaseCheckList.preformChecks()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private LoginUI createLoginPage(){
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
            (String username, String password) -> {
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

        loginPane.add(loginUI.getPage(), 1, 1);
        return loginUI;
    }

    private void successfulLogin() {
        isLoggedIn = true;
        //Create Menu
        mainPane.getChildren().clear();
        mainPane.getChildren().addAll(createSidebar(), mainPage);
        mainPane.getStyleClass().addAll("background");
        mainScene.setRoot(mainPane);
        //Open page
        changePage(new HomeController(client,databaseCheckList).getPage(), "Styles", "Menu", "HomeView", "Game");
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10); // Vertical layout for sidebar
        sidebar.setPadding(new Insets(10, 10, 10, 10));        
        sidebar.getStyleClass().add("sideBar");

        // Add logo or image to the sidebar
        ImageView logo = new ImageView(new Image(ChessBug.class.getResourceAsStream("/resources/images/Crown.png")));
        logo.setFitHeight(53);
        logo.setFitWidth(60);
        StackPane logoHolder = new StackPane(logo);
        logoHolder.getStyleClass().add("logoImage");

        // Add items to the sidebar
        // Every item comes with a page, and a list of stylesheet names, every page uses "Styles" and "Menu", and can choose its own set of stylesheets
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
                    isLoggedIn = false; // Log out
                    mainScene.setRoot(loginPane);
                    PreferencesController.playButtonSound();
                    PreferencesController.applyStyles(mainScene, "Styles", "Menu", "Login");
                }));

        return sidebar;
    }
     private void changePage(Pane newPage, String... stylePage){
        PreferencesController.playButtonSound();

        //Clear and add new page
        mainPage.getChildren().clear();
        mainPage.getChildren().add(fadeIn(newPage));
        PreferencesController.applyStyles(mainScene, stylePage); 
    }

    // A function to fade in pages
    private <T extends Region> T fadeIn(T node) {
        FadeTransition fade = new FadeTransition(Duration.millis(600), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
        return node;
    }

    // A function to load animated icons
    private Image[] loadAnimation(String animDirectory) {
        // A list of stored images
        ArrayList<Image> list = new ArrayList<>();

        // Load as many frames as we can find from the given directory (0.png, 1.png, 2.png, 3.png...)
        for(int i = 0; true; i++) {
            InputStream s = getClass().getResourceAsStream(animDirectory + i + ".png");
            if(s == null)
                break;
            list.add(new Image(s));
        }

        // Return just an array of images rather than an arraylist, with a default value if no frames were found
        Image[] out = new Image[list.size()];
        if(list.isEmpty())
            return new Image[]{new Image(getClass().getResourceAsStream("/resources/images/Crown.png"))};
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

            private final long FRAME_INTERVAL_NS = 50_000_000l;

            long prev = 0;
            int frame = 0;
            
            @Override
            public void start() {
                prev = 0;
                frame = 0;
                super.start();
            }

            @Override public void handle(long now) {
                if((now - prev) > FRAME_INTERVAL_NS) {
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

    // Default main function
    public static void main(String[] args) {
        Application.launch(args);
    }
}
