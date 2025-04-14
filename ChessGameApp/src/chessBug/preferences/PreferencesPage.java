package chessBug.preferences;

import java.net.URL;

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

        VBox page = new VBox(20);
        Region rightRegion = new Region();
        HBox.setHgrow(rightRegion, Priority.ALWAYS);
        root.getChildren().addAll(page, rightRegion);

        // Theme CSS
        applyThemeStylesheet(root);

        // Style outer container
        page.getStyleClass().add("settings-wrapper");
        page.setPadding(new Insets(30));
        page.setFillWidth(true);

        // Title
        Label title = new Label("Settings");
        title.getStyleClass().add("h1");

        // Save button
        Button savePreferencesButton = new Button("Save Preferences");
        savePreferencesButton.setOnAction(event -> PreferencesController.savePreferences());

        // Build sections
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
        moveHintsCheckBox.setOnAction(event -> PreferencesController.handleShowMoveHints(moveHintsCheckBox.isSelected()));
        moveHintsCheckBox.getStyleClass().add("label");

        CheckBox confirmMovesCheckBox = new CheckBox("Confirm Moves Before Playing");
        confirmMovesCheckBox.setSelected(PreferencesController.isConfirmMovesEnabled());
        confirmMovesCheckBox.setOnAction(event -> PreferencesController.handleConfirmMoves(confirmMovesCheckBox.isSelected()));
        confirmMovesCheckBox.getStyleClass().add("label");

        Slider volumeSlider = new Slider(0, 100, PreferencesController.getVolume() * 100);
        volumeSlider.setBlockIncrement(10);
        Label volumeLabel = new Label();
        volumeLabel.textProperty().bind(Bindings.format("Volume: %.2f", volumeSlider.valueProperty()));

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
        themeComboBox.setOnAction(event -> PreferencesController.handleThemeChange(themeComboBox.getValue(), themeComboBox.getScene()));

        themeContainer.getChildren().addAll(themeLabel, themeComboBox);

        CheckBox stayLoggedInCheckBox = new CheckBox("Stay logged in");
        stayLoggedInCheckBox.setSelected(PreferencesController.isStayLoggedIn());
        stayLoggedInCheckBox.setOnAction(event -> PreferencesController.handleStayLoggedIn(stayLoggedInCheckBox.isSelected()));
        stayLoggedInCheckBox.getStyleClass().add("label");

        container.getChildren().addAll(header, themeContainer, stayLoggedInCheckBox);
        return container;
    }

    private void applyThemeStylesheet(Region rootRegion) {
        String theme = PreferencesController.getTheme();
        String cssFile = theme.equalsIgnoreCase("Dark") ? "/css/PreferencesDark.css" : "/css/PreferencesLight.css";
        URL cssUrl = getClass().getResource(cssFile);

        if (cssUrl != null) {
            rootRegion.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("⚠️ Could not load CSS: " + cssFile);
        }
    }

    public HBox getPage() {
        return root;
    }
}
