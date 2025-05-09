package chessBug.profile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import chessBug.network.Client;
import chessBug.network.NetworkException;
import chessBug.preferences.PreferencesController;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class ProfileView extends VBox {
    private Text usernameText, emailText, profileDescriptionText;
    private ImageView profileImageView;
    private ProfileController controller;
    private TextField usernameField, emailField;
    private TextArea bioField;
    private PasswordField oldPasswordField, newPasswordField;

    public ProfileView(ProfileController controller, Client client) {
        this.controller = controller;

        setSpacing(20);
        setPadding(new Insets(50));
        setAlignment(Pos.TOP_CENTER);

        createProfileUI(client);

        // Responsive width
        prefWidthProperty().bind(Bindings.createDoubleBinding(() ->
                getScene() != null ? getScene().getWidth() * 0.9 : 1000, sceneProperty()));
    }

    private void createProfileUI(Client client) {
        Region banner = new Region();
        banner.setPrefSize(500, 100);
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

        usernameText = createText(controller.getModel().getUsername(), "h1", "biggerText");
        emailText = createText(controller.getModel().getEmail(), "h2");
        profileDescriptionText = createText(controller.getModel().getBio());
        profileDescriptionText.setWrappingWidth(400);

        usernameField = createField(controller.getModel().getUsername(), "Enter your new username");
        emailField = createField(controller.getModel().getEmail(), "Enter your email address");
        bioField = createArea(controller.getModel().getBio(), "Enter your bio");
        oldPasswordField = createPasswordField("Old Password");
        newPasswordField = createPasswordField("New Password");

        Button updateProfileButton = createButton("Update Profile", e -> {
            PreferencesController.playButtonSound();
            updateProfile();
                });
        Button changeProfilePicButton = createButton("Change Picture", e -> {
            PreferencesController.playButtonSound();
            openFileChooserForProfilePic(client);
                });
        Button resetPasswordButton = createButton("Reset Password", e -> {
            PreferencesController.playButtonSound();
            String oldPass = oldPasswordField.getText();
            String newPass = newPasswordField.getText();
                    
            if (oldPass.isEmpty() || newPass.isEmpty()) {
                showError("Both password fields must be filled");
                return;
            }
                
            controller.resetPassword(oldPass, newPass);
        });
                

        HBox passwordBox = new HBox(10, oldPasswordField, newPasswordField);
        passwordBox.setAlignment(Pos.CENTER);
        passwordBox.setMaxWidth(500);
        HBox.setHgrow(oldPasswordField, Priority.ALWAYS);
        HBox.setHgrow(newPasswordField, Priority.ALWAYS);
        
        //Focus changes
        usernameField.setOnAction(event -> emailField.requestFocus());
        emailField.setOnAction(event -> bioField.requestFocus());
        
        oldPasswordField.setOnAction(event -> newPasswordField.requestFocus());
        newPasswordField.setOnAction(event -> resetPasswordButton.fire());

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
        profileCard.getStyleClass().addAll("profile-card", "section");

        ScrollPane scrollPane = new ScrollPane(profileCard);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox scrollContainer = new VBox(scrollPane);
        scrollContainer.setAlignment(Pos.TOP_CENTER); // Center it horizontally
        scrollContainer.setPadding(new Insets(0));
        scrollContainer.setMaxWidth(700); // or match your card width (600-700 px)
        scrollContainer.setPrefWidth(Region.USE_COMPUTED_SIZE);

        getChildren().add(scrollContainer);
    }

    private Text createText(String content, String... styleClass) {
        Text text = new Text(content);
        text.getStyleClass().add("label");
        text.getStyleClass().addAll(Arrays.asList(styleClass));
        return text;
    }

    private TextField createField(String value, String prompt) {
        TextField field = new TextField(value);
        field.setPromptText(prompt);
        field.setMaxWidth(500);
        field.getStyleClass().add("field");
        return field;
    }
    
    private TextArea createArea(String value, String prompt) {
        TextArea field = new TextArea(value);
        field.setPromptText(prompt);
        field.setMaxWidth(500);
        field.setWrapText(true);
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

    public void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public void showConfirmation(String msg) {
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
