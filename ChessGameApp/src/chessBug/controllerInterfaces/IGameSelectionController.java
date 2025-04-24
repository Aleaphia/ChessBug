package chessBug.controllerInterfaces;

import chessBug.network.Match;
import chessBug.network.NetworkException;

public interface IGameSelectionController extends IDatabaseCheckController{
    /** getUsername - returns current user's username
    * @return - String username
    */
    public String getUsername();
    /** selectGame - open a game
    * @param - match : a match to start playing
    */
    public void selectGame(Match match);
    /** acceptMatchRequest - accepts a match 
    * @param - match : a match to accept and start playing
    */
    public void acceptMatchRequest(Match match) throws NetworkException;
    /** denyMatchRequest - denies an existing match request and updates database
    * @param - username : the match represented by the requested game
    */
    public void denyMatchRequest(Match match) throws NetworkException;
    /** forfitMatch - automatically loses a match, sets status such that the other player wins
    * @param - username : the match represented by the requested game
    */
    public void forfeitMatch(Match match) throws NetworkException;
}
