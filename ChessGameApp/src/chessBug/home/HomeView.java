/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package chessBug.home;

import org.json.JSONObject;

import chessBug.misc.GameSelectionUI;
import chessBug.misc.ReceiveFriendRequestUI;
import chessBug.misc.SendFriendRequestUI;
import chessBug.network.Friend;
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
import java.util.Map;
import java.util.List;
import java.util.ArrayList;


public class HomeView {
    
    private HomeController controller;
    private BorderPane page = new BorderPane();
    private VBox currentContent;
    private VBox friendsListContent;
    
    private Label gamesPlayed;
    private Label currentGames;
    private Label wins;
    private Label losses;
    private Label draws;
    
    boolean newStatsFlag = false;
    private JSONObject cachedStats = new JSONObject(Map.of("Won", 0, "Lost", 0, "Draw", 0, "Current", 0, "Total", 0));

    boolean newFriendsFlag = false;
    private List<Friend> cachedFriends = new ArrayList<>();
    
    protected HomeView(HomeController controller){
        this.controller = controller;
        
        //Create view
        VBox userStats = buildUserStats();
        VBox friendsBox = buildFriends();
        buildCurrentContent();
        
        page.setLeft(userStats);
        page.setRight(friendsBox);
        page.setCenter(currentContent);

        //Style
        page.getStyleClass().add("section");
        
        controller.addToDatabaseCheckList(() -> {
            //System.out.println("Debug: GameSelectionUI DatabaseCheck" );
            //Reload friend list
            populateFriendsContent();
            updateStatsLabels();
        });
        controller.addToDatabaseCheckList(() -> new Thread(() -> {
            JSONObject stats = controller.getGameStats();
            if(!stats.equals(cachedStats)) {
                newStatsFlag = true;
                cachedStats = stats;
            }
            List<Friend> friends = controller.getFriends();
            if(!friends.equals(cachedFriends)) {
                newFriendsFlag = true;
                cachedFriends = friends;
            }
        }).start());
    }
    
    public BorderPane getPage(){return page;}
    public void setPage(BorderPane page){this.page = page;}

    private void updateStatsLabels() {
        if(!newStatsFlag) return; 
        
        int current = (cachedStats.optInt("Total", 0)- cachedStats.optInt("Current"));
        int past = cachedStats.optInt("Current", 0);
        currentGames.setText("" + current + ((current != 1)? " games in progress" : " game in progress"));
        gamesPlayed.setText("" + past + ((past != 1)? " games completed" : " game completed"));
        wins.setText("\tWins: " + cachedStats.optInt("Won", 0));
        losses.setText("\tLosses: " + cachedStats.optInt("Lost", 0));
        draws.setText("\tDraws: " + cachedStats.optInt("Draw", 0));
    }
    
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
        
        gamesPlayed = new Label();
        currentGames = new Label();
        wins = new Label();
        losses = new Label();
        draws = new Label();
        updateStatsLabels();
        
        statsSection.getChildren().addAll(statsTitle, currentGames, gamesPlayed, wins, losses, draws);
        
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
        if(!newFriendsFlag) return;
        //Clear firends list
        friendsListContent.getChildren().clear();
        //Add each friend to the list
        cachedFriends.forEach(friend -> {
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
            new GameSelectionUI(
                    controller, GameSelectionUI.GameStatus.REQUESTED,
                    (() -> controller.receiveMatchRequest())).getPage(),
            new GameSelectionUI(
                    controller, GameSelectionUI.GameStatus.IN_PROGRESS,
                    (() -> controller.getOpenMatchList())).getPage()
        );
        
        //Style
        currentContent.setAlignment(Pos.TOP_CENTER);
        currentContent.getStyleClass().add("current-content");
        VBox.setVgrow(currentContent, Priority.ALWAYS);
        sectionTitle.getStyleClass().add("h1");
    }
}
