/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package chessBug.home;

import chessBug.misc.GameSelectionUI;
import chessBug.misc.ReceiveFriendRequestUI;
import chessBug.misc.SendFriendRequestUI;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class HomeView {
    
    private HomeController controller;

    //Page state
    private BorderPane page = new BorderPane();
    private VBox currentContent;
    private VBox friendsListContent;
    
    protected HomeView(HomeController controller){
        this.controller = controller;
        
        buildUserStats();
        buildCurrentContent();
        
        //Place parts together
        page.setLeft(buildUserStats());
        page.setRight(buildFriends());
        page.setCenter(currentContent);
    }
    public BorderPane getPage(){return page;}
    public void setPage(BorderPane page){this.page = page;}
    
    private VBox buildUserStats(){
        VBox userStatsSpace = new VBox(20); // Vertical layout with spacing between sections
        userStatsSpace.setPadding(new Insets(20, 20, 20, 20));
        userStatsSpace.setStyle("-fx-background-color: #36393f; -fx-text-fill: white;");  // Set text color to white for the entire homePage
    
        // Welcome message
        Label welcomeMessage = new Label("Welcome to ChessBug!");
        welcomeMessage.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;"); // Ensure text is white
        
        // User info
        Label userInfo = new Label("User: " + controller.getUserName());
        userInfo.setStyle("-fx-font-size: 18px; -fx-text-fill: white;"); // Ensure text is white
    
        // Add a separator line
        userStatsSpace.getChildren().addAll(welcomeMessage, userInfo, new Separator());
    
        // Recent game statistics
        VBox statsSection = new VBox(10);
        statsSection.setStyle("-fx-background-color: #2a2d34; -fx-padding: 10px; -fx-border-radius: 8px; -fx-text-fill: white;");
        Label statsTitle = new Label("Recent Game Statistics");
        statsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label gamesPlayed = new Label("Games Played: " + controller.getOpenMatchList().size());
        gamesPlayed.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");  // Ensure the text is white
        Label wins = new Label("Wins: 10"); //TODO
        wins.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
        Label losses = new Label("Losses: 5"); //TODO
        losses.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
        
        statsSection.getChildren().addAll(statsTitle, gamesPlayed, wins, losses);
        userStatsSpace.getChildren().add(statsSection);
        
        // Recent activity feed (activity log)
        VBox activityFeed = new VBox(10);
        activityFeed.setStyle("-fx-background-color: #2a2d34; -fx-padding: 10px; -fx-border-radius: 8px; -fx-text-fill: white;");
        Label activityFeedTitle = new Label("Recent Activity");
        activityFeedTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label activity1 = new Label("You played a match with User456.");
        activity1.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
        Label activity2 = new Label("You won against User789.");
        activity2.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
        Label activity3 = new Label("You started a new challenge with User321.");
        activity3.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
        
        activityFeed.getChildren().addAll(activityFeedTitle, activity1, activity2, activity3);
        userStatsSpace.getChildren().add(activityFeed);
        
        // Featured or Live Game section (Optional)
        VBox liveGameSection = new VBox(10);
        liveGameSection.setStyle("-fx-background-color:rgb(42, 45, 52); -fx-padding: 10px; -fx-border-radius: 8px; -fx-text-fill: white;");
        Label liveGameTitle = new Label("Featured Game");
        liveGameTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label liveGame = new Label("Live Game: ChessMaster123 vs. User456");
        liveGame.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
        Button joinGameButton = new Button("Join Game");
        joinGameButton.setStyle("-fx-background-color: #4e8af3; -fx-text-fill: white;");
        
        liveGameSection.getChildren().addAll(liveGameTitle, liveGame, joinGameButton);
        userStatsSpace.getChildren().add(liveGameSection);
        
        // Add a final separator for design clarity
        userStatsSpace.getChildren().add(new Separator());
        
        return userStatsSpace;
    }
    private VBox buildFriends() {
        VBox friendsSpace = new VBox(10); // Added spacing
        friendsSpace.setPadding(new Insets(20));
        friendsSpace.setStyle("-fx-background-color: #2a2d34; -fx-border-radius: 8px; -fx-text-fill: white;"); // Dark theme
    
        // Header
        Label header = new Label("Friends");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
    
        // Friends list
        friendsListContent = new VBox(5);
        populateFriendsContent();
    
        // Add friend request UI
        VBox sendRequestSection = new VBox(10, new SendFriendRequestUI(controller).getPage());
        sendRequestSection.setStyle("-fx-background-color: #3b3f45; -fx-padding: 10px; -fx-border-radius: 5px;");
    
        friendsSpace.getChildren().addAll(header, friendsListContent, sendRequestSection);
        return friendsSpace;
    }
    
    private void populateFriendsContent(){
        controller.getFriends().forEach(friend -> {
            Label curr = new Label(friend.getUsername());
            curr.setStyle("-fx-text-fill: white;");
            
            friendsListContent.getChildren().add(curr);
            
            curr.setOnMouseClicked(event ->{
                //TODO
            });
        });
    }
    private void buildCurrentContent() {
        currentContent = new VBox(10);
        currentContent.setPadding(new Insets(20));
        currentContent.setStyle("-fx-background-color: #2a2d34; -fx-border-radius: 8px; -fx-text-fill: white;"); // Dark theme
    
        Label sectionTitle = new Label("Game & Requests");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
    
        currentContent.getChildren().addAll(
            sectionTitle,
            new ReceiveFriendRequestUI(controller).getPage(),
            new GameSelectionUI(controller).getPage()
        );
    }
}
