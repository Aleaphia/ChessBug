package chessBug.login;

import org.json.JSONObject;

import chessBug.preferences.PreferencesController;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;


public class LoginUI {
    private Node page;
    LoginHandle handleSavedLogin;

    public LoginUI(LoginHandle handleLogin, LoginHandle handleAccountCreation, LoginHandle handleSavedLogin) {
        this.handleSavedLogin = handleSavedLogin;
        page = createLoginPage(handleLogin, handleAccountCreation);
    }

    private Node createLoginPage(LoginHandle handleLogin, LoginHandle handleAccountCreation) {
        // Use to report errors to the user
        Label errorTitle = new Label(), errorDescription = new Label();
        errorTitle.getStyleClass().addAll("h2", "error");
        errorDescription.getStyleClass().addAll("error");

        VBox loginPage = new VBox(15);
        loginPage.setAlignment(Pos.CENTER);
        loginPage.setPadding(new Insets(40));
        loginPage.getStyleClass().add("loginPane");

        // Title
        Label loginTitle = new Label("Welcome back!");
        loginTitle.getStyleClass().add("h1");

        Label subtitle = new Label("We're so excited to see you again!");
        subtitle.getStyleClass().add("h2");

        // Username and Password fields
        TextField usernameField = new TextField();
        usernameField.setPromptText("Email or Username");
        usernameField.getStyleClass().add("loginField");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("loginField");

        //Jump from username to password filds
        usernameField.setOnAction(event -> {
            passwordField.requestFocus();
        });

        // Entry Animation
        loginPage.setOpacity(0);
        loginPage.setScaleX(0.95);
        loginPage.setScaleY(0.95);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), loginPage);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(500), loginPage);
        scaleIn.setFromX(0.95);
        scaleIn.setFromY(0.95);
        scaleIn.setToX(1);
        scaleIn.setToY(1);

        SequentialTransition entryAnimation = new SequentialTransition(fadeIn, scaleIn);
        entryAnimation.play();

        
        // Login Button
        Button loginButton = new Button("Login");
        loginButton.setDefaultButton(true);
        loginButton.setOnAction(event -> {
            if (loginPage.getChildren().contains(errorTitle)){
                //Remove the last two children (errorTitle and errorDescription)
                loginPage.getChildren().remove(loginPage.getChildren().size() - 1);
                loginPage.getChildren().remove(loginPage.getChildren().size() - 1);
            }
            JSONObject response = handleLogin.handle(usernameField.getText(), passwordField.getText());
            if (response.getBoolean("error")) {
                errorTitle.setText("Login Failed");
                errorDescription.setText("Incorrect username or password");
                System.out.println(!loginPage.getChildren().contains(errorTitle));
                loginPage.getChildren().addAll(errorTitle, errorDescription);
            }
        });
        //Alternative login option (press enter from password)
        passwordField.setOnAction(event -> loginButton.fire());

        // Create Account Button
        Label createAccountButton = new Label("Create an account");
        createAccountButton.getStyleClass().add("createAccountButton");
        createAccountButton.setOnMouseClicked(event -> {
            if (loginPage.getChildren().contains(errorTitle)){
                //Remove the last two children (errorTitle and errorDescription)
                loginPage.getChildren().remove(loginPage.getChildren().size() - 1);
                loginPage.getChildren().remove(loginPage.getChildren().size() - 1);
            }
            JSONObject response = handleAccountCreation.handle(usernameField.getText(), passwordField.getText());
            if (response.getBoolean("error")) {
                errorTitle.setText("Account Creation Failed");
                errorDescription.setText("Invalid account credentials");
                loginPage.getChildren().addAll(errorTitle, errorDescription);
            }
        });

        // Add components to the login page
        loginPage.getChildren().addAll(loginTitle, subtitle, usernameField, passwordField, loginButton, createAccountButton);
        StackPane page = new StackPane(loginPage);
        page.setStyle("-fx-width: 33.33%; -fx-alignment: center;");
        return page;
    }
    
    public void savedLogin() throws Exception{
        //Check that there is a string returned
        if (PreferencesController.getUsername().isBlank() ||
                PreferencesController.getPassword().isBlank())
            throw new Exception();
        //Try to login
        JSONObject response = handleSavedLogin.handle(
                PreferencesController.getUsername(),
                PreferencesController.getPassword());
        if (response.getBoolean("error")) {
            throw new Exception();
        }
    }

    public Node getPage() {
        return page;
    }

    public interface LoginHandle {
        JSONObject handle(String username, String password);
    }
}
