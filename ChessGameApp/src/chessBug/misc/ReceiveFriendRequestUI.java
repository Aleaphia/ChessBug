package chessBug.misc;

import chessBug.controllerInterfaces.IFriendRequestController;
import chessBug.network.*;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;

public class ReceiveFriendRequestUI {
    private VBox page = new VBox();
    private VBox friendRequests = new VBox();
    IFriendRequestController controller;
    
    public ReceiveFriendRequestUI(IFriendRequestController controller){
        this.controller = controller;
        buildRequestField();
        
        //Add database checks
        this.controller.addToDatabaseCheckList(() -> updateRequestField());
    }
    
    public VBox getPage(){return page;}
    
    private void buildRequestField(){
        //Layout
        Label sectionTitle = new Label("Friend Requests");
        ScrollPane scroll = new ScrollPane(friendRequests);        
        page.getChildren().addAll(sectionTitle, scroll);
        updateRequestField();
        
        //Style
        sectionTitle.getStyleClass().add("h2");
        friendRequests.getStyleClass().add("scrollBackground");
    }
    
    private void updateRequestField(){
        //System.out.println("Debug: ReceiveFriendRequestUI DatabaseCheck" );
        friendRequests.getChildren().clear();
        List<User> friendRequestsList = controller.receiveFriendRequest();
        //System.out.println("DEBUG: " + friendRequestsList.isEmpty());
        friendRequestsList.forEach(user -> {
            HBox curr = new HBox();
            Label label = new Label(user.getUsername());
            Button accept = new Button("Accept");
            Button deny = new Button("Deny");
            curr.getChildren().addAll(label, accept, deny);
            friendRequests.getChildren().add(curr);

            //Style
            curr.setAlignment(Pos.CENTER_LEFT);
            curr.setStyle("-fx-padding: 5px; ");
            label.getStyleClass().add("h3");

            //Function
            accept.setOnAction(event -> {
                controller.acceptFriendRequest(user.getUsername());
                curr.getChildren().clear();
            });
            deny.setOnAction(event -> {
                controller.denyFriendRequest(user.getUsername());
                curr.getChildren().clear();
            });
        });
        
        if(friendRequests.getChildren().isEmpty()){
            friendRequests.getChildren().add(new Label("No pending requests"));
        }
    }
}
