// Represents a user, and holds a lazily-loaded profile picture
package chessBug.network;

import javafx.scene.image.Image;

public class User {
	public static final String PFP_URL_PREFIX = "https://www.zandgall.com/chessbug/content/";
	public static final String DEFAULT_PROFILE_PICTURE = "chessbug";
	public static final User NO_USER = new User(0, "unknown", DEFAULT_PROFILE_PICTURE);

	private int id;
	private String username;
	private String profilePicURL;
	private Image profilePic = null;
	
	public User(int id, String username, String profilePicName) {
		this.id = id;
		this.username = username;
		this.profilePicURL = PFP_URL_PREFIX + profilePicName;
	}

	public String getUsername() {
		return username;
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
