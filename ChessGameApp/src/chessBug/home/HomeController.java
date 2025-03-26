
package chessBug.home;

import chessBug.controllerInterfaces.IGameSelectionController;
import chessBug.controllerInterfaces.IFriendRequestController;
import chessBug.misc.*;
import chessBug.network.*;
import chessBug.game.*;
import java.util.List;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;




public class HomeController implements IGameSelectionController, IFriendRequestController{
    //Database Connection
    private Client client;
    private DatabaseCheckList databaseCheckList;
    //Page
    private HBox page = new HBox();
    //MVC
    private HomeView view;
    
    public HomeController(Client client, DatabaseCheckList databaseCheckList){
        //Connect to database
        this.client = client;
        this.databaseCheckList = databaseCheckList;
        
        //Create view
        view = new HomeView(this);
        Region leftRegion = new Region();
        Region rightRegion = new Region();
        
        page.getChildren().addAll(leftRegion,view.getPage(),rightRegion);
        
        //Style and format
        page.getStyleClass().add("padding");
        HBox.setHgrow(leftRegion, Priority.ALWAYS);
        HBox.setHgrow(rightRegion, Priority.ALWAYS);
        
    }
    
    //Getter/Setter methods
    public Pane getPage(){ return page;}
    public String getUserName(){return client.getOwnUser().getUsername();}
    public List<Friend> getFriends(){return client.getFriends();}
    public int getCompleteGamesNumber(){return client.getClosedMatches().size();}
    public int getCurrentGamesNumber(){return client.getOpenMatches().size();}
    public List<Match> getOpenMatchList(){return client.getOpenMatches();}
    public List<Match> receiveMatchRequest(){return client.getMatchRequests();}
    
    //Overriden Methods
    //IDatabaseCheckInterface methods
    @Override public void addToDatabaseCheckList(DatabaseCheck item){databaseCheckList.add(item);}
    //IFriendRequestController methods
    @Override public boolean sendFriendRequest(String username){return client.sendFriendRequest(username);}
    @Override public List<User> receiveFriendRequest(){return client.getFriendRequests();}
    @Override public void acceptFriendRequest(String user){client.acceptFriendRequest(user);}
    @Override public void denyFriendRequest(String user){client.denyFriendRequest(user);}
    
    //IGameSelectionController methods
    @Override public String getUsername(){return client.getOwnUser().getUsername();}
    @Override public void acceptMatchRequest(Match match){client.acceptMatchRequest(match);}
    @Override public void denyMatchRequest(Match match){client.denyMatchRequest(match);}
    @Override public void selectGame(Match match){
        page.getChildren().set(1, new GameController(client, databaseCheckList, match).getPage());
    }
    @Override public void forfitMatch(Match match){client.forfitMatch(match);}
    
}
