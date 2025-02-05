import javafx.scene.layout.VBox;
import javafx.scene.control.*;
import javafx.geometry.Insets;

public class PreferencesController {
    
    public VBox getPage() {
        // Main container for preferences page
        VBox preferencesPage = new VBox(15);
        preferencesPage.setStyle("-fx-background-color: #fff; -fx-padding: 20px;");
        
        // Title Label
        Label titleLabel = new Label("Preferences");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Sound Settings
        CheckBox soundCheckBox = new CheckBox("Enable Sound");
        soundCheckBox.setSelected(true); // Default sound is enabled
        soundCheckBox.setOnAction(event -> handleSoundPreference(soundCheckBox.isSelected()));

        // Visual Settings
        Label themeLabel = new Label("Select Theme:");
        ComboBox<String> themeComboBox = new ComboBox<>();
        themeComboBox.getItems().addAll("Light", "Dark");
        themeComboBox.setValue("Light"); // Default theme
        themeComboBox.setOnAction(event -> handleThemeChange(themeComboBox.getValue()));

        // Game Settings
        Label gameSettingsLabel = new Label("Game Settings:");
        CheckBox autoSaveCheckBox = new CheckBox("Enable Auto-Save");
        autoSaveCheckBox.setSelected(true); // Default auto-save is enabled
        autoSaveCheckBox.setOnAction(event -> handleAutoSave(autoSaveCheckBox.isSelected()));

        // Time Controls
        Label timeControlLabel = new Label("Game Time Control:");
        ComboBox<String> timeControlComboBox = new ComboBox<>();
        timeControlComboBox.getItems().addAll("None", "5 minutes", "10 minutes", "20 minutes");
        timeControlComboBox.setValue("None"); // Default time control
        timeControlComboBox.setOnAction(event -> handleTimeControl(timeControlComboBox.getValue()));

        // Password Settings (for account management)
        Button changePasswordButton = new Button("Change Password");
        changePasswordButton.setOnAction(event -> changePassword());

        // Save Preferences Button
        Button savePreferencesButton = new Button("Save Preferences");
        savePreferencesButton.setOnAction(event -> savePreferences());

        // Adding all settings to the preferences page layout
        preferencesPage.getChildren().addAll(
            titleLabel,
            soundCheckBox,
            themeLabel, themeComboBox,
            gameSettingsLabel, autoSaveCheckBox,
            timeControlLabel, timeControlComboBox,
            changePasswordButton,
            savePreferencesButton
        );
        
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
        // Add actual logic for changing the theme (e.g., switch between light/dark theme stylesheets)
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
        System.out.println("Changing password...");
        // Add actual logic for changing the user's password (e.g., open a password change dialog)
    }

    // Save preferences (you can extend this to save to a file or database)
    private void savePreferences() {
        System.out.println("Preferences saved!");
        // Add logic to save the user preferences (e.g., write to a file or database)
    }
}
