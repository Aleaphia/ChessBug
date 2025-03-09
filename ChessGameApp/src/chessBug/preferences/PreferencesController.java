package chessBug.preferences;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;

import chessBug.network.Client;

public class PreferencesController {

        
    // Preferences object to store user settings persistently
    private static Preferences preferences = Preferences.userNodeForPackage(PreferencesController.class);

    private Pane page;
    PreferencesView view;

    public PreferencesController(Client client) {
        view = new PreferencesView(client);
        page = view.getPage();
    }

    public Pane getPage() { return page; }

    // Handle the sound preference change
    protected static void handleSoundPreference(boolean isEnabled) {
        preferences.putBoolean("soundEnabled", isEnabled);
        System.out.println("Sound preference changed: " + (isEnabled ? "Enabled" : "Disabled"));
        // TODO: Add actual logic for handling sound settings (e.g., enable or disable sound in the game)
    }

    // Handle the theme change
    protected static void handleThemeChange(String theme, Scene scene) {
        preferences.put("theme", theme);
        System.out.println("Theme changed to: " + theme);
        
        // Ensure the scene exists
        if (scene != null) {
            try {  
                // Clear existing styles and apply the new one
                scene.getStylesheets().clear();
                scene.getStylesheets().add(getStyle("Styles"));
                // Force layout update
                scene.getRoot().requestLayout();
    
                // Save the selected theme to preferences
                preferences.put("theme", theme);
            } catch (NullPointerException e) {
                System.out.println("Error: Could not load CSS file for theme " + theme);
            }
        } else {
            System.out.println("Scene is null, cannot apply theme.");
        }
    }
    

    // Handle auto-save preference change
    protected static void handleAutoSave(boolean isEnabled) {
        preferences.putBoolean("autoSaveEnabled", isEnabled); 
        System.out.println("Auto-Save preference changed: " + (isEnabled ? "Enabled" : "Disabled"));
        // TODO: Add actual logic for handling auto-save settings in the game
    }

    // Handle the time control selection for the game
    protected static void handleTimeControl(String timeControl) {
        preferences.put("timeControl", timeControl); 
        System.out.println("Time control set to: " + timeControl);
        // TODO: Add actual logic for handling game time control (e.g., configure game clock settings)
    }

    // Save preferences to persistent storage
    protected static void savePreferences() {
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        } 

        // Optionally, show a confirmation dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Preferences Saved");
        alert.setHeaderText(null);
        alert.setContentText("Your preferences have been saved successfully.");
        alert.showAndWait();
    }

    public static boolean isSoundEnabled() {
        return preferences.getBoolean("soundEnabled", true);
    }

    public static boolean isAutoSaveEnabled() {
        return preferences.getBoolean("autoSaveEnabled", true);
    }

    public static String getTheme() {
        return preferences.get("theme", "Light");
    }

    public static String getTimeControl() {
        return preferences.get("timeControl", "None");
    }

    public static String getStyle(String type) {
        System.out.println(PreferencesController.class.getResourceAsStream("/resources/styles/" + getTheme() + "/" + type + ".css") == null);
        System.out.println(PreferencesController.class.getResource("/resources/styles/" + getTheme() + "/" + type + ".css").toExternalForm());
        // return "/resources/styles/" + getTheme() + "/" + type + ".css";
        return PreferencesController.class.getResource("/resources/styles/" + getTheme() + "/" + type + ".css").toExternalForm();
    }
}
