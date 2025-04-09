/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package chessBug.controllerInterfaces;

import chessBug.network.Match;
import chessBug.network.NetworkException;

public interface IGameSelectionController extends IDatabaseCheckController{
    /** getUsername - returns current user's username
    * @return - String username
    */
    public String getUsername();
    /** getOpenMatchList - get a list of all open matches the current user is participating in
    * @return - a list of all open matches containing the current user
    */
    public void selectGame(Match match);
    /** receiveMatchRequest - get a list of all requested matches sent to the current player
    * @return - a list of all requested matches sent to the current player
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
