package chessBug.preferences;

import chessBug.network.Client;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.beans.binding.Bindings;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class PreferencesPage {
    private final Client client;
    private HBox root;

    public PreferencesPage(Client client) {
        this.client = client;
        buildPage();
    }
    
    private void buildPage(){
        //Set up page structure -> use region to push content to the left
        root = new HBox();
        
        VBox page = new VBox(20);
        Region rightRegion = new Region();
        
        root.getChildren().addAll(page,rightRegion);
        
        //Style and format
        page.getStyleClass().addAll("section", "padding");
        HBox.setHgrow(rightRegion, Priority.ALWAYS);
        
        //Fill page
        Label title = new Label("Settings");
        title.getStyleClass().add("h1");
        // Save Preferences Button
        Button savePreferencesButton = new Button("Save Preferences");
        savePreferencesButton.setOnAction(event -> PreferencesController.savePreferences());
        
        // Add all components to the main layout
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
    private VBox buildGameSettings(){
        VBox gameSettingsContainer = new VBox(20);
        
        // Game Settings Section
        Label gameSettingsLabel = new Label("Game Settings:");
        gameSettingsLabel.getStyleClass().addAll("label", "h2");

        // Show Move Hints Checkbox
        CheckBox moveHintsCheckBox = new CheckBox("Show Move Hints");
        moveHintsCheckBox.setSelected(PreferencesController.isShowMoveHintsEnabled());
        moveHintsCheckBox.setOnAction(event -> PreferencesController.handleShowMoveHints(moveHintsCheckBox.isSelected()));
        moveHintsCheckBox.getStyleClass().add("label");

        // Confirm Moves Checkbox
        CheckBox confirmMovesCheckBox = new CheckBox("Confirm Moves Before Playing");
        confirmMovesCheckBox.setSelected(PreferencesController.isConfirmMovesEnabled());
        confirmMovesCheckBox.setOnAction(event -> PreferencesController.handleConfirmMoves(confirmMovesCheckBox.isSelected()));
        confirmMovesCheckBox.getStyleClass().add("label");

        // Slider for Volume
        Slider volumeSlider = new Slider(0, 100, PreferencesController.getVolume() * 100);
        volumeSlider.setBlockIncrement(10);
        Label volumeLabel = new Label();
        volumeLabel.textProperty().bind(Bindings.format(
                "Volume: %.2f", volumeSlider.valueProperty()));

        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> 
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
    private VBox buildAppSettings(){
        VBox container = new VBox(20);
        // App Settings Section
        Label header = new Label("Application Settings:");
        header.getStyleClass().addAll("label", "h2");
        
        // Theme selection
        VBox themeContainer = new VBox(15);
        themeContainer.setPadding(new Insets(10));
        Label themeLabel = new Label("theme:");

        ComboBox<String> themeComboBox = new ComboBox<>();
        themeComboBox.getItems().addAll("Light", "Dark");
        themeComboBox.setValue(PreferencesController.getTheme());
        themeComboBox.setOnAction(event -> PreferencesController.handleThemeChange(themeComboBox.getValue(), themeComboBox.getScene()));

        themeContainer.getChildren().addAll(themeLabel, themeComboBox);
        
        // Stay logged in Checkbox
        CheckBox stayLoggedInCheckBox = new CheckBox("Stay logged in");  
        stayLoggedInCheckBox.setSelected(PreferencesController.isStayLoggedIn());
        stayLoggedInCheckBox.setOnAction(event -> PreferencesController.handleStayLoggedIn(stayLoggedInCheckBox.isSelected()));
        stayLoggedInCheckBox.getStyleClass().add("label");
        
        // Add all components to the main layout
        container.getChildren().addAll(
            header, themeContainer, stayLoggedInCheckBox
        );
        return container;
    }

    public HBox getPage() {
        return root;
    }
}
