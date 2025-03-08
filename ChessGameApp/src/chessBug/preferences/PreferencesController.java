package chessBug.preferences;

import java.util.prefs.Preferences;

import chessBug.network.Client;
import chessBug.network.NetworkException;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;

public class PreferencesController {
    private final Preferences preferences;
    private final Client client;

    public PreferencesController(Client client) {
        this.client = client;
        this.preferences = Preferences.userNodeForPackage(PreferencesController.class);
    }

    // Get preferences with a default value
    public boolean getPreference(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    public String getPreference(String key, String defaultValue) {
        return preferences.get(key, defaultValue);
    }

    // Handle auto-save preference change
    public void handleAutoSave(boolean isEnabled) {
        System.out.println("Auto-Save preference changed: " + (isEnabled ? "Enabled" : "Disabled"));
    }

    // Handle password change
    public void changePassword() {
        TextInputDialog passwordDialog = new TextInputDialog();
        passwordDialog.setTitle("Change Password");
        passwordDialog.setHeaderText("Enter new password:");
        passwordDialog.showAndWait().ifPresent(newPassword -> {
            try {
                client.updatePassword(newPassword);
                System.out.println("Password changed successfully.");
            } catch (NetworkException e) {
                e.printStackTrace();
            }
        });
    }

    // Save all preferences (only auto-save for now)
    public void savePreferences(boolean autoSaveEnabled) {
        preferences.putBoolean("autoSaveEnabled", autoSaveEnabled);

        System.out.println("Preferences saved!");

        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Preferences Saved");
        alert.setHeaderText(null);
        alert.setContentText("Your preferences have been saved successfully.");
        alert.showAndWait();
    }
}
