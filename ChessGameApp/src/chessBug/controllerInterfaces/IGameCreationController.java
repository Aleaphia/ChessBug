package chessBug.controllerInterfaces;

import chessBug.network.User;
import chessBug.network.Friend;
import chessBug.network.NetworkException;

import java.util.List;

public interface IGameCreationController {
    public List<Friend> getFriendList() throws NetworkException;
    /** sendGameRequest - sends new game request to database
     * @param - playerColor : what color is the player requesting
     * @param - opponent: who are they playing against
     */
    public void sendGameRequest(Boolean playerColor, User opponent) throws NetworkException;
}
