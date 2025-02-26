
package chessBug.home;

import chessBug.misc.*;
import chessBug.network.*;
import chessBug.game.*;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.layout.Pane;




public class HomeController implements IGameSelectionController, IFriendRequestController{
    //Database Connection
    private Client client;
    //Page
    private Pane page = new Pane();
    //MVC
    private HomeView view;
    
    public HomeController(Client client){
        //Connect to database
        this.client = client;
        
        //Create view
        view = new HomeView(this);
        page.getChildren().add(view.getPage());
    }
    
    public Node getPage(){ return page;}
    public String getUserName(){return client.getOwnUser().getUsername();}
    public List<Friend> getFriends(){return client.getFriends();}
    
    //Overriden Methods
    //IFriendRequestController methods
    @Override public boolean sendFriendRequest(String username){return client.sendFriendRequest(username);}
    @Override public List<User> receiveFriendRequest(){return client.getFriendRequests();}
    @Override public void acceptFriendRequest(String user){client.acceptFriendRequest(user);}
    @Override public void denyFriendRequest(String user){client.denyFriendRequest(user);}
    
    //IGameSelectionController methods
    @Override public String getUsername(){return client.getOwnUser().getUsername();}
    @Override public List<Match> getOpenMatchList(){return client.getOpenMatches();}
    @Override public List<Match> receiveMatchRequest(){return client.getMatchRequests();}
    @Override public void acceptMatchRequest(Match match){client.setMatchStatus(match, Match.Status.WHITE_TURN.toString());}
    @Override public void denyMatchRequest(Match match){client.denyMatchRequest(match);}
    @Override public void selectGame(Match match){
        page.getChildren().clear();
        page.getChildren().add(new GameController(client, match).getPage());
    }
}
