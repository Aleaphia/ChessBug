package chessBug.misc;

import chessBug.network.*;
import java.util.List;

import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;



public class ReceiveFriendRequestUI {
    private VBox page = new VBox();
    IFriendRequestController controller;
    
    public ReceiveFriendRequestUI(IFriendRequestController controller){
        this.controller = controller;
        
        buildRequestField();
    }
    public VBox getPage(){return page;}
    
    private void buildRequestField(){
        List<User> friendRequests = controller.receiveFriendRequest();
        if (!friendRequests.isEmpty()){
            page.getChildren().add(new Label("Friend Requests"));
            friendRequests.forEach(user -> {
                HBox curr = new HBox();
                Button accept = new Button("Accept");
                Button deny = new Button("Deny");
                curr.getChildren().addAll(new Label(user.getUsername()), accept, deny);
                page.getChildren().add(curr);
                
                accept.setOnAction(event -> {
                    controller.acceptFriendRequest(user.getUsername());
                    curr.getChildren().clear();
                });
                deny.setOnAction(event -> {
                    controller.denyFriendRequest(user.getUsername());
                    curr.getChildren().clear();
                });
            });
        }
    }
}
