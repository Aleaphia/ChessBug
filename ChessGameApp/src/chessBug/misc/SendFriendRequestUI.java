package chessBug.misc;

import chessBug.controllerInterfaces.IFriendRequestController;
import chessBug.network.NetworkException;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.util.ArrayList;

public class SendFriendRequestUI {
    private final VBox page = new VBox();
    IFriendRequestController controller;
    Label errorMsgSpace = new Label();
    
    public SendFriendRequestUI(IFriendRequestController controller){
        this.controller = controller;
        errorMsgSpace.getStyleClass().add("error");
        buildClosedRequestField();
    }
    public VBox getPage(){return page;}
    
    private void buildClosedRequestField(){
        //Create UI Layout
        Button newFriend = new Button("Add Friend");
        page.getChildren().add(newFriend);
        
        //Function
        newFriend.setOnAction(event -> buildFriendRequest());
    }
    private void buildFriendRequest(){
        //Clear page
        page.getChildren().clear();
        
        //Create UI Layout
        TextField input = new TextField();
        Button btnSearch = new Button("Send");
        page.getChildren().addAll(new Label("Search by Username:"), input, btnSearch, errorMsgSpace);
        
        //Add functionality
        input.setOnAction(event -> sendFriendRequest(input.getText()));
        btnSearch.setOnAction(event -> sendFriendRequest(input.getText()));
    }
    
    
    private void sendFriendRequest(String friendUsername){
        try{
            //Make sure there is a name in the textbox
            if(friendUsername.isEmpty())
                throw new Exception("Empty search");

            //Check if they are already friends
            ArrayList<String> friendNameList = new ArrayList<>();
            controller.getFriends().forEach(friend -> friendNameList.add(friend.getUsername()));
            if (friendNameList.contains(friendUsername))
                 throw new Exception("Already friends with " + friendUsername);
            
            //Valid friend request ----------------------------------------------
            page.getChildren().clear();

            //Try to send friend request and display sucess status
            try {
                controller.sendFriendRequest(friendUsername);
                page.getChildren().add(new Label("Request sent"));
            } catch (NetworkException e) {
                page.getChildren().add(new Label("Server error: Request not sent"));
            }

            //Close request field
            buildClosedRequestField();
        }
        catch(Exception e){ //Output error messages
            errorMsgSpace.setText(e.getMessage());
        }
    }
    
}
