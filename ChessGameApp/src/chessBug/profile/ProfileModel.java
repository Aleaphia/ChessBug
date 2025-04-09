package chessBug.profile;

public class ProfileModel {
    private int userID;
    private String username;
    private String password; // It's okay to keep this in memory
    private String email;
    private String profilePicURL; // Path to the profile picture (optional)

    // Constructor
    public ProfileModel(int userID, String username, String password, String email, String profilePicURL) {
        this.userID = userID;
        this.username = username;
        this.password = password;
        this.email = email;
        this.profilePicURL = profilePicURL;
    }

    // Getters and setters
    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

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

    public String getProfilePicURL() {
        return profilePicURL;
    }

    public void setProfilePicURL(String profilePicURL) {
        this.profilePicURL = profilePicURL;
    }

    private String bio;

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
