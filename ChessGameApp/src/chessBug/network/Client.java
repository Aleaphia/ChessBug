package chessBug.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;

import chessBug.profile.ProfileModel;

import org.json.JSONArray;
import org.json.JSONException;

public class Client {
	// Store user information in order to log in
	private ProfileModel profile;

	private Client() {

	}

	public Client(String username, String password) throws ClientAuthException {
		// Call "login" function from the server
		profile = new ProfileModel(username, password, "", "");
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
		c.profile.setUsername(username);
		c.profile.setEmail(email);
		c.profile.setPassword(password);

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

	// Update profile with server data
	public ProfileModel syncProfile() throws NetworkException {
		JSONObject profileData = post("getAccountData", new JSONObject());
		
		// If couldn't retrieve profile data...
		if(profileData.getBoolean("error"))
			throw new NetworkException(profileData.opt("response").toString());

		profile.setEmail(profileData.getJSONObject("response").getString("Email"));

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
			friends.add(new Friend(o.getInt("UserID"), o.getString("Name"), o.getInt("Chat")));
		}

		return friends;
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
			out.put("response", "Error: Could not read server response!");
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
