/**
 * Store Match details, including a list of all moves
 * Moves need to be polled with poll(Client) in order to actually retrieve new moves from the database, including after the match has been created
 */

package chessBug.network;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;

public class Match {
	public static final Match NO_MATCH = new Match(0, Chat.NO_CHAT, User.NO_USER, User.NO_USER, Status.DRAW.toString());

	// Represent the status of the match
	public enum Status {
		WHITE_WIN("WhiteWin"), BLACK_WIN("BlackWin"), DRAW("Draw"),
		WHITE_REQUESTED("WhiteRequested"),BLACK_REQUESTED("BlackRequested"),
		WHITE_TURN("WhiteTurn"),BLACK_TURN("BlackTurn");

		private final String stringName;

		Status(String stringName){this.stringName = stringName;}
		@Override public String toString(){return stringName;}
	}

	private int matchID;

	private Chat chat;

	private User white, black;

	private String status;

	private int movesNumber = 0;
	private ArrayList<String> moves;

	public Match(int matchID, Chat chat, User white, User black, String status) {
		this.matchID = matchID;
		moves = new ArrayList<>();
		this.chat = chat;
		this.white = white;
		this.black = black;
		this.status = status;
	}

	//Load new moves from the server
	public Stream<String> poll(Client client) {
		// Ask the server how many moves exist in the current match
		JSONObject numMovesResponse = client.post("getMatchMoveCount", Map.of("match", matchID));
		if(numMovesResponse.getBoolean("error")) {
			System.err.println("Could not retrieve match move count! Match: " + matchID);
			System.err.println(numMovesResponse.opt("response"));
			return Stream.empty();
		}

		// If the number of moves if more than the amount we've already counted, grab all new moves
		int currentNumber;
		currentNumber = numMovesResponse.getInt("response");

		// No new moves
		if(currentNumber <= movesNumber)
			return Stream.empty();

		// Ask for all new moves (current number of match moves minus number already retrieved)
		JSONObject getMovesResponse = client.post("getNMatchMoves", Map.of("match", matchID, "num", currentNumber - movesNumber));

		if(getMovesResponse.getBoolean("error")) {
			System.err.println("Could not retrieve " + (currentNumber - movesNumber) + " moves from chess match: " + matchID);
			System.err.println(getMovesResponse.opt("response"));
			return Stream.empty();
		}

		// Poll through new moves
		JSONArray retrievedMoves = getMovesResponse.getJSONArray("response");
		for(int i = 0; i < currentNumber - movesNumber; i++) {
			JSONObject o = retrievedMoves.getJSONObject(i);
			// Ensure movenum is equivalent to index in moves
			if(moves.size() != o.getInt("MoveNum"))
				System.err.println("Moves may have been polled out of order! " + moves.size() + " " + o.getInt("MoveNum"));
			moves.add(o.getString("Move"));
		}

		// Return a stream of all new moves
		Stream<String> out = moves.stream().skip(movesNumber);
		movesNumber = currentNumber;
		return out;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public ArrayList<String> getAllMoves() {
		return moves;
	}

	public Chat getChat() {
		return chat;
	}

	public int getID() {
		return matchID;
	}

	public User getWhite() {
		return white;
	}

	public User getBlack() {
		return black;
	}
        
        public int getMoveNumber(){
            return movesNumber;
        }

	// Make a move, update client side moves and moveNumber while also updating the server
	public void makeMove(Client from, String move) {
                movesNumber++; //Iterate move count
                moves.add(move); //Add new move to the list
		JSONObject message = new JSONObject();
		message.put("match", matchID);
		message.put("move", move);
		from.post("makeMove", message);
	}
        
        @Override
        public String toString(){
            return white.getUsername() + " vs. " + black.getUsername();
        }
}
