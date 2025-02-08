package chessBug;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import org.json.JSONObject;

public class LoginUI {
    private VBox page;

    public LoginUI(LoginHandle handleLogin, LoginHandle handleAccountCreation) {
        page = createLoginPage(handleLogin, handleAccountCreation);
    }

    private VBox createLoginPage(LoginHandle handleLogin, LoginHandle handleAccountCreation) {
        // Use to report errors to the user
        Label errorTitle = new Label(), errorDescription = new Label();
        errorTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        errorDescription.setStyle("-fx-text-fill: white;");

        VBox loginPage = new VBox(20);
        loginPage.setPadding(new Insets(40, 40, 40, 40));
        loginPage.setStyle("-fx-background-color: #36393F; -fx-text-fill: white;");
        // Title
        Label loginTitle = new Label("Login to ChessBug");
        loginTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        // Username and Password fields
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.setStyle("-fx-background-color: #444750; -fx-text-fill: white;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setStyle("-fx-background-color: #444750; -fx-text-fill: white;");
        // Buttons for login and account creation
        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #4E8AF3; -fx-text-fill: white;");
        loginButton.setOnAction(event -> {
            JSONObject response = handleLogin.handle(usernameField.getText(), passwordField.getText());
            if(response.getBoolean("error")) {
                errorTitle.setText("Could not log in!");
                errorDescription.setText(response.getString("response"));
                if(!loginPage.getChildren().contains(errorTitle)) {
                    loginPage.getChildren().addAll(errorTitle, errorDescription);
                }
            }
        });
        Button createAccountButton = new Button("Create Account");
        createAccountButton.setStyle("-fx-background-color: #4E8AF3; -fx-text-fill: white;");
        createAccountButton.setOnAction(event -> {
            JSONObject response = handleAccountCreation.handle(usernameField.getText(), passwordField.getText());
            if(response.getBoolean("error")) {
                errorTitle.setText("Could not log in!");
                errorDescription.setText(response.getString("response"));
                if(!loginPage.getChildren().contains(errorTitle)) {
                    loginPage.getChildren().addAll(errorTitle, errorDescription);
                }
            }
        });
        // Add components to the login page
        loginPage.getChildren().addAll(loginTitle, usernameField, passwordField, loginButton, createAccountButton);
        return loginPage;
    }

    public VBox getPage() {
        return page;
    }

    public static interface LoginHandle {
        public JSONObject handle(String username, String password);
    }
}
