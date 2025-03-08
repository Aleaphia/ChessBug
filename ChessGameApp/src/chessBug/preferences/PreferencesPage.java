package chessBug.preferences;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;

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

            // Game Settings
            VBox gameSettingsContainer = new VBox(10);
            Label gameSettingsLabel = new Label("Game Settings:");
            CheckBox autoSaveCheckBox = new CheckBox("Enable Auto-Save");
            autoSaveCheckBox.setSelected(controller.getPreference("autoSaveEnabled", true));
            autoSaveCheckBox.setOnAction(event -> controller.handleAutoSave(autoSaveCheckBox.isSelected()));
            autoSaveCheckBox.setTooltip(new Tooltip("Enable or disable auto-saving of game progress."));
            gameSettingsContainer.getChildren().addAll(gameSettingsLabel, autoSaveCheckBox);

            // Password Settings
            Button changePasswordButton = new Button("Change Password");
            changePasswordButton.setOnAction(event -> controller.changePassword());

            // Save Preferences Button
            Button savePreferencesButton = new Button("Save Preferences");
            savePreferencesButton.setOnAction(event -> controller.savePreferences(
                autoSaveCheckBox.isSelected()
            ));

            // Adding elements to the layout
            root.getChildren().addAll(
                titleLabel,
                gameSettingsContainer,
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
