
package chessBug.home;

import chessBug.controllerInterfaces.IGameSelectionController;
import chessBug.controllerInterfaces.IFriendRequestController;
import chessBug.misc.*;
import chessBug.network.*;
import chessBug.game.*;
import java.util.List;

import org.json.JSONObject;

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
    public List<Friend> getFriends() throws NetworkException {return client.getFriends();}
    public List<Match> getOpenMatchList() throws NetworkException {return client.getOpenMatches();}
    public List<Match> receiveMatchRequest() throws NetworkException {return client.getMatchRequests();}
    public JSONObject getGameStats() throws NetworkException { return client.getMatchStats(); }
    
    //Overriden Methods
    //IDatabaseCheckInterface methods
    @Override public void addToDatabaseCheckList(DatabaseCheck item){databaseCheckList.add(item);}
    //IFriendRequestController methods
    @Override public void sendFriendRequest(String username) throws NetworkException {client.sendFriendRequest(username);}
    @Override public List<User> receiveFriendRequest() throws NetworkException {return client.getFriendRequests();}
    @Override public void acceptFriendRequest(String user) throws NetworkException {client.acceptFriendRequest(user);}
    @Override public void denyFriendRequest(String user) throws NetworkException {client.denyFriendRequest(user);}
    
    //IGameSelectionController methods
    @Override public String getUsername(){return client.getOwnUser().getUsername();}
    @Override public void acceptMatchRequest(Match match) throws NetworkException {client.acceptMatchRequest(match);}
    @Override public void denyMatchRequest(Match match) throws NetworkException {client.denyMatchRequest(match);}
    @Override public void selectGame(Match match){
        page.getChildren().set(1, new GameController(client, databaseCheckList, match).getPage());
    }
    @Override public void forfeitMatch(Match match) throws NetworkException {client.forfeitMatch(match);}
    
}
