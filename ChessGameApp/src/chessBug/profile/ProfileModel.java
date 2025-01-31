package chessBug.profile;

public class ProfileModel {
    private String username;
    private String password; // It's okay to keep this in memory
    private String email;
    private String profilePicPath; // Path to the profile picture (optional)

    // Constructor
    public ProfileModel(String username, String password, String email, String profilePicPath) {
        this.username = username;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
