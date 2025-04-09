// Represents a user
package chessBug.network;

import javafx.scene.image.Image;

public class User {
    public static final String PFP_URL_PREFIX = "https://www.zandgall.com/chessbug/content/";
    public static final String DEFAULT_PROFILE_PICTURE = "chessbug";
    public static final User NO_USER = new User(0, "unknown", DEFAULT_PROFILE_PICTURE, null, false);

    private int id;
    private String username;
    private String profilePicURL;
    private Image profilePic = null;
    private String secretKey;  // 2FA secret key
    private boolean is2FAEnabled;  // Flag to check if 2FA is enabled

    // Updated constructor to accept secretKey and is2FAEnabled
    public User(int id, String username, String profilePicName, String secretKey, boolean is2FAEnabled) {
        this.id = id;
        this.username = username;
        this.profilePicURL = PFP_URL_PREFIX + profilePicName;
        this.secretKey = secretKey;  // Initialize the secret key
        this.is2FAEnabled = is2FAEnabled;  // Initialize the 2FA enabled flag
    }

    public String getUsername() {
        return username;
    }

    // Getter and setter for the secret key
    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    // Getter and setter for the 2FA enabled flag
    public boolean is2FAEnabled() {
        return is2FAEnabled;
    }

    public void set2FAEnabled(boolean is2FAEnabled) {
        this.is2FAEnabled = is2FAEnabled;
    }

    public Image getProfilePicture() {
        if(profilePic == null) // lazy.... *yawn*  ..loading..
            profilePic = new Image(profilePicURL);
        return profilePic;
    }

    public String getProfilePictureURL() {
        return profilePicURL;
    }

    public void setProfilePicture(String newProfilePicture) {
        if(!this.profilePicURL.equals(PFP_URL_PREFIX + newProfilePicture))
            profilePic = null; // Forget old image
        this.profilePicURL = PFP_URL_PREFIX + newProfilePicture;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getID() {
        return id;
    }

    @Override
    public boolean equals(Object o){
        if (o instanceof User user2){
            return user2.getID() == id;
        }
        return false;
    }
}
