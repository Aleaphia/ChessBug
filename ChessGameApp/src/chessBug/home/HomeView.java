/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package chessBug.home;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;

public class HomeView {
    
    private HomeController controller;

    //Page state
    private BorderPane page = new BorderPane();
    private VBox homePage;
    private VBox currGames;
    private VBox friends;
    
    protected HomeView(HomeController controller){
        this.controller = controller;
        
        buildHomePage();
        
        //Place parts together
        page.setLeft(homePage);
    }
    public BorderPane getPage(){return page;}
    
    private void buildHomePage(){
        homePage = new VBox(20); // Vertical layout with spacing between sections
        homePage.setPadding(new Insets(20, 20, 20, 20));
        homePage.setStyle("-fx-background-color: #36393f; -fx-text-fill: white;");  // Set text color to white for the entire homePage
    
        // Welcome message
        Label welcomeMessage = new Label("Welcome to ChessBug!");
        welcomeMessage.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;"); // Ensure text is white
        
        // User info
        Label userInfo = new Label("User: " + controller.getUserName());
        userInfo.setStyle("-fx-font-size: 18px; -fx-text-fill: white;"); // Ensure text is white
    
        // Add a separator line
        homePage.getChildren().addAll(welcomeMessage, userInfo, new Separator());
    
        // Recent game statistics
        VBox statsSection = new VBox(10);
        statsSection.setStyle("-fx-background-color: #2a2d34; -fx-padding: 10px; -fx-border-radius: 8px; -fx-text-fill: white;");
        Label statsTitle = new Label("Recent Game Statistics");
        statsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label gamesPlayed = new Label("Games Played: " + controller.getMatchList().size());
        gamesPlayed.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");  // Ensure the text is white
        Label wins = new Label("Wins: 10"); //TODO
        wins.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
        Label losses = new Label("Losses: 5"); //TODO
        losses.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
        
        statsSection.getChildren().addAll(statsTitle, gamesPlayed, wins, losses);
        homePage.getChildren().add(statsSection);
        
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
        homePage.getChildren().add(activityFeed);
        
        // Featured or Live Game section (Optional)
        VBox liveGameSection = new VBox(10);
        liveGameSection.setStyle("-fx-background-color: #2a2d34; -fx-padding: 10px; -fx-border-radius: 8px; -fx-text-fill: white;");
        Label liveGameTitle = new Label("Featured Game");
        liveGameTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label liveGame = new Label("Live Game: ChessMaster123 vs. User456");
        liveGame.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
        Button joinGameButton = new Button("Join Game");
        joinGameButton.setStyle("-fx-background-color: #4e8af3; -fx-text-fill: white;");
        
        liveGameSection.getChildren().addAll(liveGameTitle, liveGame, joinGameButton);
        homePage.getChildren().add(liveGameSection);
        
        // Add a final separator for design clarity
        homePage.getChildren().add(new Separator());
    }
    
}
