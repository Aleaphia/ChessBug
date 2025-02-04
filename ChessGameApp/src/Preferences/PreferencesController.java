import javafx.scene.layout.VBox;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

public class PreferencesController {
    public VBox getPage() {
        VBox preferencesPage = new VBox(10);
        preferencesPage.setStyle("-fx-background-color: #fff; -fx-padding: 20px;");
        
        Label titleLabel = new Label("Preferences");
        CheckBox soundCheckBox = new CheckBox("Enable Sound");
        soundCheckBox.setSelected(true);

        preferencesPage.getChildren().addAll(titleLabel, soundCheckBox);

        return preferencesPage;
    }
}