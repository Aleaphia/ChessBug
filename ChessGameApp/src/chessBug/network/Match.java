package chessBug.network;

import java.util.ArrayList;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;

public class Match {
	// Although not very distinct, gives definitive list of possible results
	public static final String WHITE_WIN = "WhiteWin";
	public static final String BLACK_WIN = "WhiteWin";
	public static final String DRAW = "Draw";
	public static final String IN_PROGRESS = null;

	private int matchID;
	private Chat chat;

	private User white, black;

	private int movesNumber = 0;

	private ArrayList<String> moves;

	public Match(int matchID, int chatID, User white, User black) {
		this.matchID = matchID;
		moves = new ArrayList<>();
		this.chat = new Chat(chatID);
		this.white = white;
		this.black = black;
	}

	public Stream<String> poll(Client client) {
		// Ask the server how many moves exist in the current match
		JSONObject numMovesPoll = new JSONObject();
		numMovesPoll.put("match", matchID);
		JSONObject numMovesResponse = client.post("getMatchMoveCount", numMovesPoll);
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

		// Ask for all new moves
		JSONObject getMovesPoll = new JSONObject();
		getMovesPoll.put("match", matchID);
		getMovesPoll.put("num", currentNumber - movesNumber); // getting current number of moves minus the previous count

		JSONObject getMovesResponse = client.post("getNMatchMoves", getMovesPoll);

		if(getMovesResponse.getBoolean("error")) {
			System.err.println("Could not retrieve " + (currentNumber - movesNumber) + " moves from chess match: " + matchID);
			System.err.println(getMovesResponse.opt("response"));
			return Stream.empty();
		}

		// Poll through new moves
		JSONArray retrievedMoves = getMovesResponse.getJSONArray("response");
		for(int i = 0; i < currentNumber - movesNumber; i++) {
			JSONObject o = retrievedMoves.getJSONObject(i);
			moves.add(o.getString("Move"));
			// Ensure movenum is equivalent to index in moves
			if(moves.size() != o.getInt("MoveNum"))
				System.err.println("Moves may have been polled out of order!");
		}

		// Return a stream of all new moves
		Stream<String> out = moves.stream().skip(movesNumber);
		movesNumber = currentNumber;
		return out;
	}

	public void setResult(Client client, String result) {
		JSONObject sendData = new JSONObject();
		sendData.put("match", matchID);
		sendData.put("result", result);
		
		JSONObject response = client.post("setMatchResult", sendData);
		if(response.getBoolean("error")) {
			System.err.println("Could not set match result!");
			System.err.println(response.opt("response").toString());
		}
	}

	public String getResult(Client client) {
		JSONObject sendData = new JSONObject();
		sendData.put("match", matchID);

		JSONObject response = client.post("getMatchResult", sendData);
		if(response.getBoolean("error")) {
			System.err.println("Could not get match result!");
			System.err.println(response.opt("response").toString());
			return IN_PROGRESS;
		}

		if(response.opt("response")==null)
			return null;

		return response.getString("response");
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

	public void makeMove(Client from, String move) {
                movesNumber++; //Iterate move count
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
