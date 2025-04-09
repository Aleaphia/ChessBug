package chessBug.profile;

import java.io.File;
import java.io.IOException;

import chessBug.network.Client;
import chessBug.network.NetworkException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ProfileView extends VBox {
    private Text usernameText, emailText, profileDescriptionText;
    private ImageView profileImageView;
    private ProfileController controller;
    private TextField usernameField, emailField;
    private Button changeProfilePicButton;
    private TextField bioField;
    private TextField oldPasswordField;
    private TextField newPasswordField;

    public ProfileView(ProfileController controller, Client client) {
        this.controller = controller;

        // Set various styling things
        setSpacing(20);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: rgb(212, 215, 223); -fx-border-radius: 10px;");
        setPrefSize(3840, 2160);
        setAlignment(Pos.CENTER);
        createProfileUI(client);
    }

    private void createProfileUI(Client client) {
        // Profile Banner
        Rectangle banner = new Rectangle(400, 100);
        banner.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.DARKBLUE), new Stop(1, Color.PURPLE)));

        // Profile Picture
        profileImageView = new ImageView(client.getOwnUser().getProfilePicture());
        profileImageView.setFitWidth(100);
        profileImageView.setFitHeight(100);
        profileImageView.setClip(new Circle(50, 50, 50)); // Circular profile picture

        // Profile Description Text
        profileDescriptionText = new Text("Add a brief description about yourself...");
        profileDescriptionText.setStyle("-fx-font-size: 14px; -fx-fill: gray; -fx-font-style: italic;");

        // Username and Email Text
        usernameText = new Text(controller.getModel().getUsername());
        usernameText.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        emailText = new Text(controller.getModel().getEmail());
        emailText.setStyle("-fx-font-size: 16px; -fx-fill: gray;");
        
        // Editable Fields with Placeholder Texts
        usernameField = new TextField(controller.getModel().getUsername());
        usernameField.setMaxWidth(300);
        usernameField.setPromptText("Enter your new username");

        emailField = new TextField(controller.getModel().getEmail());
        emailField.setMaxWidth(300);
        emailField.setPromptText("Enter your email address");

        //Bio Field
        bioField = new TextField(controller.getModel().getBio());
        bioField.setMaxWidth(300);
        bioField.setPromptText("Enter your bio");

        //Password Fields
        oldPasswordField = new TextField();
        oldPasswordField.setPromptText("Old Password");
        oldPasswordField.setMaxWidth(300);

        newPasswordField = new TextField();
        newPasswordField.setPromptText("New Password");
        newPasswordField.setMaxWidth(300);

        // Buttons with Updated Styles
        Button updateProfileButton = new Button("Update Profile");
        updateProfileButton.setStyle("-fx-background-color: #4e8af3; -fx-text-fill: white;");
        updateProfileButton.setOnAction(e -> updateProfile());

        changeProfilePicButton = new Button("Change Picture");
        changeProfilePicButton.setStyle("-fx-background-color: #4e8af3; -fx-text-fill: white;");
        changeProfilePicButton.setOnAction(e -> openFileChooserForProfilePic(client));

        Button updateBioButton = new Button("Update Bio");
        updateBioButton.setOnAction(e -> controller.updateBio(bioField.getText()));
        updateBioButton.setStyle("-fx-background-color: #4e8af3; -fx-text-fill: white;");

        Button resetPasswordButton = new Button("Reset Password");
        resetPasswordButton.setOnAction(e -> {
            String oldPass = oldPasswordField.getText();
            String newPass = newPasswordField.getText();
            if (oldPass.isEmpty() || newPass.isEmpty()) {
                showError("Both password fields must be filled");
            } else {
                controller.resetPassword(oldPass, newPass);
                showConfirmation();
            }           
        });
        

        // Layout - Using VBox to stack profile image, text, fields, and buttons
        StackPane profileStack = new StackPane(banner, profileImageView);
        profileStack.setAlignment(Pos.CENTER);
        profileImageView.setTranslateY(25);

        // User Info Section
        VBox userInfoSection = new VBox(10, profileStack, 
        usernameText, 
        emailText, 
        profileDescriptionText, 
        usernameField, 
        emailField, 
        updateProfileButton, 
        changeProfilePicButton,
        updateBioButton,
        oldPasswordField,
        newPasswordField,
        resetPasswordButton
        );
        userInfoSection.setAlignment(Pos.CENTER);
        userInfoSection.setSpacing(15);

        // Center everything in the VBox and ensure responsiveness
        getChildren().add(userInfoSection);
        setAlignment(Pos.CENTER);
        setPrefWidth(600);  // Set a responsive width
    }

    //Fetching user pofile data to update
    private void updateProfile() {
        String newUsername = usernameField.getText();
        String newEmail = emailField.getText();
        
        if (newUsername.isEmpty() || newEmail.isEmpty()) {
            showError("Username and Email cannot be empty");
            return;
        }
        
        if (!newEmail.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showError("Invalid email format");
            return;
        }
        
        controller.updateProfile(newUsername, newEmail);
        showConfirmation();
    }

    //Update profile view
    public void updateProfileView(ProfileModel updatedProfile) {
        if (!usernameText.getText().equals(updatedProfile.getUsername())) {
            usernameText.setText(updatedProfile.getUsername());
        }
        if (!emailText.getText().equals(updatedProfile.getEmail())) {
            emailText.setText(updatedProfile.getEmail());
        }
        
        if (!profileImageView.getImage().getUrl().endsWith(updatedProfile.getProfilePicURL())) {
            profileImageView.setImage(new Image(updatedProfile.getProfilePicURL()));
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showConfirmation() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Profile Updated");
        alert.setHeaderText(null);
        alert.setContentText("Your profile has been successfully updated");
        alert.showAndWait();
    }

    //Open files selector on system to upload new user profile picture
    private void openFileChooserForProfilePic(Client client) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        Stage stage = new Stage();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            Image selectedImage = new Image("file:" + file.getAbsolutePath());
            try {
                client.uploadProfilePicture(file); // Ensure client method handles the file upload
                profileImageView.setImage(selectedImage);
            } catch (NetworkException | IOException e) {
                System.err.println("Unable to upload profile picture!");
                showError("Unable to upload profile picture!");
                e.printStackTrace();
            }
        }
    }
}
