package chessBug.preferences;

import java.util.prefs.Preferences;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

public class PreferencesController {

    // Instance fields for preferences controls
    private CheckBox soundCheckBox;
    private ComboBox<String> themeComboBox;
    private CheckBox autoSaveCheckBox;
    private ComboBox<String> timeControlComboBox;
    
    // Preferences object to store user settings persistently
    private Preferences preferences = Preferences.userNodeForPackage(PreferencesController.class);

    public VBox getPage() {
        // Main container for preferences page
        VBox preferencesPage = new VBox(15);
        preferencesPage.setStyle("-fx-background-color: #fff; -fx-padding: 20px;");
        
        // Title Label
        Label titleLabel = new Label("Preferences");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Sound Settings
        soundCheckBox = new CheckBox("Enable Sound");
        soundCheckBox.setSelected(preferences.getBoolean("soundEnabled", true)); // Default sound is enabled
        soundCheckBox.setOnAction(event -> handleSoundPreference(soundCheckBox.isSelected()));
        soundCheckBox.setTooltip(new Tooltip("Enable or disable game sound effects."));
        
        // Visual Settings
        Label themeLabel = new Label("Select Theme:");
        themeComboBox = new ComboBox<>();
        themeComboBox.getItems().addAll("Light", "Dark");
        themeComboBox.setValue(preferences.get("theme", "Light")); // Default theme
        themeComboBox.setOnAction(event -> handleThemeChange(themeComboBox.getValue()));
        themeComboBox.setTooltip(new Tooltip("Select the theme of the game (Light or Dark)."));

        // Game Settings
        VBox gameSettingsContainer = new VBox(10);
        gameSettingsContainer.setStyle("-fx-padding: 10px;");
        Label gameSettingsLabel = new Label("Game Settings:");
        autoSaveCheckBox = new CheckBox("Enable Auto-Save");
        autoSaveCheckBox.setSelected(preferences.getBoolean("autoSaveEnabled", true)); // Default auto-save is enabled
        autoSaveCheckBox.setOnAction(event -> handleAutoSave(autoSaveCheckBox.isSelected()));
        autoSaveCheckBox.setTooltip(new Tooltip("Enable or disable auto-saving of game progress."));
        
        gameSettingsContainer.getChildren().addAll(gameSettingsLabel, autoSaveCheckBox);

        // Time Controls
        Label timeControlLabel = new Label("Game Time Control:");
        timeControlComboBox = new ComboBox<>();
        timeControlComboBox.getItems().addAll("None", "5 minutes", "10 minutes", "20 minutes");
        timeControlComboBox.setValue(preferences.get("timeControl", "None")); // Default time control
        timeControlComboBox.setOnAction(event -> handleTimeControl(timeControlComboBox.getValue()));
        timeControlComboBox.setTooltip(new Tooltip("Select the time control for the game (None, 5 minutes, etc.)."));

        // Password Settings (for account management)
        Button changePasswordButton = new Button("Change Password");
        changePasswordButton.setOnAction(event -> changePassword());
        changePasswordButton.setTooltip(new Tooltip("Change your account password."));

        // Save Preferences Button
        Button savePreferencesButton = new Button("Save Preferences");
        savePreferencesButton.setOnAction(event -> savePreferences());

        // Adding all settings to the preferences page layout
        preferencesPage.getChildren().addAll(
            titleLabel,
            soundCheckBox,
            themeLabel, themeComboBox,
            gameSettingsContainer,
            timeControlLabel, timeControlComboBox,
            changePasswordButton,
            savePreferencesButton
        );
        
        // Load saved preferences when the page is first created
        loadPreferences();

        return preferencesPage;
    }

    // Handle the sound preference change
    private void handleSoundPreference(boolean isEnabled) {
        System.out.println("Sound preference changed: " + (isEnabled ? "Enabled" : "Disabled"));
        // Add actual logic for handling sound settings (e.g., enable or disable sound in the game)
    }

    // Handle the theme change
    private void handleThemeChange(String theme) {
        System.out.println("Theme changed to: " + theme);
        String themeFile = theme.equals("Dark") ? "dark-theme.css" : "light-theme.css";
        Scene scene = soundCheckBox.getScene();  // Use soundCheckBox's scene to access the main scene
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getClass().getResource(themeFile).toExternalForm());
    }

    // Handle auto-save preference change
    private void handleAutoSave(boolean isEnabled) {
        System.out.println("Auto-Save preference changed: " + (isEnabled ? "Enabled" : "Disabled"));
        // Add actual logic for handling auto-save settings in the game
    }

    // Handle the time control selection for the game
    private void handleTimeControl(String timeControl) {
        System.out.println("Time control set to: " + timeControl);
        // Add actual logic for handling game time control (e.g., configure game clock settings)
    }

    // Simulate changing password (for account settings)
    private void changePassword() {
        TextInputDialog passwordDialog = new TextInputDialog();
        passwordDialog.setTitle("Change Password");
        passwordDialog.setHeaderText("Enter new password:");
        passwordDialog.showAndWait().ifPresent(newPassword -> {
            // Implement password validation and updating logic here
            System.out.println("Password changed to: " + newPassword);
        });
    }

    // Save preferences to persistent storage
    private void savePreferences() {
        preferences.putBoolean("soundEnabled", soundCheckBox.isSelected());
        preferences.put("theme", themeComboBox.getValue());
        preferences.putBoolean("autoSaveEnabled", autoSaveCheckBox.isSelected());
        preferences.put("timeControl", timeControlComboBox.getValue());
        System.out.println("Preferences saved!");

        // Optionally, show a confirmation dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Preferences Saved");
        alert.setHeaderText(null);
        alert.setContentText("Your preferences have been saved successfully.");
        alert.showAndWait();
    }

    // Load saved preferences from persistent storage
    private void loadPreferences() {
        soundCheckBox.setSelected(preferences.getBoolean("soundEnabled", true));
        themeComboBox.setValue(preferences.get("theme", "Light"));
        autoSaveCheckBox.setSelected(preferences.getBoolean("autoSaveEnabled", true));
        timeControlComboBox.setValue(preferences.get("timeControl", "None"));
    }
}
