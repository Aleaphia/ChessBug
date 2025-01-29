package userProfile;

public class UserProfileController {

    private UserProfileModel model;
    private UserProfileView view;

    public UserProfileController(UserProfileModel model) {
        this.model = model;
        this.view = new UserProfileView(model);
    }

    public UserProfileView getView() {
        return view;
    }

    // Update user profile data (username, email, etc.)
    public void updateProfile(String newUsername, String newEmail, String newProfilePicPath) {
        model.setUsername(newUsername);
        model.setEmail(newEmail);
        model.setProfilePicPath(newProfilePicPath);
        
        // Update the view with the new data
        view = new UserProfileView(model);
    }
}
