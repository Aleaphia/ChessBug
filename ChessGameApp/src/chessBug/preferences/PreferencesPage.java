package chessBug.preferences;

import chessBug.network.Client;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.beans.binding.Bindings;

public class PreferencesPage {
    private final Client client;
    private VBox root;

    public PreferencesPage(Client client) {
        this.client = client;
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
            //ImageView profileImageView = new ImageView();
            //profileImageView.setImage(client.getOwnUser().getProfilePicture());

            //profileImageView.setFitWidth(90);
            //profileImageView.setFitHeight(90);
            //profileImageView.setClip(new Circle(45, 45, 45)); // Circular profile picture
            //profileImageView.setStyle("-fx-border-radius: 50%; -fx-border-color: white; -fx-border-width: 2px;");

//            // Username Label
//            String username = client.getOwnUser().getUsername();
//            Label usernameLabel = new Label(username);
//            usernameLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
//            usernameLabel.setTextFill(Color.web("#FFFFFF"));
//
//            profileContainer.getChildren().addAll(usernameLabel);

            // Game Settings Section
            VBox gameSettingsContainer = new VBox(20);
            gameSettingsContainer.setPadding(new Insets(10));
            Label gameSettingsLabel = new Label("Game Settings:");
            gameSettingsLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
            gameSettingsLabel.setTextFill(Color.web("#FFFFFF"));

            // Auto-Save Checkbox
            CheckBox autoSaveCheckBox = new CheckBox("Enable Auto-Save");  
            autoSaveCheckBox.setSelected(PreferencesController.isAutoSaveEnabled());
            autoSaveCheckBox.setOnAction(event -> PreferencesController.handleAutoSave(autoSaveCheckBox.isSelected()));
            autoSaveCheckBox.setTextFill(Color.web("#B0B0B0"));

            // Show Move Hints Checkbox
            CheckBox moveHintsCheckBox = new CheckBox("Show Move Hints");
            moveHintsCheckBox.setSelected(PreferencesController.isShowMoveHintsEnabled());
            moveHintsCheckBox.setOnAction(event -> PreferencesController.handleShowMoveHints(moveHintsCheckBox.isSelected()));
            moveHintsCheckBox.setTextFill(Color.web("#B0B0B0"));

            // Confirm Moves Checkbox
            CheckBox confirmMovesCheckBox = new CheckBox("Confirm Moves Before Playing");
            confirmMovesCheckBox.setSelected(PreferencesController.isConfirmMovesEnabled());
            confirmMovesCheckBox.setOnAction(event -> PreferencesController.handleConfirmMoves(confirmMovesCheckBox.isSelected()));
            confirmMovesCheckBox.setTextFill(Color.web("#B0B0B0"));

            // Slider for Volume
            Slider volumeSlider = new Slider(0, 100, PreferencesController.getVolume() * 100);
            volumeSlider.setBlockIncrement(10);
            volumeSlider.setStyle("-fx-base: #7289DA; -fx-accent: #4CAF50;");
            Label volumeLabel = new Label();
            volumeLabel.textProperty().bind(Bindings.format(
                    "Volume: %.2f", volumeSlider.valueProperty()));
            
//            volumeSlider.setOnDragDropped(event -> 
//                PreferencesController.handleVolume(volumeSlider.getValue() / 100.0)
//            );
            volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> 
                PreferencesController.handleVolume(volumeSlider.getValue() / 100.0)
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
            languageComboBox.setValue(PreferencesController.getLanguage());
            languageComboBox.setOnAction(event -> PreferencesController.handleLanguageChange(languageComboBox.getValue()));

            languageContainer.getChildren().addAll(languageLabel, languageComboBox);

            // Theme selection
            VBox themeContainer = new VBox(15);
            themeContainer.setPadding(new Insets(10));
            Label themeLabel = new Label("theme:");
            themeLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
            themeLabel.setTextFill(Color.web("#FFFFFF"));

            ComboBox<String> themeComboBox = new ComboBox<>();
            themeComboBox.getItems().addAll("Light", "Dark");
            themeComboBox.setValue(PreferencesController.getTheme());
            themeComboBox.setOnAction(event -> PreferencesController.handleThemeChange(themeComboBox.getValue(), themeComboBox.getScene()));

            themeContainer.getChildren().addAll(themeLabel, themeComboBox);
            
            // Loggin Settings Section
            VBox loginSettingsContainer = new VBox(20);
            loginSettingsContainer.setPadding(new Insets(10));
            // Stay logged in Checkbox
            CheckBox stayLoggedInCheckBox = new CheckBox("Stay logged in");  
            stayLoggedInCheckBox.setSelected(PreferencesController.isStayLoggedIn());
            stayLoggedInCheckBox.setOnAction(event -> PreferencesController.handleStayLoggedIn(stayLoggedInCheckBox.isSelected()));
            stayLoggedInCheckBox.setTextFill(Color.web("#B0B0B0"));
 
            loginSettingsContainer.getChildren().addAll(stayLoggedInCheckBox);
            
            // Save Preferences Button
            Button savePreferencesButton = new Button("Save Preferences");
            savePreferencesButton.setOnAction(event -> PreferencesController.savePreferences());

            // Add all components to the main layout
            root.getChildren().addAll(
                profileContainer,
                new Separator(),
                gameSettingsContainer,
                new Separator(),
                languageContainer,
                themeContainer,
                new Separator(),
                loginSettingsContainer,
                new Separator(),
                savePreferencesButton
            );
        }
        return root;
    }
}
