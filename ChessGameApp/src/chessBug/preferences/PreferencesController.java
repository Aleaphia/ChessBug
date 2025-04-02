package chessBug.preferences;

import java.net.URL;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.scene.control.ButtonType;
/*
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.Media;
*/

import chessBug.network.Client;

public class PreferencesController {
       
    // Preferences object to store user settings persistently
    private static Preferences preferences = Preferences.userNodeForPackage(PreferencesController.class);

    private Pane page;
    PreferencesPage view;

    public PreferencesController(Client client) {
        view = new PreferencesPage(client);
        page = view.getPage();
    }

    public Pane getPage() { return page; }

    //Methods for Handling changes
    // Handle the theme change
    protected static void handleThemeChange(String theme, Scene scene) {
        preferences.put("theme", theme);
        System.out.println("Theme changed to: " + theme);
        
        // Ensure the scene exists
        if (scene != null) {
            try {  
                // Clear existing styles and apply the new ones
                applyStyles(scene, "Styles", "Menu", "Preferences");
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
    }

    protected static void handleShowMoveHints(boolean isEnabled) {
        preferences.putBoolean("showMoveHints", isEnabled); 
        System.out.println("Move Hints preference changed: " + (isEnabled ? "Enabled" : "Disabled")); 
    }

    // Handle confirm moves preference change
    protected static void handleConfirmMoves(boolean isEnabled) {
        preferences.putBoolean("confirmMoves", isEnabled);
        System.out.println("Confirm Moves preference changed: " + (isEnabled ? "Enabled" : "Disabled"));
    }
    
    protected static void handleStayLoggedIn(boolean isEnabled) {
        preferences.putBoolean("stayLoggedIn", isEnabled); 
        System.out.println("Login preference changed: " + (isEnabled ? "Enabled" : "Disabled"));
    }

    // Handle language change
    protected static void handleLanguageChange(String language) {
        preferences.put("language", language);
        System.out.println("Language changed to: " + language);
    }

    // Save preferences to persistent storage
    protected static void savePreferences() {
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }

        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Preferences Saved");
        alert.setHeaderText(null);
        alert.setContentText("Your preferences have been saved successfully.");
        alert.showAndWait();
    }
    
    //Save login credentials
    public static void setLogginCredentials(String username, String password){
        preferences.put("username", username);
        preferences.put("password", password);
    }
    
    //Getter methods
    public static boolean isAutoSaveEnabled() {
        return preferences.getBoolean("autoSaveEnabled", true);
    }

    public static String getTheme() {
        return preferences.get("theme", "Light");
    }

    public static String getLanguage() {
        return preferences.get("language", "English");
    }

    public static boolean isShowMoveHintsEnabled() {
        return preferences.getBoolean("showMoveHints", true);
    }

    public static boolean isConfirmMovesEnabled() {
        return preferences.getBoolean("confirmMoves", false);
    }
    
    public static boolean isStayLoggedIn() {
        return preferences.getBoolean("stayLoggedIn", false);
    }
    
    public static String getUsername(){
        return preferences.get("username", "");
    }
    
    public static String getPassword(){
        return preferences.get("password", "");
    }

    public static void applyStyles(Scene scene, String... styles) {
        scene.getStylesheets().clear();
        for(String style : styles) {
            scene.getStylesheets().add(PreferencesController.class.getResource("/resources/styles/" + style + ".css").toExternalForm());
            URL theme = PreferencesController.class.getResource("/resources/styles/" + getTheme() + "/" + style + ".css");
            if(theme != null)
                scene.getStylesheets().add(theme.toExternalForm());
            
            //For game page check if movehints are on
            if (style.equals("Game") && isShowMoveHintsEnabled())
                scene.getStylesheets().add(PreferencesController.class.getResource("/resources/styles/MoveHints.css").toExternalForm());
        }
    }
    
    public static void playSound(){
        System.out.println("DEBUG: play sound");
        //URL url = PreferencesController.class.getResource("/resources/sounds/test.wav");
        //MediaPlayer player = new MediaPlayer( new Media(url.toString()));
        //player.play
    }
    
    public static boolean confirmMove(){
        //Skip confirmation dialog if confirm moves is off
        if (!isConfirmMovesEnabled())
            return true;
        
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Preferences Saved");
        alert.setHeaderText(null);
        alert.setContentText("Confirm your move");
        //Returns true if the okay button is pressed
        return alert.showAndWait().isPresent() && alert.getResult().equals(ButtonType.OK);
    }
}
