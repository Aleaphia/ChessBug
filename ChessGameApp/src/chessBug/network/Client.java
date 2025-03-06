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

	private Map<Integer, User> userMap = new HashMap<>();

	private Client() {}

	public Client(String username, String password) throws ClientAuthException {
		// Call "login" function from the server
		profile = new ProfileModel(0, username, password, "", ProfileModel.DEFAULT_PROFILE_PICTURE);
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

		// Create a message to send to the server's "createAccount" function
		JSONObject accountData = new JSONObject();
		accountData.put("email", email); // Client automatically attaches username and password, we just need to also provide email
		JSONObject createMessage = c.post("createAccount", accountData);

		// If the server returns an error, throw an exception
		if(createMessage.getBoolean("error")) {
			throw new ClientAuthException(ClientAuthException.TYPE_CREATE_ACCOUNT, createMessage.opt("response").toString());
		}

		return c;
	}

	public User getOwnUser() {
		return getOrCreateUser(profile.getUserID(), profile.getUsername(), profile.getProfilePicURL());
	}

	public User getUserByID(int id) {
		if(userMap.containsKey(id))
			return userMap.get(id);
		else return new User(0, "unknown", User.DEFAULT_PROFILE_PICTURE);
	}

	private User getOrCreateUser(int id, String username, String pfp) {
		if(!userMap.containsKey(id))
			userMap.put(id, new User(id, username, pfp));
		return userMap.get(id);
	}

	private Friend getOrCreateFriend(int id, String username, String pfp, int chat) {
		if(!userMap.containsKey(id) || !(userMap.get(id) instanceof Friend))
			userMap.put(id, new Friend(id, username, pfp, chat));
		return (Friend)userMap.get(id);
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
			profile.setProfilePicURL("https://www.zandgall.com/chessbug/content/"+profileData.getJSONObject("response").getString("pfp"));
		profile.setUserID(profileData.getJSONObject("response").getInt("UserID"));

		return profile;
	}

	public ProfileModel getProfile() {
		return profile;
	}

	// Update profile with new data
	public void updateProfile(String newUsername, String newEmail, String newPassword) throws NetworkException {

		// Create message to send to server
		JSONObject profileData = new JSONObject();
		profileData.put("newUsername", newUsername);
		profileData.put("newPassword", newPassword);
		profileData.put("newEmail", newEmail);
		
		// Send to server to update profile data
		JSONObject response = post("updateProfile", profileData);
		if(response.getBoolean("error"))
			throw new NetworkException(response.opt("response").toString());

		profile.setUsername(newUsername);
		profile.setEmail(newEmail);
		profile.setPassword(newPassword);
	}

	public List<Match> getMatches() {
		ArrayList<Match> matches = new ArrayList<>();
		JSONObject matchesResponse = post("getMatches", new JSONObject());

		// Return none if error in response
		if(matchesResponse.getBoolean("error")) {
			System.err.println("Could not retrieve matches for \"" + profile.getUsername() + "\"");
			System.err.println(matchesResponse.opt("response"));
			return List.of();
		}

		JSONArray matchesReceived = matchesResponse.getJSONArray("response");
		for(int i = 0; i < matchesReceived.length(); i++) {
			JSONObject o = matchesReceived.getJSONObject(i);
			matches.add(new Match(o.getInt("MatchID"), o.getInt("Chat"), 
				getOrCreateUser(o.getInt("WhitePlayer"), o.getString("WhiteName"), o.isNull("WhitePfp") ? User.DEFAULT_PROFILE_PICTURE : o.getString("WhitePfp")), 
				getOrCreateUser(o.getInt("BlackPlayer"), o.getString("BlackName"), o.isNull("BlackPfp") ? User.DEFAULT_PROFILE_PICTURE : o.getString("BlackPfp")), o.getString("Status")));

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
			result.add(new Match(o.getInt("MatchID"), o.getInt("Chat"), 
				getOrCreateUser(o.getInt("WhitePlayer"), o.getString("WhiteName"), o.isNull("WhitePfp") ? User.DEFAULT_PROFILE_PICTURE : o.getString("WhitePfp")), 
				getOrCreateUser(o.getInt("BlackPlayer"), o.getString("BlackName"), o.isNull("BlackPfp") ? User.DEFAULT_PROFILE_PICTURE : o.getString("BlackPfp")), o.getString("Status")));
		}
		return result;
	}

	public List<Match> getClosedMatches() {
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
			result.add(new Match(o.getInt("MatchID"), o.getInt("Chat"), 
				getOrCreateUser(o.getInt("WhitePlayer"), o.getString("WhiteName"), o.isNull("WhitePfp") ? User.DEFAULT_PROFILE_PICTURE : o.getString("WhitePfp")), 
				getOrCreateUser(o.getInt("BlackPlayer"), o.getString("BlackName"), o.isNull("BlackPfp") ? User.DEFAULT_PROFILE_PICTURE : o.getString("BlackPfp")), o.getString("Status")));
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
			result.add(new Match(o.getInt("MatchID"), o.getInt("Chat"), 
				getOrCreateUser(o.getInt("WhitePlayer"), o.getString("WhiteName"), o.isNull("WhitePfp") ? User.DEFAULT_PROFILE_PICTURE : o.getString("WhitePfp")), 
				getOrCreateUser(o.getInt("BlackPlayer"), o.getString("BlackName"), o.isNull("BlackPfp") ? User.DEFAULT_PROFILE_PICTURE : o.getString("BlackPfp")), o.getString("Status")));
		}
		return result;
	}

	/* Returns true if successfully sent a match request 
	 * Prints error if received error
	 */
	public boolean sendMatchRequest(String username, boolean playingAsWhite) {
		JSONObject send = new JSONObject();
		send.put("target", username);
		send.put("request", (playingAsWhite)? Match.Status.WHITE_REQUESTED.toString() : Match.Status.BLACK_REQUESTED.toString());
		JSONObject received = post("sendMatchRequest", send);

		// Return none if error in response
		if(received.getBoolean("error")) {
			System.err.println("Could not send match request from \"" + profile.getUsername() + "\" to \"" + username + "\"");
			System.err.println(received.opt("response"));
			return false;
		}

		return received.getBoolean("response");
	}

	public boolean acceptMatchRequest(Match match) {
		JSONObject send = new JSONObject();
		send.put("match", match.getID());
		JSONObject received = post("acceptMatchRequest", send);

		// Return none if error in response
		if(received.getBoolean("error")) {
			System.err.println("Could not accept match request for #" + match.getID() + " to \"" + profile.getUsername() + "\"");
			System.err.println(received.opt("response"));
			return false;
		}

		return received.getBoolean("response");
	}

	public boolean denyMatchRequest(Match match) {
		JSONObject send = new JSONObject();
		send.put("match", match.getID());
		JSONObject received = post("denyMatchRequest", send);

		// Return none if error in response
		if(received.getBoolean("error")) {
			System.err.println("Could not accept match request for #" + match.getID() + " to \"" + profile.getUsername() + "\"");
			System.err.println(received.opt("response"));
			return false;
		}

		return received.getBoolean("response");
	}

	public boolean sendMatchRequest(User user, boolean playingAsWhite) { return sendMatchRequest(user.getUsername(), playingAsWhite); }

	public List<Friend> getFriends() {
		ArrayList<Friend> friends = new ArrayList<>();
		JSONObject friendsResponse = post("getFriends", new JSONObject());

		// Return none if error in response
		if(friendsResponse.getBoolean("error")) {
			System.err.println("Could not retrieve friends for \"" + profile.getUsername() + "\"");
			System.err.println(friendsResponse.opt("response"));
			return List.of();
		}

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
		JSONObject send = new JSONObject();
		send.put("target", username);
		JSONObject received = post("sendFriendRequest", send);

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
		JSONObject send = new JSONObject();
		send.put("target", username);
		JSONObject received = post("acceptFriendRequest", send);

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
		JSONObject send = new JSONObject();
		send.put("target", username);
		JSONObject received = post("denyFriendRequest", send);

		// Return none if error in response
		if(received.getBoolean("error")) {
			System.err.println("Could not deny friend request from \"" + username + "\" to \"" + profile.getUsername() + "\"");
			System.err.println(received.opt("response"));
			return false;
		}

		return received.getBoolean("response");
	}

	public boolean denyFriendRequest(User user) { return denyFriendRequest(user.getUsername()); }


	public List<User> getFriendRequests() {
		ArrayList<User> result = new ArrayList<>();
		JSONObject received = post("getFriendRequests", new JSONObject());

		// Return none if error in response
		if(received.getBoolean("error")) {
			System.err.println("Could not retrieve friend requests for \"" + profile.getUsername() + "\"");
			System.err.println(received.opt("response"));
			return List.of();
		}

		JSONArray response = received.getJSONArray("response");
		for(int i = 0; i < response.length(); i++) {
			JSONObject o = response.getJSONObject(i);
			result.add(getOrCreateUser(o.getInt("UserID"), o.getString("Name"), o.isNull("pfp") ? User.DEFAULT_PROFILE_PICTURE : o.getString("pfp")));
		}

		return result;
	}

	public void setMatchStatus(Match match, String status) {
		JSONObject sendData = new JSONObject();
		match.setStatus(status);
		sendData.put("match", match.getID());
		sendData.put("status", status);
		
		JSONObject response = post("setMatchStatus", sendData);
		if(response.getBoolean("error")) {
			System.err.println("Could not set match status!");
			System.err.println(response.opt("response").toString());
		}
	}

	public String syncMatchStatus(Match match) {
		JSONObject sendData = new JSONObject();
		sendData.put("match", match.getID());

		JSONObject response = post("getMatchStatus", sendData);
		if(response.getBoolean("error")) {
			System.err.println("Could not get match status!");
			System.err.println(response.opt("response").toString());
			return null;
		}

		match.setStatus(response.getString("response"));
		return response.getString("response");
	}

	/*public String getUserProfilePictureURL(String username) {
		JSONObject sendData = new JSONObject();
		sendData.put("target", username);

		JSONObject received = post("getUserProfilePictureID", sendData);
		if(received.getBoolean("error")) {
			System.err.println("Could not get user profile picture: " + username);
			System.err.println(received.opt("response").toString());
			return ProfileModel.DEFAULT_PROFILE_PICTURE;
		}

		if(received.isNull("response"))
			return ProfileModel.DEFAULT_PROFILE_PICTURE;	

		return "https://www.zandgall.com/chessbug/content/" + received.getString("response");
	}*/

	/*public String getUserProfilePictureURL(User u) { return getUserProfilePictureURL(u.getUsername()); }*/

	public void uploadProfilePicture(File file) {
		JSONObject send = new JSONObject();
		try {
			send.put("image", Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath())));

			JSONObject received = post("uploadProfilePicture", send);
			if(received.getBoolean("error")) {
				System.err.println("Could not upload profile picture! " + file.toPath());
				System.err.println(received.opt("response").toString());
				return;
			}

			profile.setProfilePicURL("https://www.zandgall.com/chessbug/content/" + received.getString("response"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public JSONObject post(String function, JSONObject message) {
		// Sent JSON Object to server and retrieve response
		message.put("username", profile.getUsername());
		message.put("password", profile.getPassword());
		return post(function, message.toString());
	}

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
