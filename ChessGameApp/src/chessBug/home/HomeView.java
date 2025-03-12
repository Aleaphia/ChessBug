/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package chessBug.home;

import chessBug.misc.GameSelectionUI;
import chessBug.misc.ReceiveFriendRequestUI;
import chessBug.misc.SendFriendRequestUI;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;


public class HomeView {
    
    private HomeController controller;
    private BorderPane page = new BorderPane();
    private VBox currentContent;
    private VBox friendsListContent;
    
    private Label gamesPlayed;
    private Label currentGames;
    private Label wins;
    private Label losses;
    
    
    protected HomeView(HomeController controller){
        this.controller = controller;
        
        //Create view
        VBox userStats = buildUserStats();
        VBox friends = buildFriends();
        buildCurrentContent();
        
        page.setLeft(userStats);
        page.setRight(friends);
        page.setCenter(currentContent);

        //Style
        page.getStyleClass().add("section");
        page.getStylesheets().add(getClass().getResource("/HomeView.css").toExternalForm());
        
        controller.addToDatabaseCheckList(() -> {
            //System.out.println("Debug: GameSelectionUI DatabaseCheck" );
            //Reload friend list
            populateFriendsContent();
            gamesPlayed.setText("Games Played: " + controller.getCompleteGamesNumber());
            currentGames.setText("Games In Progress: " + controller.getCurrentGamesNumber());
        });
    }
    
    public BorderPane getPage(){return page;}
    public void setPage(BorderPane page){this.page = page;}
    
    private VBox buildUserStats(){
        //Layout content
        VBox userStatsSpace = new VBox(20);
        userStatsSpace.setPadding(new Insets(20));
        userStatsSpace.getStyleClass().add("user-stats");
        VBox.setVgrow(userStatsSpace, Priority.ALWAYS);
        
        Label welcomeMessage = new Label("Welcome to ChessBug!");
        welcomeMessage.getStyleClass().addAll("welcome-message", "h1");
        
        Label userInfo = new Label("User: " + controller.getUserName());
        userInfo.getStyleClass().addAll("h2", "user-info");
        
        VBox statsSection = new VBox(10);
        statsSection.getStyleClass().add("stats-section");
        
        userStatsSpace.getChildren().addAll(welcomeMessage, userInfo, new Separator(), statsSection);
        
        
        //Statistics sub-section (statsSection) layout
        Label statsTitle = new Label("Recent Game Statistics");
        statsTitle.getStyleClass().addAll("h3","section-title");
        
        gamesPlayed = new Label("Games Played: " + controller.getCompleteGamesNumber());
        currentGames = new Label("Games In Progress: " + controller.getCurrentGamesNumber());
        
        statsSection.getChildren().addAll(statsTitle, gamesPlayed, currentGames);
        
        //Return parent node
        return userStatsSpace;
    }
    
    private VBox buildFriends() {
        //Layout content
        VBox friendsSpace = new VBox(10);
        friendsSpace.setPadding(new Insets(20));
        friendsSpace.getStyleClass().add("friends-space");
        VBox.setVgrow(friendsSpace, Priority.ALWAYS);
        
        Label header = new Label("Friends");
        header.getStyleClass().add("h1");
        
        friendsListContent = new VBox(5);
        ScrollPane scroll = new ScrollPane(friendsListContent);
        populateFriendsContent();
        
        VBox sendRequestSection = new VBox(10, new SendFriendRequestUI(controller).getPage());
        sendRequestSection.getStyleClass().add("send-request-section");
        
        friendsSpace.getChildren().addAll(header, new Separator(), scroll, sendRequestSection);
        
        //Style
        friendsListContent.getStyleClass().add("scrollBackground");
        
        return friendsSpace;
    }
    
    private void populateFriendsContent(){
        //Clear firends list
        friendsListContent.getChildren().clear();
        //Add each friend to the list
        controller.getFriends().forEach(friend -> {
            Label curr = new Label(friend.getUsername());
            curr.getStyleClass().add("friend-label");
            friendsListContent.getChildren().add(curr);
        });
    }
    
    private void buildCurrentContent() {
        //Layout content
        currentContent = new VBox(10);
        Label sectionTitle = new Label("Game & Requests");
        
        currentContent.getChildren().addAll(
            sectionTitle, new Separator(),
            new ReceiveFriendRequestUI(controller).getPage(),
            new GameSelectionUI(controller).getPage()
        );
        
        //Style
        currentContent.setAlignment(Pos.TOP_CENTER);
        currentContent.getStyleClass().add("current-content");
        VBox.setVgrow(currentContent, Priority.ALWAYS);
        sectionTitle.getStyleClass().add("h1");
    }
}
