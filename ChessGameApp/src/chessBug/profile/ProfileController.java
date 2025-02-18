package chessBug.profile;

import chessBug.network.Client;
import chessBug.network.NetworkException;

public class ProfileController {

    // Client contains an instance of "UserProfileModel"
    private Client client;
    private ProfileView view;

    public ProfileController(Client client) {
        this.client = client;
        this.view = new ProfileView(client.getProfile());
    }

    public ProfileView getPage() {
        return view;
    }

   // Update user profile data (username, password, email, etc.)
    public void updateProfile(String newUsername, String newPassword, String newEmail, String newProfilePicPath) {
        try {
            // Update the profile on the server
            client.updateProfile(newUsername, newPassword, newEmail);
            
            // Set the new profile picture path
            client.getProfile().setProfilePicPath(newProfilePicPath);
            
            // After updating, refresh the view with the updated data (need to fix)
           // view.updateProfileView(client.getProfile());  // This updates the profile UI
        } catch (NetworkException e) {
            System.err.println("Unable to update profile details");
            e.printStackTrace();
        }
        
        // Update the view with the new data
        view = new ProfileView(client.getProfile());
    }
}
