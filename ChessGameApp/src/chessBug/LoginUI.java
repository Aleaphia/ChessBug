package chessBug;

import org.json.JSONObject;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class LoginUI {
    private VBox page;

    public LoginUI(LoginHandle handleLogin, LoginHandle handleAccountCreation) {
        page = createLoginPage(handleLogin, handleAccountCreation);
    }

    private VBox createLoginPage(LoginHandle handleLogin, LoginHandle handleAccountCreation) {
        // Use to report errors to the user
        Label errorTitle = new Label(), errorDescription = new Label();
        errorTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #FF5555;");
        errorDescription.setStyle("-fx-text-fill: #FF5555;");

        VBox loginPage = new VBox(15);
        loginPage.setAlignment(Pos.CENTER);
        loginPage.setPadding(new Insets(40));
        loginPage.setStyle("-fx-background-color: #36393F; -fx-text-fill: white;");

        // Title
        Label loginTitle = new Label("Welcome back!");
        loginTitle.setFont(Font.font("Arial", 24));
        loginTitle.setTextFill(Color.WHITE);

        Label subtitle = new Label("We're so excited to see you again!");
        subtitle.setTextFill(Color.web("#b9bbbe"));
        subtitle.setFont(Font.font("Arial", 14));

        // Username and Password fields
        TextField usernameField = new TextField();
        usernameField.setPromptText("Email or Username");
        usernameField.setStyle("-fx-background-color: #40444B; -fx-text-fill: white; -fx-border-radius: 5px; -fx-padding: 10px;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-background-color: #40444B; -fx-text-fill: white; -fx-border-radius: 5px; -fx-padding: 10px;");

        // Login Button
        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #5865F2; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5px;");
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
        Button createAccountButton = new Button("Create an account");
        createAccountButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #4E8AF3;");
        createAccountButton.setOnAction(event -> {
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
        return loginPage;
    }

    public VBox getPage() {
        return page;
    }

    public interface LoginHandle {
        JSONObject handle(String username, String password);
    }
}
