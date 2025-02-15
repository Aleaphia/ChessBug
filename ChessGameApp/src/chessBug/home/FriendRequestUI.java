package chessBug.home;

import chessBug.network.*;

import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.json.JSONObject;




public class FriendRequestUI {
    private VBox page = new VBox();
    HomeController controller;
    
    FriendRequestUI(HomeController controller){
        this.controller = controller;
        
        buildClosedRequestField();
    }
    public VBox getPage(){return page;}
    
    private void buildClosedRequestField(){
        Button newFriend = new Button("Add Friend");
        newFriend.setOnAction(event -> buildFriendRequest());
        page.getChildren().add(newFriend);
    }
    private void buildFriendRequest(){
        page.getChildren().clear();
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
        out.put("user1", controller.getUserName());
        out.put("user2", friendUsername);
        
        //Output message
        page.getChildren().clear();
        page.getChildren().add(new Label("Request Sent"));
        buildClosedRequestField();
    }
    
}
