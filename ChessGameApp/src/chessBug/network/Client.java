/*
 * A Class used for calling ChessBug server side Web API functions
 * There is a method for every server side function supported
 * Client is initialized with user login details, in order to authorize all web interactions
 */

package chessBug.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;
import java.util.Map;
import java.util.HashMap;

import java.nio.file.Files;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;

import chessBug.profile.ProfileModel;

import org.json.JSONArray;
import org.json.JSONException;

public class Client {
	// Store user information in order to log in
	private ProfileModel profile;

	// Used to cache all users  TODO: partially clear cache every once in a while
	private Map<Integer, User> userMap = new HashMap<>();

	private Client() {}

	public Client(String username, String password) throws ClientAuthException {
		// Call "login" function from the server
		profile = new ProfileModel(0, username, password, "", User.DEFAULT_PROFILE_PICTURE);
		JSONObject loginMessage = post("login", new JSONObject());

		// If the server returns an error, throw an exception
		if(loginMessage.getBoolean("error")) {
			throw new ClientAuthException(ClientAuthException.TYPE_LOGIN, loginMessage.opt("response").toString());
		}

		// Update profile data with email and any other data
		try {
			syncProfile();
		} catch(NetworkException e) {
			throw new ClientAuthException(ClientAuthException.TYPE_LOGIN, e.getMessage());
		}
	}

	public static Client createAccount(String username, String password, String email) throws ClientAuthException {
		// Create a new blank Client, setting profile data
		Client c = new Client();
		c.profile = new ProfileModel(0, username, password, email, "");

		// Create a message to send to the server's "createAccount" function, 'c' holds password and username, need to also provide email
		JSONObject createMessage = c.post("createAccount", Map.of("email", email));

		// If the server returns an error, throw an exception
		if(createMessage.getBoolean("error"))
			throw new ClientAuthException(ClientAuthException.TYPE_CREATE_ACCOUNT, createMessage.opt("response").toString());

		return c;
	}

	// Retrieve a user from cache if the user has been cached, otherwise create default unknown user
	public User getUserByID(int id) {
		if(userMap.containsKey(id))
			return userMap.get(id);
		else return new User(0, "unknown", User.DEFAULT_PROFILE_PICTURE);
	}

	// Retrieve a user from cache if exists, if not, creates one and puts it in cache
	// TODO: Cache checking can be handled here, checking if the cached user has the right username and profile picture
	private User getOrCreateUser(int id, String username, String pfp) {
		if(!userMap.containsKey(id))
			userMap.put(id, new User(id, username, pfp));
		else {
			userMap.get(id).setUsername(username);
			userMap.get(id).setProfilePicture(pfp);
		}
		return userMap.get(id);
	}

	// Retrieve a friend from cache if exists and is a friend, if not, creates one and puts it in cache
	// TODO: Create Friend(User, chat) constructor, also do cache handling
	private Friend getOrCreateFriend(int id, String username, String pfp, int chat) {
		if(!userMap.containsKey(id) || !(userMap.get(id) instanceof Friend))
			userMap.put(id, new Friend(id, username, pfp, chat));
		return (Friend)userMap.get(id);
	}

	// Retrieve user object for current user
	public User getOwnUser() {
		return getOrCreateUser(profile.getUserID(), profile.getUsername(), profile.getProfilePicURL());
	}
	
	// Update profile with server data
	public ProfileModel syncProfile() throws NetworkException {
		JSONObject profileData = post("getProfileData", new JSONObject());
		
		// If couldn't retrieve profile data...
		if(profileData.getBoolean("error"))
			throw new NetworkException(profileData.opt("response").toString());

		// We need to send correct username and password to retrieve information so no need to update those variables
		profile.setEmail(profileData.getJSONObject("response").getString("EmailAddress"));
		if(!profileData.getJSONObject("response").isNull("pfp"))
			profile.setProfilePicURL(profileData.getJSONObject("response").getString("pfp"));
		profile.setUserID(profileData.getJSONObject("response").getInt("UserID"));

		return profile;
	}

	public ProfileModel getProfile() {
		return profile;
	}

	// Update profile with new data
	public void updateProfile(String newUsername, String newEmail, String newPassword) throws NetworkException {
		// Send to server to update profile data
		JSONObject response = post("updateProfile", Map.of("newUsername", newUsername, "newPassword", newPassword, "newEmail", newEmail));
		if(response.getBoolean("error"))
			throw new NetworkException(response.opt("response").toString());

		profile.setUsername(newUsername);
		profile.setEmail(newEmail);
		profile.setPassword(newPassword);
	}

	public void updatePassword(String newPassword) throws NetworkException {
		JSONObject received = post("updatePassword", Map.of("newPassword", newPassword));
		if(received.getBoolean("error"))
			throw new NetworkException(received.opt("response").toString());

		profile.setPassword(newPassword);
	}

	// Retrieve a list of all matches the user is a part of
	public List<Match> getMatches() {
		// Call the server function
		ArrayList<Match> matches = new ArrayList<>();
		JSONObject matchesResponse = post("getMatches", new JSONObject());

		// Return none if error in response
		if(matchesResponse.getBoolean("error")) {
			System.err.println("Could not retrieve matches for \"" + profile.getUsername() + "\"");
			System.err.println(matchesResponse.opt("response"));
			return List.of();
		}

		// Loop through all entries of received matches array, and make new Matches out of JSON representations
		JSONArray matchesReceived = matchesResponse.getJSONArray("response");
		for(int i = 0; i < matchesReceived.length(); i++) {
			JSONObject o = matchesReceived.getJSONObject(i);
			// Retrieve player users from data
			User whitePlayer = getOrCreateUser(o.getInt("WhitePlayer"), o.getString("WhiteName"), o.isNull("WhitePfp") ? User.DEFAULT_PROFILE_PICTURE : o.getString("WhitePfp"));
			User blackPlayer = getOrCreateUser(o.getInt("BlackPlayer"), o.getString("BlackName"), o.isNull("BlackPfp") ? User.DEFAULT_PROFILE_PICTURE : o.getString("BlackPfp"));
			matches.add(new Match(o.getInt("MatchID"), o.getInt("Chat"), whitePlayer, blackPlayer, o.getString("Status")));
		}
		return matches;
	}

	public List<Match> getOpenMatches() {
		ArrayList<Match> result = new ArrayList<>();
		JSONObject received = post("getOpenMatches", new JSONObject());

		// Return none if error in response
		if(received.getBoolean("error")) {
			System.err.println("Could not retrieve open matches for \"" + profile.getUsername() + "\"");
			System.err.println(received.opt("response"));
			return List.of();
		}
                
		JSONArray response = received.getJSONArray("response");
		for(int i = 0; i < response.length(); i++) {
			JSONObject o = response.getJSONObject(i);
			// Retrieve player users from data
			User whitePlayer = getOrCreateUser(o.getInt("WhitePlayer"), o.getString("WhiteName"), o.isNull("WhitePfp") ? User.DEFAULT_PROFILE_PICTURE : o.getString("WhitePfp"));
			User blackPlayer = getOrCreateUser(o.getInt("BlackPlayer"), o.getString("BlackName"), o.isNull("BlackPfp") ? User.DEFAULT_PROFILE_PICTURE : o.getString("BlackPfp"));
			result.add(new Match(o.getInt("MatchID"), o.getInt("Chat"), whitePlayer, blackPlayer, o.getString("Status")));
		}
		return result;
	}
        
        public List<Match> getClosedMatches() {
		ArrayList<Match> result = new ArrayList<>();
		JSONObject received = post("getClosedMatches", new JSONObject());

		// Return none if error in response
		if(received.getBoolean("error")) {
			System.err.println("Could not retrieve closed matches for \"" + profile.getUsername() + "\"");
			System.err.println(received.opt("response"));
			return List.of();
		}

		JSONArray response = received.getJSONArray("response");
		for(int i = 0; i < response.length(); i++) {
			JSONObject o = response.getJSONObject(i);
			// Retrieve player users from data
			User whitePlayer = getOrCreateUser(o.getInt("WhitePlayer"), o.getString("WhiteName"), o.isNull("WhitePfp") ? User.DEFAULT_PROFILE_PICTURE : o.getString("WhitePfp"));
			User blackPlayer = getOrCreateUser(o.getInt("BlackPlayer"), o.getString("BlackName"), o.isNull("BlackPfp") ? User.DEFAULT_PROFILE_PICTURE : o.getString("BlackPfp"));
			result.add(new Match(o.getInt("MatchID"), o.getInt("Chat"), whitePlayer, blackPlayer, o.getString("Status")));
		}
		return result;
	}

	public List<Match> getMatchRequests() {
		ArrayList<Match> result = new ArrayList<>();
		JSONObject received = post("getMatchRequests", new JSONObject());

		// Return none if error in response
		if(received.getBoolean("error")) {
			System.err.println("Could not retrieve match requests for \"" + profile.getUsername() + "\"");
			System.err.println(received.opt("response"));
			return List.of();
		}

		JSONArray response = received.getJSONArray("response");
		for(int i = 0; i < response.length(); i++) {
			JSONObject o = response.getJSONObject(i);
			User whitePlayer = getOrCreateUser(o.getInt("WhitePlayer"), o.getString("WhiteName"), o.isNull("WhitePfp") ? User.DEFAULT_PROFILE_PICTURE : o.getString("WhitePfp"));
			User blackPlayer = getOrCreateUser(o.getInt("BlackPlayer"), o.getString("BlackName"), o.isNull("BlackPfp") ? User.DEFAULT_PROFILE_PICTURE : o.getString("BlackPfp"));
			result.add(new Match(o.getInt("MatchID"), o.getInt("Chat"), whitePlayer, blackPlayer, o.getString("Status")));
		}
		return result;
	}

	/* Returns true if successfully sent a match request 
	 * Prints error if received error
	 */
	public boolean sendMatchRequest(String username, boolean playingAsWhite) {
		// Send request to server
		JSONObject received = post("sendMatchRequest", Map.of("target", username, "request", playingAsWhite ? Match.Status.WHITE_REQUESTED.toString()
			: Match.Status.BLACK_REQUESTED.toString()));

		// Return none if error in response
		if(received.getBoolean("error")) {
			System.err.println("Could not send match request from \"" + profile.getUsername() + "\" to \"" + username + "\"");
			System.err.println(received.opt("response"));
			return false;
		}

		// Return response (true if successful)
		return received.getBoolean("response");
	}
	
	// Send match request based on user
	public boolean sendMatchRequest(User user, boolean playingAsWhite) { return sendMatchRequest(user.getUsername(), playingAsWhite); }

	// Deny match request from given match
	public boolean denyMatchRequest(Match match) {
		JSONObject received = post("denyMatchRequest", Map.of("match", match.getID()));

		// Return none if error in response
		if(received.getBoolean("error")) {
			System.err.println("Could not accept match request for #" + match.getID() + " to \"" + profile.getUsername() + "\"");
			System.err.println(received.opt("response"));
			return false;
		}

		return received.getBoolean("response");
	}

	// Retrieve all of user's friends
	public List<Friend> getFriends() {
		ArrayList<Friend> friends = new ArrayList<>();
		JSONObject friendsResponse = post("getFriends", new JSONObject());

		// Return none if error in response
		if(friendsResponse.getBoolean("error")) {
			System.err.println("Could not retrieve friends for \"" + profile.getUsername() + "\"");
			System.err.println(friendsResponse.opt("response"));
			return List.of();
		}

		// Go through received array of friends, retrieve friend for every JSON representation
		JSONArray friendsReceived = friendsResponse.getJSONArray("response");
		for(int i = 0; i < friendsReceived.length(); i++) {
			JSONObject o = friendsReceived.getJSONObject(i);
			friends.add(getOrCreateFriend(o.getInt("UserID"), o.getString("Name"), o.isNull("pfp") ? User.DEFAULT_PROFILE_PICTURE : o.getString("pfp"), o.getInt("Chat")));
		}

		return friends;
	}

	/* Returns true if successfully sent a friend request 
	 * Prints error if received error
	 * Prints nothing and returns false on no friend request for "normal" reasons (i.e. already friended or sent/received friend request)
	 * TODO: Print more information regarding false returns
	 */
	public boolean sendFriendRequest(String username) {
		JSONObject received = post("sendFriendRequest", Map.of("target", username));

		// Return none if error in response
		if(received.getBoolean("error")) {
			System.err.println("Could not send friend request from \"" + profile.getUsername() + "\" to \"" + username + "\"");
			System.err.println(received.opt("response"));
			return false;
		}

		return received.getBoolean("response");
	}

	public boolean sendFriendRequest(User user) { return sendFriendRequest(user.getUsername()); }

	/* Returns true if successfully accepted a friend request 
	 * Prints error if received error
	 * Prints nothing and returns false on no friend request accepted for "normal" reasons (i.e. friend request doesn't exist or already friends with target)
	 * TODO: Print more information regarding false returns
	 */
	public boolean acceptFriendRequest(String username) {
		JSONObject received = post("acceptFriendRequest", Map.of("target", username));

		// Return none if error in response
		if(received.getBoolean("error")) {
			System.err.println("Could not accept friend request from \"" + username + "\" to \"" + profile.getUsername() + "\"");
			System.err.println(received.opt("response"));
			return false;
		}

		return received.getBoolean("response");
	}

	public boolean acceptFriendRequest(User user) { return acceptFriendRequest(user.getUsername()); }

	/* Returns true if successfully denied a friend request 
	 * Prints error if received error
	 * Prints nothing and returns false on no friend request denied for "normal" reasons (i.e. friend request doesn't exist or is friends with target)
	 * TODO: Print more information regarding false returns
	 */
	public boolean denyFriendRequest(String username) {
		JSONObject received = post("denyFriendRequest", Map.of("target", username));

		// Return none if error in response
		if(received.getBoolean("error")) {
			System.err.println("Could not deny friend request from \"" + username + "\" to \"" + profile.getUsername() + "\"");
			System.err.println(received.opt("response"));
			return false;
		}

		return received.getBoolean("response");
	}

	public boolean denyFriendRequest(User user) { return denyFriendRequest(user.getUsername()); }

	// Retrieve all friend requests
	public List<User> getFriendRequests() {
		ArrayList<User> result = new ArrayList<>();
		JSONObject received = post("getFriendRequests", new JSONObject());

		// Return none if error in response
		if(received.getBoolean("error")) {
			System.err.println("Could not retrieve friend requests for \"" + profile.getUsername() + "\"");
			System.err.println(received.opt("response"));
			return List.of();
		}

		// Loop through received array and create new user for every JSON representation
		JSONArray response = received.getJSONArray("response");
		for(int i = 0; i < response.length(); i++) {
			JSONObject o = response.getJSONObject(i);
			result.add(getOrCreateUser(o.getInt("UserID"), o.getString("Name"), o.isNull("pfp") ? User.DEFAULT_PROFILE_PICTURE : o.getString("pfp")));
		}

		return result;
	}

	/* Update Match Status based on function name */
	public void acceptMatchRequest(Match match){
            setMatchStatus(match, Match.Status.WHITE_TURN.toString());
        }
        public void forfitMatch(Match match){
            setMatchStatus(match, getOwnUser().getUsername().equals(match.getWhite().getUsername())?
                Match.Status.BLACK_WIN.toString() :     //If the user who forfit is white -> black wins
                Match.Status.WHITE_WIN.toString()       //Otherwise white wins
            );
        }
        public void setGameTurn(Match match, Boolean turn){
            setMatchStatus(match,turn ? //set game status
                Match.Status.WHITE_TURN.toString() :
                Match.Status.BLACK_TURN.toString()
            );
        }
        public void setGameWinner(Match match, Boolean winner){
            setMatchStatus(match,winner ? //set game status
                Match.Status.WHITE_WIN.toString() :
                Match.Status.BLACK_WIN.toString()
            );
        }
        public void setGameDraw(Match match){
            setMatchStatus(match, Match.Status.DRAW.toString());
        }
	/* ~~~~ */
        
	// Set match status on server
        private void setMatchStatus(Match match, String status) {
		JSONObject response = post("setMatchStatus", Map.of("match", match.getID(), "status", status));
		if(response.getBoolean("error")) {
			System.err.println("Could not set match status!");
			System.err.println(response.opt("response").toString());
		}
		match.setStatus(status);
	}

	// Retrieve new match status from server
	public String syncMatchStatus(Match match) {
		JSONObject response = post("getMatchStatus", Map.of("match", match.getID()));
		if(response.getBoolean("error")) {
			System.err.println("Could not get match status!");
			System.err.println(response.opt("response").toString());
			return null;
		}

		match.setStatus(response.getString("response"));
		return response.getString("response");
	}

	// Upload profile picture to server, using a Base64 representation
	public void uploadProfilePicture(File file) {
		try {
			JSONObject send = new JSONObject();
			send.put("image", Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath())));

			JSONObject received = post("uploadProfilePicture", send);
			if(received.getBoolean("error")) {
				System.err.println("Could not upload profile picture! " + file.toPath());
				System.err.println(received.opt("response").toString());
				return;
			}

			profile.setProfilePicURL(received.getString("response"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Send a message to the server based on a Map representation of JSON data, (appends login details) and return the result
	public JSONObject post(String function, Map<?, ?> message) {
		return post(function, new JSONObject(message));
	}

	// Send a message to the server based on a JSON Object, appending User login details, and return the result
	public JSONObject post(String function, JSONObject message) {
		// Sent JSON Object to server and retrieve response
		message.put("username", profile.getUsername());
		message.put("password", profile.getPassword());
		return post(function, message.toString());
	}

	// Send a string to a given function in the web api, return a JSON result
	public JSONObject post(String function, String message) {
		// Send arbitrary string as 
		byte[] data = message.getBytes(StandardCharsets.UTF_8);

		// Set up a connection to the server
		HttpsURLConnection con = getConnection(function);
		if(con == null) {
			System.err.println("Could not connect to server!");
			JSONObject out = new JSONObject();
			out.put("response", "Error: Could not connect to server!");
			out.put("error", true);
			return out;
		}

		// Tell how much data we're sending, connect, and send it
		con.setFixedLengthStreamingMode(data.length);
		try {
			con.connect();
			OutputStream os = con.getOutputStream();
			os.write(data);
		} catch (IOException ioex) {
			System.err.println("Could not write data to server!");
			ioex.printStackTrace();
			JSONObject out = new JSONObject();
			out.put("response", "Error: Could not write data to server!");
			out.put("error", true);
			return out;
		}

		// Read response into `builder`
		String input = "";
		try {
			BufferedReader b = new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuilder builder = new StringBuilder();
			while( (input = b.readLine()) != null) {
				builder.append(input);
			}
			input = builder.toString();
		} catch (IOException ioex2) {
			System.err.println("Could not read server response!");
			JSONObject out = new JSONObject();
			out.put("response", "Unable to reach or communicate with the server!");
			out.put("error", true);	
			ioex2.printStackTrace();
			return out;
		}

		// Try to parse as straight JSON Object,
		try {
			return new JSONObject(input);
		} catch (JSONException e) {
			// If it's not a JSON Object, it probably contains error data
			JSONObject jo = new JSONObject();
			jo.put("error", true);
			jo.put("response", input);
			return jo;
		}
	}

	// Create a HTTPS connection based on a web API function, filling out the required headers
	private static HttpsURLConnection getConnection(String function) {
		try {
			HttpsURLConnection con = (HttpsURLConnection)new URI("https://www.zandgall.com/chessbug/" + function + ".php").toURL().openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			return con;
		} catch (URISyntaxException e1) {
			System.err.println("Could not make connection to function \"" + function + "\": Invalid URI Syntax!");
			e1.printStackTrace();
			return null;
		} catch (MalformedURLException e2) {
			System.err.println("Could not make connection to function \"" + function + "\": Malformed URL!");
			e2.printStackTrace();
			return null;
		} catch (IOException e3) {
			System.err.println("Could not make connection to function \"" + function + "\"!");
			e3.printStackTrace();
			return null;
		}
	}
}
