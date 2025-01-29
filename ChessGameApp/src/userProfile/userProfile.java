package userProfile;

public class UserProfileModel {
    private String username;
    private String email;
    private String profilePicPath; // Path to the profile picture (optional)

    // Constructor
    public UserProfileModel(String username, String email, String profilePicPath) {
        this.username = username;
        this.email = email;
        this.profilePicPath = profilePicPath;
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePicPath() {
        return profilePicPath;
    }

    public void setProfilePicPath(String profilePicPath) {
        this.profilePicPath = profilePicPath;
    }
}
