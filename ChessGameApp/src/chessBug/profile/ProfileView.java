package chessBug.profile;

import java.io.File;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ProfileView extends VBox {

    private Text usernameText;
    private Text emailText;
    private ImageView profileImageView;
    private ProfileModel model;

    private TextField usernameField;
    private TextField emailField;

    private Button changeProfilePicButton;

    public ProfileView(ProfileModel model) {
        this.model = model;
        setSpacing(20);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: #2f3136; -fx-text-fill: white; -fx-border-radius: 10px;");

        setPrefSize(3840, 2160);
        VBox.setVgrow(this, Priority.ALWAYS);
        


        setAlignment(Pos.CENTER);

        // Initialize UI components
        createProfileUI();
    }

    private void createProfileUI() {
        // Profile Picture
        profileImageView = new ImageView();
        if (model.getProfilePicPath() != null && !model.getProfilePicPath().isEmpty()) {
            Image profileImage = new Image("file:" + model.getProfilePicPath());
            profileImageView.setImage(profileImage);
        }
        profileImageView.setFitWidth(100);
        profileImageView.setFitHeight(100);
    
        // Username and Email Text
        usernameText = new Text(model.getUsername());
        usernameText.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        emailText = new Text(model.getEmail());
        emailText.setStyle("-fx-font-size: 16px; -fx-fill: gray;");
    
        // Editable Username and Email fields
        usernameField = new TextField(model.getUsername());
        usernameField.setStyle("-fx-font-size: 24px;");
        usernameField.setMaxWidth(300);
        HBox.setHgrow(usernameField, Priority.ALWAYS); // ✅ Expands inside HBox
    
        emailField = new TextField(model.getEmail());
        emailField.setStyle("-fx-font-size: 16px;");
        emailField.setMaxWidth(300);
        HBox.setHgrow(emailField, Priority.ALWAYS); // ✅ Expands inside HBox
    
        // Profile Header with Image and Username
        HBox profileHeader = new HBox(20, profileImageView, usernameText);
        profileHeader.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(usernameText, Priority.ALWAYS); // ✅ Allows username to expand
    
        // Buttons for changing profile data
        Button updateProfileButton = new Button("Update Profile");
        updateProfileButton.setStyle("-fx-background-color: #4e8af3; -fx-text-fill: white;");
        updateProfileButton.setOnAction(e -> updateProfile());
    
        changeProfilePicButton = new Button("Change Picture");
        changeProfilePicButton.setStyle("-fx-background-color: #4e8af3; -fx-text-fill: white;");
        changeProfilePicButton.setOnAction(e -> openFileChooserForProfilePic());
    
        // User Info Section (fields and buttons)
        VBox userInfoSection = new VBox(10, profileHeader, usernameField, emailField, updateProfileButton, changeProfilePicButton);
        userInfoSection.setAlignment(Pos.CENTER_LEFT);
        VBox.setVgrow(userInfoSection, Priority.ALWAYS); // ✅ Allows the section to expand in the VBox
    
        // Add everything to the main VBox layout
        getChildren().add(userInfoSection);
    }

    // Method to update profile view with new information
    public void updateProfileView(ProfileModel updatedProfile) {
        // Only update the username and email if they've changed
        if (!usernameText.getText().equals(updatedProfile.getUsername())) {
            usernameText.setText(updatedProfile.getUsername());
        }
        if (!emailText.getText().equals(updatedProfile.getEmail())) {
            emailText.setText(updatedProfile.getEmail());
        }

        // Only reload the image if the profile picture has changed
        if (!profileImageView.getImage().getUrl().equals("file:" + updatedProfile.getProfilePicPath())) {
            Image updatedImage = new Image("file:" + updatedProfile.getProfilePicPath());
            profileImageView.setImage(updatedImage);
        }
    }

    // Method to handle updating profile data when the user clicks the "Update Profile" button
    private void updateProfile() {
        String newUsername = usernameField.getText();
        String newEmail = emailField.getText();
        
        // Validate input
        if (newUsername.isEmpty() || newEmail.isEmpty()) {
            showError("Username and Email cannot be empty");
            return;
        }

        // Optionally, use regex for email validation
        if (!newEmail.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showError("Invalid email format");
            return;
        }
        
        // Assume the model has setter methods for updating the profile info
        model.setUsername(newUsername);
        model.setEmail(newEmail);

        // Update the view with new data
        updateProfileView(model);
        showConfirmation();
        
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

    // Method to open file chooser for changing profile picture
    private void openFileChooserForProfilePic() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        
        Stage stage = new Stage();
        File file = fileChooser.showOpenDialog(stage);
        
        if (file != null) {
            // Preview the image before saving
            Image selectedImage = new Image("file:" + file.getAbsolutePath());
            profileImageView.setImage(selectedImage);
            model.setProfilePicPath(file.getAbsolutePath());
        }
    }
}