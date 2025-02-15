
package chessBug.home;

import chessBug.network.*;
import java.util.List;
import javafx.scene.Node;



public class HomeController {
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
    public List<Match> getMatchList(){return client.getMatches();}
    public List<Friend> getFriends(){return client.getFriends();}
}
