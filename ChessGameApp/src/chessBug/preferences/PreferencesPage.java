package chessBug.preferences;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
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
            root = new VBox(20);
            root.setPadding(new Insets(30));
            root.setBackground(new Background(new BackgroundFill(Color.web("#2C2F34"), CornerRadii.EMPTY, Insets.EMPTY)));

            // Profile Section
            HBox profileContainer = new HBox(15);
            profileContainer.setAlignment(Pos.CENTER_LEFT);
            profileContainer.setPadding(new Insets(10));

            // Profile Picture
            ImageView profileImageView = new ImageView();
            String profilePicURL = controller.getProfilePicURL();
            if (profilePicURL != null && !profilePicURL.isEmpty()) {
                Image profileImage = new Image(profilePicURL);
                profileImageView.setImage(profileImage);
            }

            profileImageView.setFitWidth(90);
            profileImageView.setFitHeight(90);
            profileImageView.setClip(new Circle(45, 45, 45)); // Circular profile picture
            profileImageView.setStyle("-fx-border-radius: 50%; -fx-border-color: white; -fx-border-width: 2px;");

            // Username Label
            String username = controller.getUsername();
            Label usernameLabel = new Label(username);
            usernameLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
            usernameLabel.setTextFill(Color.web("#FFFFFF"));

            profileContainer.getChildren().addAll(profileImageView, usernameLabel);

            // Game Settings Section
            VBox gameSettingsContainer = new VBox(20);
            gameSettingsContainer.setPadding(new Insets(10));
            Label gameSettingsLabel = new Label("Game Settings:");
            gameSettingsLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
            gameSettingsLabel.setTextFill(Color.web("#FFFFFF"));

            // Auto-Save Checkbox
            CheckBox autoSaveCheckBox = new CheckBox("Enable Auto-Save");
            autoSaveCheckBox.setSelected(controller.getPreference("autoSaveEnabled", true));
            autoSaveCheckBox.setOnAction(event -> controller.handleAutoSave(autoSaveCheckBox.isSelected()));
            autoSaveCheckBox.setTextFill(Color.web("#B0B0B0"));

            // Show Move Hints Checkbox
            CheckBox moveHintsCheckBox = new CheckBox("Show Move Hints");
            moveHintsCheckBox.setSelected(controller.getPreference("showMoveHints", true));
            moveHintsCheckBox.setOnAction(event -> controller.handleMoveHints(moveHintsCheckBox.isSelected()));
            moveHintsCheckBox.setTextFill(Color.web("#B0B0B0"));

            // Confirm Moves Checkbox
            CheckBox confirmMovesCheckBox = new CheckBox("Confirm Moves Before Playing");
            confirmMovesCheckBox.setSelected(controller.getPreference("confirmMoves", false));
            confirmMovesCheckBox.setOnAction(event -> controller.handleConfirmMoves(confirmMovesCheckBox.isSelected()));
            confirmMovesCheckBox.setTextFill(Color.web("#B0B0B0"));

            // Slider for Volume
            Slider volumeSlider = new Slider(0, 100, 50);
            volumeSlider.setBlockIncrement(10);
            volumeSlider.setStyle("-fx-base: #7289DA; -fx-accent: #4CAF50;");
            Label volumeLabel = new Label("Volume: " + (int) volumeSlider.getValue());
            volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> 
                volumeLabel.setText("Volume: " + newValue.intValue())
            );

            gameSettingsContainer.getChildren().addAll(
                gameSettingsLabel, 
                autoSaveCheckBox, 
                moveHintsCheckBox, 
                confirmMovesCheckBox, 
                volumeLabel, 
                volumeSlider
            );

            // Language Section
            VBox languageContainer = new VBox(15);
            languageContainer.setPadding(new Insets(10));
            Label languageLabel = new Label("Language:");
            languageLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
            languageLabel.setTextFill(Color.web("#FFFFFF"));

            ComboBox<String> languageComboBox = new ComboBox<>();
            languageComboBox.getItems().addAll("English", "Spanish", "French", "German");
            languageComboBox.setValue(controller.getPreference("language", "English"));
            languageComboBox.setOnAction(event -> controller.handleLanguageChange(languageComboBox.getValue()));

            languageContainer.getChildren().addAll(languageLabel, languageComboBox);

            // Save Preferences Button
            Button savePreferencesButton = new Button("Save Preferences");
            savePreferencesButton.setStyle("-fx-background-color: #7289DA; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-background-radius: 5px;");
            savePreferencesButton.setOnAction(event -> controller.savePreferences(
                autoSaveCheckBox.isSelected(),
                moveHintsCheckBox.isSelected(),
                confirmMovesCheckBox.isSelected(),
                languageComboBox.getValue()
            ));

            // Hover Effect for Save Button
            savePreferencesButton.setOnMouseEntered(e -> savePreferencesButton.setStyle("-fx-background-color: #5C6A8B; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-background-radius: 5px;"));
            savePreferencesButton.setOnMouseExited(e -> savePreferencesButton.setStyle("-fx-background-color: #7289DA; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-background-radius: 5px;"));

            // Add all components to the main layout
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
