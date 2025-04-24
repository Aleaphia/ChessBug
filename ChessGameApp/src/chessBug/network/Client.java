/*
 * A Class used for calling ChessBug server side Web API functions
 * There is a method for every server side function supported
 * Client is initialized with user login details, in order to authorize all web interactions
 */

package chessBug.network;

import chessBug.profile.ProfileModel;

import java.io.IOException;
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
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.KeyGenerator;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;

import org.json.JSONObject;
import org.json.JSONException;

public class Client {
	// "Salt" used to hash passwords
	private static final byte[] SALT = "chessbug!(%*¡ºªħéñ€óßáñåçœø’ħ‘ºº".getBytes(StandardCharsets.UTF_8);

	// Public key used to make handshake with server
	private static final byte[] PUBLIC_KEY = Base64.getDecoder().decode("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqAwpV8rPgnN1yaWTBpPqctTIXCJCu80pbA0jUtug7WVTq0hrKIjZD7VkwSPWBAu/SQaT31Rrcfo2X4wdQiPT4mnncyE5gHgpTZFOLtiTMOEjJmtcF6JW7nzp7c1//NKagP/1gdom3Xrnyr91qsiyMWIij69proLcv1gnQV0pPFifjBNBqMC3czG6bbyhYAWZMgWNODwegYd6DrZ1qqRvVgb5F2qdpdySNVpqERMIXhT0AJnL6IbjA1kB4lq6m6fnB6lU8hSatguH0mRP5HSgg/fhNnu26ajJkxr9BrikJyEk9fOhjp2besWlMhuq8eO4VIRAa2KLwWTTg1NM3i6+3QIDAQAB");

	private static Cipher CLIENT_ENCRYPT = null, CLIENT_DECRYPT = null;	
	private static String ENCRYPTED_KEY = null;

	// Store user information in order to log in
	private ProfileModel profile;

	private static boolean LOCK = false;

	// Used to cache users, matches, and chats
	private Map<Integer, User> userMap = new HashMap<>();
	private Map<Integer, Match> matchMap = new HashMap<>();
	private Map<Integer, Chat> chatMap = new HashMap<>();

	private Client() {}

	public Client(String username, String password) throws ClientAuthException {
		if(username.isEmpty() || password.isEmpty())
			throw new ClientAuthException(ClientAuthException.TYPE_LOGIN, new IllegalArgumentException("Username and password can't be empty!"));
		// Call "login" function from the server
		profile = new ProfileModel(0, username, hashPassword(password), "", User.DEFAULT_PROFILE_PICTURE);

		// Update profile data with email and any other data
		try {
			post("login", new JSONObject());
			syncProfile();
		} catch(NetworkException e) {
			throw new ClientAuthException(ClientAuthException.TYPE_LOGIN, e);
		}
	}

	// Login with a provided password that's already hashed
	public static Client loginPreHashed(String username, String password) throws ClientAuthException {
		if(username.isEmpty() || password.isEmpty())
			throw new ClientAuthException(ClientAuthException.TYPE_LOGIN, new IllegalArgumentException("Username and password can't be empty!"));
		
		Client c = new Client();
		c.profile = new ProfileModel(0, username, password, "", User.DEFAULT_PROFILE_PICTURE);

		// Update profile data with email and any other data
		try {
			c.post("login", new JSONObject());
			c.syncProfile();
		} catch(NetworkException e) {
			throw new ClientAuthException(ClientAuthException.TYPE_LOGIN, e);
		}
		return c;
	}

	public static Client createAccount(String username, String password, String email) throws ClientAuthException {
		if(username.isEmpty() || password.isEmpty())
			throw new ClientAuthException(ClientAuthException.TYPE_LOGIN, new IllegalArgumentException("Username and password can't be empty!"));
		// Create a new blank Client, setting profile data
		Client c = new Client();
		c.profile = new ProfileModel(0, username, hashPassword(password), email, "");

		// Create a message to send to the server's "createAccount" function, 'c' holds password and username, need to also provide email
		try {
			c.post("createAccount", Map.of("email", email));
		} catch(NetworkException e) {
			throw new ClientAuthException(ClientAuthException.TYPE_CREATE_ACCOUNT, e);
		}

		return c;
	}

	// Retrieve a user from cache if the user has been cached, otherwise create default unknown user
	public User getUserByID(int id) {
		if(userMap.containsKey(id))
			return userMap.get(id);
		else return User.NO_USER;
	}

	// Retrieve a user from cache if exists, if not, creates one and puts it in cache
	private User getOrCreateUser(int id, String username, String pfp) {
		if(!userMap.containsKey(id))
			userMap.put(id, new User(id, username, pfp));
		else {
			userMap.get(id).setUsername(username);
			userMap.get(id).setProfilePicture(pfp);
		}
		return userMap.get(id);
	}

	private User getOrCreateUser(JSONObject o) throws JSONException {
		return getOrCreateUser(o.optInt("UserID", 0), o.optString("Name", "unknown"), o.optString("pfp", User.DEFAULT_PROFILE_PICTURE));
	}

	// Retrieve a friend from cache if exists and is a friend, if not, creates one and puts it in cache
	private Friend getOrCreateFriend(int id, String username, String pfp, int chat) {
		if(!userMap.containsKey(id) || !(userMap.get(id) instanceof Friend))
			userMap.put(id, new Friend(id, username, pfp, chat));
		else {
			userMap.get(id).setUsername(username);
			userMap.get(id).setProfilePicture(pfp);
		}
		return (Friend)userMap.get(id);
	}

	// Wraps above function with JSON input
	private Friend getOrCreateFriend(JSONObject o) throws JSONException {
		return getOrCreateFriend(o.optInt("UserID", 0), o.optString("Name", "unknown"), o.optString("pfp", User.DEFAULT_PROFILE_PICTURE), o.optInt("Chat", 0));
	}

	// Retrieve user object for current user
	public User getOwnUser() {
		return getOrCreateUser(profile.getUserID(), profile.getUsername(), profile.getProfilePicURL());
	}

	public Match getMatchByID(int id) {
		if(matchMap.containsKey(id))
			return matchMap.get(id);
		return Match.NO_MATCH;
	}

	// Retrieve a match from cache if exists, if not, creates a new one and puts it in cache
	private Match getOrCreateMatch(int id, int chatID, User white, User black, String status) {
		Match m;
		if(!matchMap.containsKey(id)) {
			m = new Match(id, getOrCreateChat(chatID), white, black, status);
			matchMap.put(id, m);
		} else {
			m = matchMap.get(id);
			m.setStatus(status);
		}
		return m;
	}

	// Wraps above function with JSON input
	private Match getOrCreateMatch(JSONObject o) throws JSONException {	
		User whitePlayer = getOrCreateUser(o.getInt("WhitePlayer"), o.getString("WhiteName"), o.isNull("WhitePfp") ? User.DEFAULT_PROFILE_PICTURE : o.getString("WhitePfp"));
		User blackPlayer = getOrCreateUser(o.getInt("BlackPlayer"), o.getString("BlackName"), o.isNull("BlackPfp") ? User.DEFAULT_PROFILE_PICTURE : o.getString("BlackPfp"));
		return getOrCreateMatch(o.getInt("MatchID"), o.getInt("Chat"), whitePlayer, blackPlayer, o.getString("Status"));
	}

	public Chat getChatByID(int id) {
		if(chatMap.containsKey(id))
			return chatMap.get(id);
		return Chat.NO_CHAT;
	}

	public Chat getOrCreateChat(int id) {
		if(!chatMap.containsKey(id))
			chatMap.put(id, new Chat(id));
		return chatMap.get(id);
	}
	
	// Update profile with server data
	public ProfileModel syncProfile() throws NetworkException {
		try {
			JSONObject profileData = post("getProfileData", new JSONObject());

			// We need to send correct username and password to retrieve information so no need to update those variables
			profile.setEmail(profileData.getJSONObject("response").getString("EmailAddress"));
			if(!profileData.getJSONObject("response").isNull("pfp"))
				profile.setProfilePicURL(profileData.getJSONObject("response").getString("pfp"));
			profile.setUserID(profileData.getJSONObject("response").getInt("UserID"));

			profile.setBio(profileData.getJSONObject("response").optString("bio", ""));

			return profile;
		} catch (JSONException | ClassCastException e) {
			throw new NetworkException("Error in JSON structure", e);
		}
	}

	public ProfileModel getProfile() { return profile; }

	// Update profile with new data
	public void updateProfile(String newUsername, String newEmail, String newBio) throws NetworkException {
		// Send to server to update profile data
		post("updateProfile", Map.of("newUsername", newUsername, "newEmail", newEmail, "newBio", newBio));
		profile.setUsername(newUsername);
		profile.setEmail(newEmail);
		profile.setBio(newBio);
	}

	// Returns {"Won": (int), "Lost": (int), "Draw": (int), "Current": (int), "Total": (int)}
	public JSONObject getMatchStats() throws NetworkException {
		JSONObject received = post("matchStats", new JSONObject());
		if(received.has("response") && received.opt("response") instanceof JSONObject)
			return received.getJSONObject("response");
		else throw new NetworkException("Invalid response for getStats");
	}

	public void updatePassword(String oldPassword, String newPassword) throws NetworkException {
		JSONObject received = post("updatePassword", Map.of("oldPassword", hashPassword(oldPassword), "newPassword", hashPassword(newPassword)));
		if(!received.has("error") || received.getBoolean("error"))
			throw new NetworkException(received.getJSONArray("response").getString(0));
		profile.setPassword(hashPassword(newPassword));
	}

	// Retrieve a list of all matches the user is a part of
	public List<Match> getMatches() throws NetworkException {
		// Call server and throw exception if error in response
		try {
			JSONObject received = post("getMatches", new JSONObject());
			if(received.getBoolean("error"))
				throw new NetworkException("Could not retrieve matches for \"" + profile.getUsername() + "\" " + received.optString("response", ""));

			// Loop through all entries of received matches array, and make new Matches out of JSON representations
			ArrayList<Match> matches = new ArrayList<>();
			received.getJSONArray("response").iterator().forEachRemaining(o -> matches.add(getOrCreateMatch((JSONObject)o)));
			return matches;
		} catch (JSONException | ClassCastException e) {
			throw new NetworkException("Error in JSON structure", e);
		}
	}

	public List<Match> getOpenMatches() throws NetworkException {
		// Call server and throw exception if error in response
		try {
			JSONObject received = post("getOpenMatches", new JSONObject());
			if(received.getBoolean("error"))
				throw new NetworkException("Could not retrieve open matches for \"" + profile.getUsername() + "\" " + received.optString("response", ""));
					
			ArrayList<Match> matches = new ArrayList<>();
			received.getJSONArray("response").iterator().forEachRemaining(o -> matches.add(getOrCreateMatch((JSONObject)o)));
			return matches;
		} catch (JSONException | ClassCastException e) {
			throw new NetworkException("Error in JSON structure", e);
		}
	}
        
	public List<Match> getClosedMatches() throws NetworkException {
		// Call server and throw exception if error in response
		try {
			JSONObject received = post("getClosedMatches", new JSONObject());
			if(received.getBoolean("error"))
				throw new NetworkException("Could not retrieve closed matches for \"" + profile.getUsername() + "\" " + received.optString("response", ""));

			ArrayList<Match> matches = new ArrayList<>();
			received.getJSONArray("response").iterator().forEachRemaining(o -> matches.add(getOrCreateMatch((JSONObject)o)));
			return matches;
		} catch (JSONException | ClassCastException e) {
			throw new NetworkException("Error in JSON structure", e);
		}
	}

	public List<Match> getMatchRequests() throws NetworkException {
		try {
			JSONObject received = post("getMatchRequests", new JSONObject());

			ArrayList<Match> matches = new ArrayList<>();
			received.getJSONArray("response").iterator().forEachRemaining(o -> matches.add(getOrCreateMatch((JSONObject)o)));
			return matches;
		} catch (JSONException | ClassCastException e) {
			throw new NetworkException("Error in JSON structure", e);
		}
	}

	public void sendMatchRequest(String username, boolean playingAsWhite) throws NetworkException {
		// Call server and throw exception if error in response
		JSONObject received = post("sendMatchRequest", Map.of("target", username, "request", playingAsWhite ? Match.Status.WHITE_REQUESTED.toString()
			: Match.Status.BLACK_REQUESTED.toString()));
		// TODO: See if any cases where 'received.getBoolean("response")' is false, and throw a special exception
		// received.getBoolean("received"); // is true when match request successful
	}
	
	// Send match request based on user
	public void sendMatchRequest(User user, boolean playingAsWhite) throws NetworkException { sendMatchRequest(user.getUsername(), playingAsWhite); }

	// Deny match request from given match
	public void denyMatchRequest(Match match) throws NetworkException {
		JSONObject received = post("denyMatchRequest", Map.of("match", match.getID()));
		// TODO: See if any cases where 'received.getBoolean("response")' is false, and throw a special exception
		// received.getBoolean("received"); // is true when match request successful
	}

	public void updateBio(String newBio) throws NetworkException {
		try {
			JSONObject response = post("updateBio", Map.of("bio", newBio));
			if (response.getBoolean("error")) {
				throw new NetworkException(response.opt("response").toString());
			}
			profile.setBio(newBio);
		} catch (JSONException e) {
			throw new NetworkException("Error in JSON structure", e);
		}
	}

	// Retrieve all of user's friends
	public List<Friend> getFriends() throws NetworkException {
		try {
			JSONObject friendsResponse = post("getFriends", new JSONObject());

			// Create new friend for every JSON representation received
			ArrayList<Friend> friends = new ArrayList<>();
			friendsResponse.getJSONArray("response").iterator().forEachRemaining(o -> friends.add(getOrCreateFriend((JSONObject) o)));
			return friends;
		} catch (JSONException | ClassCastException e) {
			throw new NetworkException("Error in JSON structure", e);
		}

	}

	/* Returns true if successfully sent a friend request
	 * Prints error if received error
	 * Prints nothing and returns false on no friend request for "normal" reasons (i.e. already friended or sent/received friend request)
	 * TODO: Print more information regarding false returns
	 */
	public void sendFriendRequest(String username) throws NetworkException {	
		JSONObject received = post("sendFriendRequest", Map.of("target", username));
		// TODO: See if any cases where 'received.getBoolean("response")' is false, and throw a special exception
		// received.getBoolean("received"); // is true when match request successful	
	}

	public void sendFriendRequest(User user) throws NetworkException { sendFriendRequest(user.getUsername()); }

	/* Returns true if successfully accepted a friend request 
	 * Prints error if received error
	 * Prints nothing and returns false on no friend request accepted for "normal" reasons (i.e. friend request doesn't exist or already friends with target)
	 * TODO: Print more information regarding false returns
	 */
	public void acceptFriendRequest(String username) throws NetworkException {
		JSONObject received = post("acceptFriendRequest", Map.of("target", username));
		// TODO: See if any cases where 'received.getBoolean("response")' is false, and throw a special exception
		// received.getBoolean("received"); // is true when match request successful	
	}

	public void acceptFriendRequest(User user) throws NetworkException { acceptFriendRequest(user.getUsername()); }

	/* Returns true if successfully denied a friend request 
	 * Prints error if received error
	 * Prints nothing and returns false on no friend request denied for "normal" reasons (i.e. friend request doesn't exist or is friends with target)
	 * TODO: Print more information regarding false returns
	 */
	public void denyFriendRequest(String username) throws NetworkException {
		JSONObject received = post("denyFriendRequest", Map.of("target", username));
		// TODO: See if any cases where 'received.getBoolean("response")' is false, and throw a special exception
		// received.getBoolean("received"); // is true when match request successful	
	}

	public void denyFriendRequest(User user) throws NetworkException { denyFriendRequest(user.getUsername()); }

	// Retrieve all friend requests
	public List<User> getFriendRequests() throws NetworkException {
		try {
			JSONObject received = post("getFriendRequests", new JSONObject());
			
			// Create new user for every JSON representation received
			ArrayList<User> result = new ArrayList<>();
			received.getJSONArray("response").iterator().forEachRemaining(o -> { result.add(getOrCreateUser((JSONObject)o)); });
			return result;
		} catch (JSONException e) {
			throw new NetworkException("Error in JSON structure", e);
		}

	}

	/* Update Match Status based on function name */
	public void acceptMatchRequest(Match match) throws NetworkException {
		setMatchStatus(match, Match.Status.WHITE_TURN.toString());
	}

	//The other player wins
	public void forfeitMatch(Match match) throws NetworkException {
		setGameWinner(match, !getOwnUser().getUsername().equals(match.getWhite().getUsername()));
    }

	public void setGameTurn(Match match, Boolean turn) throws NetworkException {
		setMatchStatus(match,turn ? //set game status
			Match.Status.WHITE_TURN.toString() :
			Match.Status.BLACK_TURN.toString()
		);
	}
	public void setGameWinner(Match match, Boolean winner) throws NetworkException {
		setMatchStatus(match, winner ? //set game 
			Match.Status.WHITE_WIN.toString() :
			Match.Status.BLACK_WIN.toString()
		);
		match.makeMove(this, "end");
	}
    
	public void setGameDraw(Match match) throws NetworkException {
		setMatchStatus(match, Match.Status.DRAW.toString());
		match.makeMove(this, "end");
	}

	// Set match status on server
	private void setMatchStatus(Match match, String status) throws NetworkException {
		post("setMatchStatus", Map.of("match", match.getID(), "status", status));
		match.setStatus(status);
	}

	// Retrieve new match status from server
	public String syncMatchStatus(Match match) throws NetworkException {
		try {
			JSONObject response = post("getMatchStatus", Map.of("match", match.getID()));
			match.setStatus(response.getString("response"));
			return response.getString("response");
		} catch (JSONException e) {
			throw new NetworkException("Error in JSON structure", e);
		}
	}

	// Upload profile picture to server, using a Base64 representation
	public void uploadProfilePicture(File file) throws NetworkException, IOException {
		try {
			JSONObject send = new JSONObject();
			send.put("image", Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath())));
			JSONObject received = post("uploadProfilePicture", send);
			profile.setProfilePicURL(received.getString("response"));
		} catch (JSONException e) {
			throw new NetworkException("Error in JSON structure", e);
		}
	}

	// Send a message to the server based on a Map representation of JSON data, (appends login details) and return the result
	public JSONObject post(String function, Map<?, ?> message) throws NetworkException {
		return post(function, new JSONObject(message));
	}

	public static String hashPassword(String password) {
		KeySpec spec = new PBEKeySpec(password.toCharArray(), SALT, 65536, 128);
		try {
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			return Base64.getEncoder().encodeToString(keyFactory.generateSecret(spec).getEncoded());
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.err.println("WARN: Could not hash password! Password will be stored as-is");
			return password;
		}
	}

	// Send a message to the server based on a JSON Object, appending User login details, and return the result
	public JSONObject post(String function, JSONObject message) throws NetworkException {
		// Sent JSON Object to server and retrieve response
		message.put("username", profile.getUsername());
		message.put("password", profile.getPassword());
		return post(function, message.toString());
	}

	public JSONObject post(String function) throws NetworkException {
		// Sent JSON Object to server and retrieve response
		JSONObject message = new JSONObject(Map.of("username", profile.getUsername(), "password", profile.getPassword()));
		return post(function, message.toString());
	}

	public static byte[] encrypt(String input) {
		// If encryption is not set up, then set it up
		if(CLIENT_ENCRYPT == null) {
			try {
				// Create a public key out of stored PUBLIC_KEY data
				KeyFactory keyFactory = KeyFactory.getInstance("RSA");
				X509EncodedKeySpec keySpec = new X509EncodedKeySpec(PUBLIC_KEY);
				PublicKey publicKey = keyFactory.generatePublic(keySpec);

				// Create cipher to encrypt AES key with
				Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
				rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
	
				// Generate AES key, and encrypt it
				KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
				keyGenerator.init(128);

				SecretKey aesKey = keyGenerator.generateKey();
				ENCRYPTED_KEY = Base64.getEncoder().encodeToString(rsaCipher.doFinal(aesKey.getEncoded()));

				// Create encrypt and decrypt cipher, with the given IV and key
				IvParameterSpec iv = new IvParameterSpec("hellochessbug!<3".getBytes("UTF-8"));
				CLIENT_ENCRYPT = Cipher.getInstance("AES/CBC/PKCS5PADDING");
				CLIENT_ENCRYPT.init(Cipher.ENCRYPT_MODE, aesKey, iv);
				CLIENT_DECRYPT = Cipher.getInstance("AES/CBC/PKCS5PADDING");
				CLIENT_DECRYPT.init(Cipher.DECRYPT_MODE, aesKey, iv);

				// Encryption set up properly!
				System.out.println("Initialized key and encryption");
			} catch(IOException e) {
				System.err.println("Could not read public key data!");
				e.printStackTrace();
				System.err.println("WARN: Could not initialize encryption! Messages will be sent and received unencrypted");
				CLIENT_ENCRYPT = null;
			} catch(Exception e) {
				System.err.println("WARN: Could not initialize encryption! Messages will be sent and received unencrypted");
				CLIENT_ENCRYPT = null;
			}
		}

		// bytes of the data to send
		byte[] normalBytes = input.getBytes(StandardCharsets.UTF_8);

		// If there is no cipher set up/encryption failed, then resort to unencrypted data. The response will be unencrypted as well
		if(CLIENT_ENCRYPT == null)
			return normalBytes;

		// Attempt to encrypt data, if it fails, just provide raw
		try {
			return new JSONObject(Map.of(
				"data", Base64.getEncoder().encodeToString(CLIENT_ENCRYPT.doFinal(normalBytes)), 
				"key", ENCRYPTED_KEY)
			).toString().getBytes(StandardCharsets.UTF_8);
		} catch (BadPaddingException | IllegalBlockSizeException e) {
			System.err.println("Could not encrypt data!");
			return normalBytes;
		}
	}
	
	public static String decrypt(byte[] data) throws Exception {
		byte[] base64Decoded;
		try {
			base64Decoded = Base64.getDecoder().decode(data);
		} catch (IllegalArgumentException e) {
			// Could not decode with base64, must be raw
			return new String(data);
		}
		return new String(CLIENT_DECRYPT.doFinal(base64Decoded));
	}	

	// Send a string to a given function in the web api, return a JSON result
	public JSONObject post(String function, String message) throws NetworkException {
		long lockBypass = System.nanoTime();
		while(LOCK) {if (System.nanoTime() - lockBypass > 200000000) break;}
		LOCK = true;

		// Send arbitrary string as bytes
	
		byte[] data = encrypt(message);

		// Set up a connection to the server
		HttpsURLConnection con;
		try {
			con = getConnection(function);
		} catch (URISyntaxException e1) {
			LOCK = false;
			throw new NetworkException(function + " - Error: Could not connect to server, Invalid URI Syntax!", e1);
		} catch (MalformedURLException e2) {
			LOCK = false;
			throw new NetworkException(function + " - Error: Could not connect to server, Malformed URL!", e2);
		} catch (IOException e3) {
			LOCK = false;
			throw new NetworkException(function + " - Error: Could not connect to server!", e3);
		}

		// Tell how much data we're sending, connect, and send it
		con.setFixedLengthStreamingMode(data.length);
		try {
			con.connect();
			OutputStream os = con.getOutputStream();
			os.write(data);
		} catch (IOException ioex) {
			LOCK = false;
			throw new NetworkException(function + " - Error: Could not write data to server!", ioex);
		}

		// Read response into `builder`
		String input = "";
		try {
			input = decrypt(con.getInputStream().readAllBytes());
		} catch (Exception ex) {
			LOCK = false;
			throw new NetworkException(function + " - Error: Could not read server response!", ex);
		}

		// Try to parse as straight JSON Object,
		try {
			LOCK = false;
			return new JSONObject(input);
		} catch (JSONException e) {
			// If it's not a JSON Object, it probably contains error data
			LOCK = false;
			throw new NetworkException(function + " - Could not parse response as JSON! \"" + input + "\"", e);
		}
	}

	// Create a HTTPS connection based on a web API function, filling out the required headers
	private static HttpsURLConnection getConnection(String function) throws URISyntaxException, MalformedURLException, IOException {
		HttpsURLConnection con = (HttpsURLConnection)new URI("https://www.zandgall.com/chessbug/" + function).toURL().openConnection();
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		return con;
	}
}
