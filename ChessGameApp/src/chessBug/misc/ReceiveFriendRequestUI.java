package chessBug.misc;

import chessBug.network.*;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;

import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Duration;



public class ReceiveFriendRequestUI {
    private VBox page = new VBox();
    private VBox friendRequests = new VBox();
    IFriendRequestController controller;
    
    public ReceiveFriendRequestUI(IFriendRequestController controller){
        this.controller = controller;
        
        buildRequestField();
        continueDatabaseChecks();
    }
    private void continueDatabaseChecks(){
        //Check database
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), (ActionEvent event) -> {
            //Add repeated database checks here ================================
            //Reload game info
            updateRequestField();
            // =================================================================
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    public VBox getPage(){return page;}
    
    private void buildRequestField(){
        page.getChildren().addAll(new Label("Friend Requests"), friendRequests);
        updateRequestField();
    }
    
    private void updateRequestField(){
        friendRequests.getChildren().clear();
        List<User> friendRequestsList = controller.receiveFriendRequest();
        if (friendRequestsList.isEmpty()){
            friendRequests.getChildren().add(new Label("No pending requests"));
        }
        else{
            friendRequestsList.forEach(user -> {
                HBox curr = new HBox();
                Button accept = new Button("Accept");
                Button deny = new Button("Deny");
                curr.getChildren().addAll(new Label(user.getUsername()), accept, deny);
                friendRequests.getChildren().add(curr);
                
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
