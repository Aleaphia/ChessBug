
package chessBug.home;

import chessBug.misc.*;
import chessBug.network.*;
import chessBug.game.*;
import java.util.List;
import javafx.scene.Node;



public class HomeController implements IGameSelectionController, IFriendRequestController{
    //Database Connection
    private Client client;
    //MVC
    private HomeView view;
    
    public HomeController(Client client){
        //Connect to database
        this.client = client;
        
        //Create view
        view = new HomeView(this);
    }
    
    public Node getPage(){ return view.getPage();}
    public String getUserName(){return client.getOwnUser().getUsername();}
    public List<Friend> getFriends(){return client.getFriends();}
    
    //Overriden Methods
    //IFriendRequestController methods
    @Override public boolean sendFriendRequest(String username){return client.sendFriendRequest(username);}
    @Override public List<User> receiveFriendRequest(){return client.getFriendRequests();}
    @Override public void acceptFriendRequest(String user){client.acceptFriendRequest(user);}
    
    //IGameSelectionController methods
    @Override public List<Match> getOpenMatchList(){return client.getOpenMatches();}
    @Override public List<Match> receiveMatchRequest(){return client.getMatchRequests();}
    @Override public void acceptMatchRequest(Match match){client.acceptMatchRequest(match);}
    @Override public void selectGame(Match match){
        view.setPage(new GameController(client, match).getPage());
    }
}
