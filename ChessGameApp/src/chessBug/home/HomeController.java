
package chessBug.home;

import chessBug.network.*;
import chessBug.game.*;
import java.util.List;
import javafx.scene.Node;



public class HomeController implements IGameSelectionController{
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
    public List<Match> getOpenMatchList(){return client.getOpenMatches();} //TODO
    public List<Friend> getFriends(){return client.getFriends();}
    public boolean sendFriendRequest(String username){return client.sendFriendRequest(username);}
    public void selectGame(Match match){
        view.setPage(new GameController(client, match).getPage());
    }
}
