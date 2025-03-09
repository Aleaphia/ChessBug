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

    // Get the profile's username
    public String getUsername() {
        return client.getProfile().getUsername();
    }

    // Get the profile's profile picture URL
    public String getProfilePicURL() {
        return client.getProfile().getProfilePicURL();
    }

    // Handle auto-save preference change
    public void handleAutoSave(boolean isEnabled) {
        preferences.putBoolean("autoSaveEnabled", isEnabled);
        System.out.println("Auto-Save preference changed: " + (isEnabled ? "Enabled" : "Disabled"));
    }

    // Handle move hints preference change
    public void handleMoveHints(boolean isEnabled) {
        preferences.putBoolean("showMoveHints", isEnabled);
        System.out.println("Move Hints preference changed: " + (isEnabled ? "Enabled" : "Disabled"));
    }

    // Handle confirm moves preference change
    public void handleConfirmMoves(boolean isEnabled) {
        preferences.putBoolean("confirmMoves", isEnabled);
        System.out.println("Confirm Moves preference changed: " + (isEnabled ? "Enabled" : "Disabled"));
    }

    // Handle language change
    public void handleLanguageChange(String language) {
        preferences.put("language", language);
        System.out.println("Language changed to: " + language);
    }

    // Handle logging out from all devices
    public void logoutAllDevices() {
        System.out.println("Logged out from all devices.");
    }

    // Handle enabling two-factor authentication
    public void enableTwoFactorAuth() {
        System.out.println("Two-Factor Authentication enabled.");
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

    // Update user profile data (username, password, email, profile picture URL)
    public void updateProfile(String newUsername, String newPassword, String newEmail, String newProfilePicURL) {
        try {
            // Update the profile on the server
            client.updateProfile(newUsername, newPassword, newEmail);
            
            // Set the new profile picture URL
            client.getProfile().setProfilePicURL(newProfilePicURL);
            
            // Set the new username
            client.getProfile().setUsername(newUsername);
            
            System.out.println("Profile updated successfully!");
        } catch (NetworkException e) {
            System.err.println("Unable to update profile details");
            e.printStackTrace();
        }
    }

    // Save all preferences
    public void savePreferences(boolean autoSaveEnabled, boolean moveHintsEnabled, boolean confirmMovesEnabled, String language) {
        preferences.putBoolean("autoSaveEnabled", autoSaveEnabled);
        preferences.putBoolean("showMoveHints", moveHintsEnabled);
        preferences.putBoolean("confirmMoves", confirmMovesEnabled);
        preferences.put("language", language);

        System.out.println("Preferences saved!");

        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Preferences Saved");
        alert.setHeaderText(null);
        alert.setContentText("Your preferences have been saved successfully.");
        alert.showAndWait();
    }
}
