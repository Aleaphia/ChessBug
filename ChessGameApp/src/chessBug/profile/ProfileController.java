package chessBug.profile;

import chessBug.network.Client;
import chessBug.network.NetworkException;

public class ProfileController {

    private Client client;
    private ProfileView view;

    public ProfileController(Client client) {
        this.client = client;
        this.view = new ProfileView(client);
    }

    public ProfileView getPage() {
        return view;
    }

    // Update user profile data (username, password, email, profile picture)
    public void updateProfile(String newUsername, String newPassword, String newEmail, String newProfilePicURL) {
        try {
            // Update the profile on the server
            client.updateProfile(newUsername, newPassword, newEmail);
            
            // Set the new profile picture
            client.getProfile().setProfilePicURL(newProfilePicURL);
            
            // Refresh the view with the updated data
            if (view != null) {
                view.updateProfileView(client.getProfile());
            }
        } catch (NetworkException e) {
            System.err.println("Unable to update profile details");
            e.printStackTrace();
        }
    }

    // Allow updating only the profile picture
    public void updateProfilePicture(String newProfilePicURL) {
        client.getProfile().setProfilePicURL(newProfilePicURL);
        if (view != null) {
            view.updateProfileView(client.getProfile());
        }
    }
}
