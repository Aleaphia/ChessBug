package chessBug.preferences;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class PreferencesPage {
    private final PreferencesController controller;
    private VBox root;

    public PreferencesPage(PreferencesController controller) {
        this.controller = controller;
    }

    public VBox getPage() {
        if (root == null) {
            root = new VBox(15);
            root.setPadding(new Insets(20));
            root.setBackground(new Background(new BackgroundFill(Color.web("#232428"), CornerRadii.EMPTY, Insets.EMPTY)));

            // Profile Section
            HBox profileContainer = new HBox(10);
            profileContainer.setAlignment(Pos.CENTER_LEFT);

            // Initialize the profile picture ImageView
            ImageView profileImageView = new ImageView();

            // Set the profile picture using the URL from the model (from the controller)
            String profilePicURL = controller.getProfilePicURL();
            if (profilePicURL != null && !profilePicURL.isEmpty()) {
                Image profileImage = new Image(profilePicURL);
                profileImageView.setImage(profileImage);
                System.out.println("Set profile picture to " + profilePicURL);
            }

            // Set the image size (100x100)
            profileImageView.setFitWidth(100);
            profileImageView.setFitHeight(100);
            profileImageView.setClip(new Circle(50, 50, 50));  // Make it circular

            // Set the username label from the controller
            String username = controller.getUsername();
            Label usernameLabel = new Label(username);
            usernameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            usernameLabel.setTextFill(Color.web("#ffffff"));

            profileContainer.getChildren().addAll(profileImageView, usernameLabel);

            // Game Settings
            VBox gameSettingsContainer = new VBox(10);
            Label gameSettingsLabel = new Label("Game Settings:");
            gameSettingsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            gameSettingsLabel.setTextFill(Color.web("#ffffff"));

            CheckBox autoSaveCheckBox = new CheckBox("Enable Auto-Save");
            autoSaveCheckBox.setSelected(controller.getPreference("autoSaveEnabled", true));
            autoSaveCheckBox.setOnAction(event -> controller.handleAutoSave(autoSaveCheckBox.isSelected()));
            autoSaveCheckBox.setTextFill(Color.web("#ffffff"));

            CheckBox moveHintsCheckBox = new CheckBox("Show Move Hints");
            moveHintsCheckBox.setSelected(controller.getPreference("showMoveHints", true));
            moveHintsCheckBox.setOnAction(event -> controller.handleMoveHints(moveHintsCheckBox.isSelected()));
            moveHintsCheckBox.setTextFill(Color.web("#ffffff"));

            CheckBox confirmMovesCheckBox = new CheckBox("Confirm Moves Before Playing");
            confirmMovesCheckBox.setSelected(controller.getPreference("confirmMoves", false));
            confirmMovesCheckBox.setOnAction(event -> controller.handleConfirmMoves(confirmMovesCheckBox.isSelected()));
            confirmMovesCheckBox.setTextFill(Color.web("#ffffff"));

            gameSettingsContainer.getChildren().addAll(gameSettingsLabel, autoSaveCheckBox, moveHintsCheckBox, confirmMovesCheckBox);

            // Language Selection
            VBox languageContainer = new VBox(10);
            Label languageLabel = new Label("Language:");
            languageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            languageLabel.setTextFill(Color.web("#ffffff"));

            ComboBox<String> languageComboBox = new ComboBox<>();
            languageComboBox.getItems().addAll("English", "Spanish", "French", "German");
            languageComboBox.setValue(controller.getPreference("language", "English"));
            languageComboBox.setOnAction(event -> controller.handleLanguageChange(languageComboBox.getValue()));

            languageContainer.getChildren().addAll(languageLabel, languageComboBox);

            // Save Preferences Button
            Button savePreferencesButton = new Button("Save Preferences");
            savePreferencesButton.setOnAction(event -> controller.savePreferences(
                autoSaveCheckBox.isSelected(),
                moveHintsCheckBox.isSelected(),
                confirmMovesCheckBox.isSelected(),
                languageComboBox.getValue()
            ));

            // Adding elements to the layout
            root.getChildren().addAll(
                profileContainer,
                new Separator(),
                gameSettingsContainer,
                new Separator(),
                languageContainer,
                new Separator(),
                savePreferencesButton
            );
        }
        return root;
    }
}
