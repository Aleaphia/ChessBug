/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package chessBug.misc;

import chessBug.network.Match;
import java.util.List;

public interface IGameSelectionController {
    public List<Match> getOpenMatchList();
    public void selectGame(Match match);
    public List<Match> receiveMatchRequest();
    public void acceptMatchRequest(Match match);

}
