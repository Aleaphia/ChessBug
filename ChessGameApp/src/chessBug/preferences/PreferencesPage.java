package chessBug.preferences;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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
            root.setBackground(new Background(new BackgroundFill(Color.web("#2b2d31"), new CornerRadii(10), Insets.EMPTY)));
            root.setEffect(new DropShadow(10, Color.BLACK));

            // Title Label
            Label titleLabel = new Label("Preferences");
            titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            titleLabel.setTextFill(Color.web("#ffffff"));

            // Game Settings Container
            VBox gameSettingsContainer = new VBox(10);
            gameSettingsContainer.setPadding(new Insets(10));
            gameSettingsContainer.setBackground(new Background(new BackgroundFill(Color.web("#232428"), new CornerRadii(8), Insets.EMPTY)));

            Label gameSettingsLabel = new Label("Game Settings:");
            gameSettingsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            gameSettingsLabel.setTextFill(Color.web("#ffffff"));

            CheckBox autoSaveCheckBox = new CheckBox("Enable Auto-Save");
            autoSaveCheckBox.setSelected(controller.getPreference("autoSaveEnabled", true));
            autoSaveCheckBox.setOnAction(event -> controller.handleAutoSave(autoSaveCheckBox.isSelected()));
            autoSaveCheckBox.setTooltip(new Tooltip("Enable or disable auto-saving of game progress."));
            autoSaveCheckBox.setTextFill(Color.web("#dddddd"));

            gameSettingsContainer.getChildren().addAll(gameSettingsLabel, autoSaveCheckBox);

            // Password Settings Button
            Button changePasswordButton = new Button("Change Password");
            styleButton(changePasswordButton);
            changePasswordButton.setOnAction(event -> controller.changePassword());

            // Save Preferences Button
            Button savePreferencesButton = new Button("Save Preferences");
            styleButton(savePreferencesButton);
            savePreferencesButton.setOnAction(event -> controller.savePreferences(autoSaveCheckBox.isSelected()));

            // Adding elements to the layout
            root.getChildren().addAll(
                titleLabel,
                gameSettingsContainer,
                changePasswordButton,
                savePreferencesButton
            );
        }
        return root;
    }

    // Custom button styling
    private void styleButton(Button button) {
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        button.setTextFill(Color.WHITE);
        button.setBackground(new Background(new BackgroundFill(Color.web("#5865F2"), new CornerRadii(5), Insets.EMPTY)));
        button.setPadding(new Insets(10, 15, 10, 15));

        // Hover effect
        button.setOnMouseEntered(e -> button.setBackground(new Background(new BackgroundFill(Color.web("#4752C4"), new CornerRadii(5), Insets.EMPTY))));
        button.setOnMouseExited(e -> button.setBackground(new Background(new BackgroundFill(Color.web("#5865F2"), new CornerRadii(5), Insets.EMPTY))));
    }
}
