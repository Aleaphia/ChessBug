package chessBug.misc;

import chessBug.controllerInterfaces.IFriendRequestController;
import chessBug.network.*;
import java.util.List;
import java.util.ArrayList;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;

public class ReceiveFriendRequestUI {
    private final VBox page = new VBox();
    private final VBox friendRequests = new VBox();
    IFriendRequestController controller;

    boolean newRequestsFlag = false;
    List<User> cachedRequests = new ArrayList<>();
    
    public ReceiveFriendRequestUI(IFriendRequestController controller){
        this.controller = controller;
        buildRequestField();
        
        //Add database checks
        this.controller.addToDatabaseCheckList(() -> new Thread(() -> {
            try {
                List<User> requests = controller.receiveFriendRequest();
                if(!requests.equals(cachedRequests)) {
                    cachedRequests = requests;
                    newRequestsFlag = true;
                }
            } catch (NetworkException ignored) {} // We'll try again soon
        }).start());
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
        if(!newRequestsFlag) return;
        //System.out.println("Debug: ReceiveFriendRequestUI DatabaseCheck" );
        friendRequests.getChildren().clear();
        //System.out.println("DEBUG: " + friendRequestsList.isEmpty());
        cachedRequests.forEach(user -> {
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
                try {
                    controller.acceptFriendRequest(user.getUsername());
                    curr.getChildren().clear();
                } catch(NetworkException e) {
                    System.err.println("Unable to accept friend request!");
                    e.printStackTrace();
                }
            });
            deny.setOnAction(event -> {
                try {
                    controller.denyFriendRequest(user.getUsername());
                    curr.getChildren().clear();
                } catch(NetworkException e) {
                    System.err.println("Unable to accept friend request!");
                    e.printStackTrace();
                }
            });
        });
        
        if(friendRequests.getChildren().isEmpty()){
            friendRequests.getChildren().add(new Label("No pending requests"));
        }
    }
}
