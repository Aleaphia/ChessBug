package chessBug.profile;

import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ProfileView extends VBox {

    private Text usernameText;
    private Text emailText;
    private ImageView profileImageView;

    public ProfileView(ProfileModel model) {
        super();
        // Initialize UI components
        usernameText = new Text("Username: " + model.getUsername());
        emailText = new Text("Email: " + model.getEmail());
        
        // Optionally load the profile image
        if (model.getProfilePicPath() != null && !model.getProfilePicPath().isEmpty()) {
            Image profileImage = new Image("file:" + model.getProfilePicPath());
            profileImageView = new ImageView(profileImage);
        } else
            profileImageView = new ImageView();

        // Add to layout
        getChildren().addAll(usernameText, emailText, profileImageView);
    }
}
