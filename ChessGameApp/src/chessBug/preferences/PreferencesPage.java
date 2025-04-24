package chessBug.preferences;

import chessBug.network.Client;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class PreferencesPage {
    private final Client client;
    private HBox root;

    public PreferencesPage(Client client) {
        this.client = client;
        buildPage();
    }

    private void buildPage() {
        root = new HBox();
//        root.getStyleClass().add("preferences-page");

        VBox settingsCard = new VBox();
//        settingsCard.getStyleClass().add("settings-card");

        VBox page = new VBox(20);
        page.setPadding(new Insets(30));
        page.getStyleClass().addAll(/*"settings-wrapper",*/ "section");
        page.setFillWidth(true);

        Region rightRegion = new Region();
        HBox.setHgrow(rightRegion, Priority.ALWAYS);

        root.getChildren().addAll(settingsCard, rightRegion);
        settingsCard.getChildren().add(page);

        // Title
        Label title = new Label("Settings");
        title.getStyleClass().add("h1");

        // Save Preferences Button
        Button savePreferencesButton = createAnimatedButton("Save Preferences", () -> {
            PreferencesController.playButtonSound();
            PreferencesController.savePreferences();
        });

        // Page Sections
        page.getChildren().addAll(
                title,
                new Separator(),
                buildGameSettings(),
                new Separator(),
                buildAppSettings(),
                new Separator(),
                savePreferencesButton
        );
    }

    private VBox buildGameSettings() {
        VBox gameSettingsContainer = new VBox(20);

        Label gameSettingsLabel = new Label("Game Settings:");
        gameSettingsLabel.getStyleClass().addAll("label", "h2");

        CheckBox moveHintsCheckBox = new CheckBox("Show Move Hints");
        moveHintsCheckBox.setSelected(PreferencesController.isShowMoveHintsEnabled());
        moveHintsCheckBox.setOnAction(event -> {
            PreferencesController.playButtonSound();
            PreferencesController.handleShowMoveHints(moveHintsCheckBox.isSelected());
                });
        moveHintsCheckBox.getStyleClass().add("label");

        CheckBox confirmMovesCheckBox = new CheckBox("Confirm Moves Before Playing");
        confirmMovesCheckBox.setWrapText(true);
        confirmMovesCheckBox.setSelected(PreferencesController.isConfirmMovesEnabled());
        confirmMovesCheckBox.setOnAction(event -> {
            PreferencesController.playButtonSound();
            PreferencesController.handleConfirmMoves(confirmMovesCheckBox.isSelected());
                });
        confirmMovesCheckBox.getStyleClass().add("label");

        Slider volumeSlider = new Slider(0, 100, PreferencesController.getVolume() * 100);
        volumeSlider.setBlockIncrement(10);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setShowTickLabels(true);

        Label volumeLabel = new Label();
        volumeLabel.textProperty().bind(Bindings.format("Volume: %.0f", volumeSlider.valueProperty()));

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                PreferencesController.handleVolume(volumeSlider.getValue() / 100.0)
        );

        gameSettingsContainer.getChildren().addAll(
                gameSettingsLabel,
                moveHintsCheckBox,
                confirmMovesCheckBox,
                volumeLabel,
                volumeSlider
        );

        return gameSettingsContainer;
    }

    private VBox buildAppSettings() {
        VBox container = new VBox(20);
        Label header = new Label("Application Settings:");
        header.getStyleClass().addAll("label", "h2");

        VBox themeContainer = new VBox(15);
        themeContainer.setPadding(new Insets(10));
        Label themeLabel = new Label("Theme:");

        ComboBox<String> themeComboBox = new ComboBox<>();
        themeComboBox.getItems().addAll("Light", "Dark");
        themeComboBox.setValue(PreferencesController.getTheme());
        themeComboBox.setOnAction(event ->{
            PreferencesController.playButtonSound();
            PreferencesController.handleThemeChange(themeComboBox.getValue(), themeComboBox.getScene());
        });

        themeContainer.getChildren().addAll(themeLabel, themeComboBox);

        CheckBox stayLoggedInCheckBox = new CheckBox("Stay logged in");
        stayLoggedInCheckBox.setSelected(PreferencesController.isStayLoggedIn());
        stayLoggedInCheckBox.setOnAction(event -> {
            PreferencesController.playButtonSound();
            PreferencesController.handleStayLoggedIn(stayLoggedInCheckBox.isSelected());
        });
        stayLoggedInCheckBox.getStyleClass().add("label");

        container.getChildren().addAll(header, themeContainer, stayLoggedInCheckBox);
        return container;
    }

    private Button createAnimatedButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("red-button");
        button.setOnAction(e -> action.run());

        // Hover scale animation
        button.setOnMouseEntered(e -> button.setScaleX(1.05));
        button.setOnMouseExited(e -> button.setScaleX(1.0));

        return button;
    }

    public HBox getPage() {
        return root;
    }
}
