package chessBug.login;

import chessBug.preferences.PreferencesController;
import chessBug.profile.ProfileModel;
import org.json.JSONObject;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;

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

        // Jump from username to password fields
        usernameField.setOnAction(event -> passwordField.requestFocus());

        // 2FA field (initially hidden)
        TextField otpField = new TextField();
        otpField.setPromptText("Enter 2FA Code (if enabled)");
        otpField.getStyleClass().add("loginField");
        otpField.setVisible(false); // Hide OTP field initially

        // Login Button
        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #5865F2; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5px;");
        loginButton.setDefaultButton(true);
        loginButton.setOnAction(event -> {
            // First, try to log in with the username and password
            JSONObject response = handleLogin.handle(usernameField.getText(), passwordField.getText());

            // If login is unsuccessful, show error
            if (response.getBoolean("error")) {
                errorTitle.setText("Login Failed");
                errorDescription.setText(response.getString("response"));
                if (!loginPage.getChildren().contains(errorTitle)) {
                    loginPage.getChildren().addAll(errorTitle, errorDescription);
                }
            } else {
                // Check if 2FA is enabled
                boolean is2FAEnabled = response.has("2fa") && response.getBoolean("2fa");

                if (is2FAEnabled) {
                    // Show the OTP field if 2FA is enabled
                    otpField.setVisible(true);
                    
                    // Check for 2FA secret key
                    if (!response.has("secretKey")) {
                        errorTitle.setText("2FA Setup Error");
                        errorDescription.setText("No secret key found for 2FA validation.");
                        if (!loginPage.getChildren().contains(errorTitle)) {
                            loginPage.getChildren().addAll(errorTitle, errorDescription);
                        }
                        return;
                    }
                    
                    String secretKey = response.getString("secretKey");
                    String otpText = otpField.getText().trim();

                    // Validate 2FA code
                    if (otpText.isEmpty()) {
                        errorTitle.setText("2FA Required");
                        errorDescription.setText("Please enter your 2FA code.");
                        if (!loginPage.getChildren().contains(errorTitle)) {
                            loginPage.getChildren().addAll(errorTitle, errorDescription);
                        }
                        return;
                    }

                    try {
                        int otp = Integer.parseInt(otpText);
                        GoogleAuthenticator gAuth = new GoogleAuthenticator();
                        boolean isValid = gAuth.authorize(secretKey, otp);

                        if (!isValid) {
                            errorTitle.setText("Invalid 2FA Code");
                            errorDescription.setText("Please enter the correct 2FA code.");
                            if (!loginPage.getChildren().contains(errorTitle)) {
                                loginPage.getChildren().addAll(errorTitle, errorDescription);
                            }
                            return;
                        }
                    } catch (NumberFormatException e) {
                        errorTitle.setText("Invalid Code");
                        errorDescription.setText("2FA code must be a number.");
                        if (!loginPage.getChildren().contains(errorTitle)) {
                            loginPage.getChildren().addAll(errorTitle, errorDescription);
                        }
                        return;
                    }
                }

                // If login is successful or 2FA is validated
                System.out.println("Login successful!");
                // Proceed with app flow after successful login
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
        loginPage.getChildren().addAll(loginTitle, subtitle, usernameField, passwordField, otpField, loginButton, createAccountButton);
        StackPane page = new StackPane(loginPage);
        page.setStyle("-fx-width: 33.33%; -fx-alignment: center;");
        return page;
    }
    
    public void savedLogin() throws Exception {
        // Check that there is a string returned
        if (PreferencesController.getUsername().isBlank() ||
                PreferencesController.getPassword().isBlank())
            throw new Exception();
        // Try to login
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
