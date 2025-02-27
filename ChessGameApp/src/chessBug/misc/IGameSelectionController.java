/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package chessBug.misc;

import chessBug.network.Match;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.util.Duration;

public interface IGameSelectionController {
    public String getUsername();
    public List<Match> getOpenMatchList();
    public void selectGame(Match match);
    public List<Match> receiveMatchRequest();
    public void acceptMatchRequest(Match match);
    public void denyMatchRequest(Match match);
    public void forfitMatch(Match match);
    //TODO public void forfeitGame(Match match);

}
