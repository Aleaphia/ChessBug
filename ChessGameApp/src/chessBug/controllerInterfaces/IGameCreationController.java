/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package chessBug.controllerInterfaces;

import chessBug.network.User;
import chessBug.network.Friend;
import java.util.List;

public interface IGameCreationController {
    public List<Friend> getFriendList();
    /** sendGameRequest - sends new game request to database
     * @param - playerColor : what color is the player requesting
     * @param - opponent: who are they playing against
     */
    public void sendGameRequest(Boolean playerColor, User opponent);
}
