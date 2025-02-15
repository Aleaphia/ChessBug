package chessBug.home;

import chessBug.network.*;

import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.json.JSONObject;




public class FriendRequestUI {
    private VBox page = new VBox();
    Client client;
    
    FriendRequestUI(Client client){
        this.client = client;
        
        //Create UI Layout
        TextField input = new TextField();
        Button btnSearch = new Button("Send");
        page.getChildren().addAll(new Label("Search by Username:"), input, btnSearch);
        
        //Add functionality
        input.setOnAction(event -> sendFriendRequest(input.getText()));
        btnSearch.setOnAction(event -> sendFriendRequest(input.getText()));
    }
    
    private void sendFriendRequest(String friendUsername){
        JSONObject out = new JSONObject();
    }
    
}
