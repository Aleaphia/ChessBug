/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package chessBug.misc;

import chessBug.network.Match;
import java.util.List;

public interface IGameSelectionController {
    public String getUsername();
    public List<Match> getOpenMatchList();
    public void selectGame(Match match);
    public List<Match> receiveMatchRequest();
    public void acceptMatchRequest(Match match);
    public void denyMatchRequest(Match match);
    //TODO public void forfeitGame(Match match);
}
