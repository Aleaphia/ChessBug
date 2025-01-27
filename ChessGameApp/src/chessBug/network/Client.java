package chessBug.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class Client {
	// Store user information in order to log in
	private String username, password;

	private Client() {

	}

	public Client(String username, String password) throws ClientAuthException {
		this.username = username;
		this.password = password;
		JSONObject loginMessage = post("login", new JSONObject());
		if(loginMessage.getBoolean("error")) {
			throw new ClientAuthException(ClientAuthException.TYPE_LOGIN, loginMessage.opt("response").toString());
		}
	}

	public static Client createAccount(String username, String password, String email) throws ClientAuthException {
		Client c = new Client();
		c.username = username;
		c.password = password;
		JSONObject accountData = new JSONObject();
		accountData.put("email", email);
		JSONObject createMessage = c.post("createAccount", accountData);

		if(createMessage.getBoolean("error")) {
			throw new ClientAuthException(ClientAuthException.TYPE_CREATE_ACCOUNT, createMessage.opt("response").toString());
		}

		return c;
	}

	public List<Friend> getFriends() {
		ArrayList<Friend> friends = new ArrayList<>();
		JSONObject friendsResponse = post("getFriends", new JSONObject());

		// Return none if error in response
		if(friendsResponse.getBoolean("error")) {
			System.err.println("Could not retrieve friends for \"" + username + "\"");
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
		message.put("username", username);
		message.put("password", password);
		return post(function, message.toString());
	}

	public JSONObject post(String function, String message) {
		// Send arbitrary string as 
		byte[] data = message.getBytes(StandardCharsets.UTF_8);

		// Set up a connection to the server
		HttpURLConnection con = getConnection(function);
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
		
		// TODO: Standardize output on server to always return a JSON Object

		// Try to parse as straight JSON Object,
		try {
			return new JSONObject(input);
		} catch (JSONException e) {
			JSONObject jo = new JSONObject();
			jo.put("error", true);
			// If it's not a JSON Object, it probably contains error data
			jo.put("response", input);
			return jo;
		}
	}

	private static HttpURLConnection getConnection(String function) {
		try {
			HttpURLConnection con = (HttpURLConnection)new URI("http://localhost/chessbug-server/" + function + ".php").toURL().openConnection();
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
