/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package chessBug.controllerInterfaces;

import chessBug.network.Match;
import java.util.List;

public interface IGameSelectionController extends IDatabaseCheckController{
    /** getUsername - returns current user's username
    * @return - String username
    */
    public String getUsername();
    /** getOpenMatchList - get a list of all open matches the current user is participating in
    * @return - a list of all open matches containing the current user
    */
    public List<Match> getOpenMatchList();
    /** selectGame - opens the selected game
    * @param - match : the selected game match
    */
    public void selectGame(Match match);
    /** receiveMatchRequest - get a list of all requested matches sent to the current player
    * @return - a list of all requested matches sent to the current player
    */
    public List<Match> receiveMatchRequest();
    /** acceptMatchRequest - accepts an existing match request and updates database
    * @param - match : the match represented by the requested game
    */
    public void acceptMatchRequest(Match match);
    /** denyMatchRequest - denies an existing match request and updates database
    * @param - username : the match represented by the requested game
    */
    public void denyMatchRequest(Match match);
    /** forfitMatch - automatically loses a match, sets status such that the other player wins
    * @param - username : the match represented by the requested game
    */
    public void forfitMatch(Match match);
}
