package chessBug.preferences;

import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.util.prefs.Preferences;

public class PreferencesPage {
    private final PreferencesController controller;
    private VBox root;

    public PreferencesPage(PreferencesController controller) {
        this.controller = controller;
    }

    public VBox getPage() {
        if (root == null) {
            root = new VBox(15);
            root.getStyleClass().add("preferences-page"); // Reference CSS class

            // Title Label
            Label titleLabel = new Label("Preferences");
            titleLabel.getStyleClass().add("title-label");

            // Sound Settings
            CheckBox soundCheckBox = new CheckBox("Enable Sound");
            soundCheckBox.setSelected(controller.getPreference("soundEnabled", true));
            soundCheckBox.setOnAction(event -> controller.handleSoundPreference(soundCheckBox.isSelected()));
            soundCheckBox.setTooltip(new Tooltip("Enable or disable game sound effects."));

            // Visual Settings
            Label themeLabel = new Label("Select Theme:");
            ComboBox<String> themeComboBox = new ComboBox<>();
            themeComboBox.getItems().addAll("Light", "Dark");
            themeComboBox.setValue(controller.getPreference("theme", "Light"));
            themeComboBox.setOnAction(event -> controller.handleThemeChange(themeComboBox.getValue()));
            themeComboBox.setTooltip(new Tooltip("Select the theme of the game."));

            // Game Settings
            VBox gameSettingsContainer = new VBox(10);
            Label gameSettingsLabel = new Label("Game Settings:");
            CheckBox autoSaveCheckBox = new CheckBox("Enable Auto-Save");
            autoSaveCheckBox.setSelected(controller.getPreference("autoSaveEnabled", true));
            autoSaveCheckBox.setOnAction(event -> controller.handleAutoSave(autoSaveCheckBox.isSelected()));
            autoSaveCheckBox.setTooltip(new Tooltip("Enable or disable auto-saving of game progress."));
            gameSettingsContainer.getChildren().addAll(gameSettingsLabel, autoSaveCheckBox);

            // Time Controls
            Label timeControlLabel = new Label("Game Time Control:");
            ComboBox<String> timeControlComboBox = new ComboBox<>();
            timeControlComboBox.getItems().addAll("None", "5 minutes", "10 minutes", "20 minutes");
            timeControlComboBox.setValue(controller.getPreference("timeControl", "None"));
            timeControlComboBox.setOnAction(event -> controller.handleTimeControl(timeControlComboBox.getValue()));
            timeControlComboBox.setTooltip(new Tooltip("Select the time control for the game."));

            // Password Settings
            Button changePasswordButton = new Button("Change Password");
            changePasswordButton.setOnAction(event -> controller.changePassword());

            // Save Preferences Button
            Button savePreferencesButton = new Button("Save Preferences");
            savePreferencesButton.setOnAction(event -> controller.savePreferences(
                soundCheckBox.isSelected(),
                themeComboBox.getValue(),
                autoSaveCheckBox.isSelected(),
                timeControlComboBox.getValue()
            ));

            // Adding elements to the layout
            root.getChildren().addAll(
                titleLabel,
                soundCheckBox,
                themeLabel, themeComboBox,
                gameSettingsContainer,
                timeControlLabel, timeControlComboBox,
                changePasswordButton,
                savePreferencesButton
            );

            // Apply external CSS
            Scene scene = root.getScene();
            if (scene != null) {
                scene.getStylesheets().add(getClass().getResource("/styles/preferences.css").toExternalForm());
            }
        }
        return root;
    }
}
