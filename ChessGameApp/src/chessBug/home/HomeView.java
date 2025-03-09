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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;


public class HomeView {
    
    private HomeController controller;
    private HBox page = new HBox();
    private BorderPane pageLayout = new BorderPane();
    private VBox currentContent;
    private VBox friendsListContent;
    
    protected HomeView(HomeController controller){
        this.controller = controller;
        
        //Create view
        //Game Prompt Panel
        Region leftRegion = new Region();
        Region rightRegion = new Region();
        
        page.getChildren().addAll(leftRegion, pageLayout, rightRegion);
        
      
        //Layout page
        VBox userStats = buildUserStats();
        VBox friends = buildFriends();
        buildCurrentContent();
        
        pageLayout.setLeft(userStats);
        pageLayout.setRight(friends);
        pageLayout.setCenter(currentContent);

        //Style
        page.getStyleClass().add("padding");
        page.getStylesheets().add(getClass().getResource("/HomeView.css").toExternalForm());
        HBox.setHgrow(leftRegion, Priority.ALWAYS);
        HBox.setHgrow(rightRegion, Priority.ALWAYS);
                
        continueDatabaseChecks();
    }
    private void continueDatabaseChecks(){
        //Check database
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), (ActionEvent event) -> {
            //Add repeated database checks here ================================
            //Reload friend list
            populateFriendsContent();
            // =================================================================
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    public HBox getPage(){return page;}
    public void setPage(BorderPane pageLayout){this.pageLayout = pageLayout;}
    
    private VBox buildUserStats(){
        VBox userStatsSpace = new VBox(20);
        userStatsSpace.setPadding(new Insets(20));
        userStatsSpace.getStyleClass().add("user-stats");
        VBox.setVgrow(userStatsSpace, Priority.ALWAYS);
        
        Label welcomeMessage = new Label("Welcome to ChessBug!");
        welcomeMessage.getStyleClass().addAll("welcome-message", "header");
        
        Label userInfo = new Label("User: " + controller.getUserName());
        userInfo.getStyleClass().add("user-info");
        
        userStatsSpace.getChildren().addAll(welcomeMessage, userInfo, new Separator());
        
        VBox statsSection = new VBox(10);
        statsSection.getStyleClass().add("stats-section");
        
        Label statsTitle = new Label("Recent Game Statistics");
        statsTitle.getStyleClass().add("section-title");
        
        Label gamesPlayed = new Label("Games Played: " + controller.getOpenMatchList().size());
        Label wins = new Label("Wins: 10");
        Label losses = new Label("Losses: 5");
        
        statsSection.getChildren().addAll(statsTitle, gamesPlayed, wins, losses);
        userStatsSpace.getChildren().add(statsSection);
        
        return userStatsSpace;
    }
    
    private VBox buildFriends() {
        VBox friendsSpace = new VBox(10);
        friendsSpace.setPadding(new Insets(20));
        friendsSpace.getStyleClass().add("friends-space");
        VBox.setVgrow(friendsSpace, Priority.ALWAYS);
        
        Label header = new Label("Friends");
        header.getStyleClass().add("header");
        
        friendsListContent = new VBox(5);
        populateFriendsContent();
        
        VBox sendRequestSection = new VBox(10, new SendFriendRequestUI(controller).getPage());
        sendRequestSection.getStyleClass().add("send-request-section");
        
        friendsSpace.getChildren().addAll(header, new Separator(), friendsListContent, sendRequestSection);
        return friendsSpace;
    }
    
    private void populateFriendsContent(){
        friendsListContent.getChildren().clear();
        controller.getFriends().forEach(friend -> {
            Label curr = new Label(friend.getUsername());
            curr.getStyleClass().add("friend-label");
            friendsListContent.getChildren().add(curr);
        });
    }
    
    private void buildCurrentContent() {
        currentContent = new VBox(10);
        currentContent.setPadding(new Insets(20));
        currentContent.getStyleClass().add("current-content");
        VBox.setVgrow(currentContent, Priority.ALWAYS);
        
        Label sectionTitle = new Label("Game & Requests");
        sectionTitle.getStyleClass().add("header");
        
        currentContent.getChildren().addAll(
            sectionTitle,
            new ReceiveFriendRequestUI(controller).getPage(),
            new GameSelectionUI(controller).getPage()
        );
    }
}