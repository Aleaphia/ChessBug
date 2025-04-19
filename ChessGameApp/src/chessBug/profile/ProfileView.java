package chessBug.profile;

import java.io.File;
import java.io.IOException;

import chessBug.network.Client;
import chessBug.network.NetworkException;
import javafx.animation.FadeTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;


public class ProfileView extends VBox {
    private Text usernameText, emailText, profileDescriptionText;
    private ImageView profileImageView;
    private ProfileController controller;
    private TextField usernameField, emailField, bioField;
    private PasswordField oldPasswordField, newPasswordField;

    public ProfileView(ProfileController controller, Client client) {
        this.controller = controller;

        setSpacing(20);
        setPadding(new Insets(50));
        setAlignment(Pos.TOP_CENTER);
        setStyle("-fx-background-color: #1a1a1a;");

        createProfileUI(client);

        // Responsive width
        prefWidthProperty().bind(Bindings.createDoubleBinding(() ->
                getScene() != null ? getScene().getWidth() * 0.9 : 1000, sceneProperty()));

        // Entry animation
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.0), this);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void createProfileUI(Client client) {
        Rectangle banner = new Rectangle(500, 120);
        banner.setArcWidth(20);
        banner.setArcHeight(20);
        banner.getStyleClass().add("profile-banner");

        profileImageView = new ImageView(client.getOwnUser().getProfilePicture());
        profileImageView.setFitWidth(120);
        profileImageView.setFitHeight(120);
        profileImageView.setClip(new Circle(60, 60, 60));
        DropShadow glow = new DropShadow(15, Color.RED);
        glow.setSpread(0.3);
        profileImageView.setEffect(glow);

        profileImageView.setOnMouseEntered(e -> glow.setColor(Color.ORANGERED));
        profileImageView.setOnMouseExited(e -> glow.setColor(Color.RED));

        StackPane profileStack = new StackPane(banner, profileImageView);
        profileStack.setAlignment(Pos.BOTTOM_CENTER);
        profileImageView.setTranslateY(30);

        usernameText = createText(controller.getModel().getUsername(), 26, "text-primary", true);
        emailText = createText(controller.getModel().getEmail(), 16, "text-secondary", false);
        profileDescriptionText = createText(controller.getModel().getBio(), 14, "text-secondary", false);
        profileDescriptionText.setWrappingWidth(400);

        usernameField = createField(controller.getModel().getUsername(), "Enter your new username");
        emailField = createField(controller.getModel().getEmail(), "Enter your email address");
        bioField = createField(controller.getModel().getBio(), "Enter your bio");
        oldPasswordField = createPasswordField("Old Password");
        newPasswordField = createPasswordField("New Password");

        Button updateProfileButton = createButton("Update Profile", e -> updateProfile());
        Button changeProfilePicButton = createButton("Change Picture", e -> openFileChooserForProfilePic(client));
        Button resetPasswordButton = createButton("Reset Password", e -> {
            String oldPass = oldPasswordField.getText();
            String newPass = newPasswordField.getText();
            if (oldPass.isEmpty() || newPass.isEmpty()) {
                showError("Both password fields must be filled");
            } else {
                controller.resetPassword(oldPass, newPass);
                showConfirmation("Password reset successfully.");
            }
        });

        HBox passwordBox = new HBox(10, oldPasswordField, newPasswordField);
        passwordBox.setAlignment(Pos.CENTER);
        passwordBox.setMaxWidth(500);
        HBox.setHgrow(oldPasswordField, Priority.ALWAYS);
        HBox.setHgrow(newPasswordField, Priority.ALWAYS);

        VBox profileCard = new VBox(15,
                profileStack,
                usernameText,
                emailText,
                profileDescriptionText,
                usernameField,
                emailField,
                bioField,
                updateProfileButton,
                changeProfilePicButton,
                passwordBox,
                resetPasswordButton
        );
        profileCard.setAlignment(Pos.CENTER);
        profileCard.setPadding(new Insets(30));
        profileCard.setMaxWidth(600);
        profileCard.getStyleClass().add("profile-card");

        getChildren().add(profileCard);
    }

    private Text createText(String content, int size, String styleClass, boolean bold) {
        Text text = new Text(content);
        text.setFont(Font.font("Arial", bold ? javafx.scene.text.FontWeight.BOLD : javafx.scene.text.FontWeight.NORMAL, size));
        text.getStyleClass().add(styleClass);
        return text;
    }

    private TextField createField(String value, String prompt) {
        TextField field = new TextField(value);
        field.setPromptText(prompt);
        field.setMaxWidth(500);
        field.getStyleClass().add("field");
        return field;
    }
    
    private PasswordField createPasswordField(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.setMaxWidth(500);
        field.getStyleClass().add("field");
        return field;
    }

    private Button createButton(String label, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(label);
        btn.setOnAction(handler);
        btn.getStyleClass().add("button");
        btn.setMaxWidth(500);
        return btn;
    }

    private void updateProfile() {
        String newUsername = usernameField.getText();
        String newEmail = emailField.getText();
        String newBio = bioField.getText();
        if (newUsername.isEmpty() || newEmail.isEmpty()) {
            showError("Username and Email cannot be empty");
            return;
        }
        if (!newEmail.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showError("Invalid email format");
            return;
        }
        controller.updateProfile(newUsername, newEmail, newBio);
        showConfirmation("Profile updated successfully.");
    }

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
        String newBio = updatedProfile.getBio();
        if (newBio != null && !newBio.equals(profileDescriptionText.getText())) {
            profileDescriptionText.setText(newBio.isBlank()
                    ? "Add a brief description about yourself..."
                    : newBio);
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showConfirmation(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void openFileChooserForProfilePic(Client client) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        Stage stage = new Stage();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            Image selectedImage = new Image("file:" + file.getAbsolutePath());
            try {
                client.uploadProfilePicture(file);
                profileImageView.setImage(selectedImage);
            } catch (NetworkException | IOException e) {
                showError("Unable to upload profile picture!");
                e.printStackTrace();
            }
        }
    }
}
