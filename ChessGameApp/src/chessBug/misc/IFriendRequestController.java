package chessBug.misc;

import chessBug.network.User;
import java.util.List;

public interface IFriendRequestController {
    public boolean sendFriendRequest(String username);
    public List<User> receiveFriendRequest();
    public void acceptFriendRequest(String username);
}
