package chessBug.controllerInterfaces;

import chessBug.network.User;
import java.util.List;

public interface IFriendRequestController extends IDatabaseCheckController {
    /** sendFriendRequest - place friend request in the database
    * @param - username : the username of the person who is receiving the friend request
    * @return - returns status of database update
    */
    public boolean sendFriendRequest(String username);
    /** receiveFriendRequest - get a list of friend requests directed at current user
    * @return - a list of users that have requested a friend status with the current user
    */
    public List<User> receiveFriendRequest();
    /** acceptFriendRequest - accepts an existing friend request and updates database
    * @param - username : the username of the person who is receiving the friend request
    */
    public void acceptFriendRequest(String username);
    /** denyFriendRequest - denies an existing friend request and updates database
    * @param - username : the username of the person who is receiving the friend request
    */
    public void denyFriendRequest(String username);
}
