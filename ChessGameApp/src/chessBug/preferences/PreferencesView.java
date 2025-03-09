package chessBug.preferences;

import chessBug.network.Client;
import chessBug.network.NetworkException;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

public class PreferencesView {
    // Instance fields for preferences controls
    private CheckBox soundCheckBox;
    private ComboBox<String> themeComboBox;
    private CheckBox autoSaveCheckBox;
    private ComboBox<String> timeControlComboBox;

    // Handle communication with Server in order to change password
    private Client client;

    public PreferencesView(Client client) {
        this.client = client;
    }

    public VBox getPage() {
        // Main container for preferences page
        VBox preferencesPage = new VBox(15);
        preferencesPage.setStyle("-fx-background-color: #fff; -fx-padding: 20px;");
        
        // Title Label
        Label titleLabel = new Label("Preferences");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Sound Settings
        soundCheckBox = new CheckBox("Enable Sound");
        soundCheckBox.setSelected(PreferencesController.isSoundEnabled()); // Default sound is enabled
        soundCheckBox.setOnAction(event -> PreferencesController.handleSoundPreference(soundCheckBox.isSelected()));
        soundCheckBox.setTooltip(new Tooltip("Enable or disable game sound effects."));
        
        // Visual Settings
        Label themeLabel = new Label("Select Theme:");
        themeComboBox = new ComboBox<>();
        themeComboBox.getItems().addAll("Light", "Dark");
        themeComboBox.setValue(PreferencesController.getTheme()); // Default theme
        themeComboBox.setOnAction(event -> PreferencesController.handleThemeChange(themeComboBox.getValue(), themeComboBox.getScene()));
        themeComboBox.setTooltip(new Tooltip("Select the theme of the game (Light or Dark)."));

        // Game Settings
        VBox gameSettingsContainer = new VBox(10);
        gameSettingsContainer.setStyle("-fx-padding: 10px;");
        Label gameSettingsLabel = new Label("Game Settings:");
        autoSaveCheckBox = new CheckBox("Enable Auto-Save");
        autoSaveCheckBox.setSelected(PreferencesController.isAutoSaveEnabled()); // Default auto-save is enabled
        autoSaveCheckBox.setOnAction(event -> PreferencesController.handleAutoSave(autoSaveCheckBox.isSelected()));
        autoSaveCheckBox.setTooltip(new Tooltip("Enable or disable auto-saving of game progress."));
        
        gameSettingsContainer.getChildren().addAll(gameSettingsLabel, autoSaveCheckBox);

        // Time Controls
        Label timeControlLabel = new Label("Game Time Control:");
        timeControlComboBox = new ComboBox<>();
        timeControlComboBox.getItems().addAll("None", "5 minutes", "10 minutes", "20 minutes");
        timeControlComboBox.setValue(PreferencesController.getTimeControl()); // Default time control
        timeControlComboBox.setOnAction(event -> PreferencesController.handleTimeControl(timeControlComboBox.getValue()));
        timeControlComboBox.setTooltip(new Tooltip("Select the time control for the game (None, 5 minutes, etc.)."));

        // Password Settings (for account management)
        Button changePasswordButton = new Button("Change Password");
        changePasswordButton.setOnAction(event -> changePassword());
        changePasswordButton.setTooltip(new Tooltip("Change your account password."));

        // Save Preferences Button
        Button savePreferencesButton = new Button("Save Preferences");
        savePreferencesButton.setOnAction(event -> PreferencesController.savePreferences());

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
        return preferencesPage;
    }

    private void changePassword() {
        TextInputDialog passwordDialog = new TextInputDialog();
        passwordDialog.setTitle("Change Password");
        passwordDialog.setHeaderText("Enter new password:");
        passwordDialog.showAndWait().ifPresent(newPassword -> {
            // Implement password validation and updating logic here
            System.out.println("Password changed to: " + newPassword);
            try {
                client.updatePassword(newPassword);
            } catch(NetworkException e) {
                e.printStackTrace();
            }
        });
    }


}
