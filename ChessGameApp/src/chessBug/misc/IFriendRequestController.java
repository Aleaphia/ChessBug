package chessBug.misc;

import chessBug.network.User;
import java.util.List;

public interface IFriendRequestController extends IDatabaseCheckInterface {
    public boolean sendFriendRequest(String username);
    public List<User> receiveFriendRequest();
    public void acceptFriendRequest(String username);
    public void denyFriendRequest(String username);
}
