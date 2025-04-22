package chessBug.controllerInterfaces;

import chessBug.network.Friend;
import chessBug.network.NetworkException;
import chessBug.network.User;
import java.util.List;

public interface IFriendRequestController extends IDatabaseCheckController {
    /** sendFriendRequest - place friend request in the database
    * @param - username : the username of the person who is receiving the friend request
    */
    public void sendFriendRequest(String username) throws NetworkException;
    /** receiveFriendRequest - get a list of friend requests directed at current user
    * @return - a list of users that have requested a friend status with the current user
    */
    public List<User> receiveFriendRequest() throws NetworkException;
     /** getFriendList - get a list of accepted friends
    * @return - a list of users that are friends with the current user
    */
    public List<Friend> getFriends() throws NetworkException;
    /** acceptFriendRequest - accepts an existing friend request and updates database
    * @param - username : the username of the person who is receiving the friend request
    */
    public void acceptFriendRequest(String username) throws NetworkException;
    /** denyFriendRequest - denies an existing friend request and updates database
    * @param - username : the username of the person who is receiving the friend request
    */
    public void denyFriendRequest(String username) throws NetworkException;
}
