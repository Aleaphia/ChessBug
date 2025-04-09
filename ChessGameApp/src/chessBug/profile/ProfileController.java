package chessBug.profile;

import chessBug.network.Client;
import chessBug.network.NetworkException;

public class ProfileController {

    private Client client;
    private ProfileView view;

    public ProfileController(Client client) {
        this.client = client;
        this.view = new ProfileView(this, client);
    }

    public ProfileView getPage() {
        return view;
    }

    public ProfileModel getModel() {
        return client.getProfile();
    }

    // Update user profile data (username, password, email, profile picture)
    public void updateProfile(String newUsername, String newEmail) {
        try {
            // Update the profile on the server
            client.updateProfile(newUsername, newEmail, getModel().getPassword());
            
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

    public void updateBio(String newBio) {
        try {
            client.updateBio(newBio);
            getModel().setBio(newBio);
        } catch (NetworkException e) {
            System.err.println("Unable to update bio.");
            e.printStackTrace();
        }
    }

    public void resetPassword(String oldPassword, String newPassword) {
        try {
            client.changePassword(oldPassword, newPassword);
            getModel().setPassword(newPassword);
        } catch (NetworkException e) {
            System.err.println("Password reset failed.");
            e.printStackTrace();
        }
    }
}
