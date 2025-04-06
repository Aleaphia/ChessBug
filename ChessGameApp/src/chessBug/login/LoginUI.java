package chessBug.login;

import chessBug.preferences.PreferencesController;
import chessBug.profile.ProfileModel;
import org.json.JSONObject;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

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
        usernameField.setOnAction(event -> passwordField.requestFocus());
        
        // Login Button
        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #5865F2; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5px;");
        loginButton.setDefaultButton(true);
        loginButton.setOnAction(event -> {
            JSONObject response = handleLogin.handle(usernameField.getText(), passwordField.getText());
            if (response.getBoolean("error")) {
                errorTitle.setText("Login Failed");
                errorDescription.setText(response.getString("response"));
                if (!loginPage.getChildren().contains(errorTitle)) {
                    loginPage.getChildren().addAll(errorTitle, errorDescription);
                }
            }
        });

        // Create Account Button
        Label createAccountButton = new Label("Create an account");
        createAccountButton.getStyleClass().add("createAccountButton");
        createAccountButton.setOnMouseClicked(event -> {
            JSONObject response = handleAccountCreation.handle(usernameField.getText(), passwordField.getText());
            if (response.getBoolean("error")) {
                errorTitle.setText("Account Creation Failed");
                errorDescription.setText(response.getString("response"));
                if (!loginPage.getChildren().contains(errorTitle)) {
                    loginPage.getChildren().addAll(errorTitle, errorDescription);
                }
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
