/*
 * A Class used for calling ChessBug server side Web API functions
 * There is a method for every server side function supported
 * Client is initialized with user login details, in order to authorize all web interactions
 */

 package chessBug.network;

 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.nio.charset.StandardCharsets;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Base64;
 import java.util.Map;
 import java.util.HashMap;
 
 import java.nio.file.Files;
 import java.nio.charset.StandardCharsets;
 
 import javax.net.ssl.HttpsURLConnection;
 import javax.crypto.BadPaddingException;
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.SecretKey;
 import javax.crypto.SecretKeyFactory;
 import javax.crypto.KeyGenerator;
 
 import java.security.InvalidAlgorithmParameterException;
 import java.security.InvalidKeyException;
 import java.security.KeyFactory;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.NoSuchAlgorithmException;
 import java.security.PublicKey;
 import java.security.spec.KeySpec;
 import java.security.spec.X509EncodedKeySpec;
 import java.security.spec.InvalidKeySpecException;
 
 import javax.crypto.spec.IvParameterSpec;
 import javax.crypto.spec.PBEKeySpec;
 import javax.crypto.spec.SecretKeySpec;
 
 import org.json.JSONObject;
 
 import com.warrenstrange.googleauth.GoogleAuthenticator;
 
 import chessBug.profile.ProfileModel;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 public class Client {
	 // "Salt" used to hash passwords
	 private static final byte[] SALT = "chessbug!(%*¡ºªħéñ€óßáñåçœø’ħ‘ºº".getBytes(StandardCharsets.UTF_8);
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
		 // Call "login" function from the server
		 System.out.println("Hashed password to: " + hashPassword(password));
		 profile = new ProfileModel(0, username, hashPassword(password), "", User.DEFAULT_PROFILE_PICTURE);
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
 
	 public static Client loginPreHashed(String username, String password) throws ClientAuthException {
		 Client c = new Client();
		 c.profile = new ProfileModel(0, username, password, "", User.DEFAULT_PROFILE_PICTURE);
		 JSONObject loginMessage = c.post("login", new JSONObject());
		 // If the server returns an error, throw an exception
		 if(loginMessage.getBoolean("error")) {
			 throw new ClientAuthException(ClientAuthException.TYPE_LOGIN, loginMessage.opt("response").toString());
		 }
 
		 // Update profile data with email and any other data
		 try {
			 c.syncProfile();
		 } catch(NetworkException e) {
			 throw new ClientAuthException(ClientAuthException.TYPE_LOGIN, e.getMessage());
		 }
		 return c;
	 }
 
	 public static Client createAccount(String username, String password, String email) throws ClientAuthException {
		 // Create a new blank Client, setting profile data
		 Client c = new Client();
		 c.profile = new ProfileModel(0, username, hashPassword(password), email, "");
 
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
		 else return User.NO_USER;
	 }
 
	 // Retrieve a user from cache if exists, if not, creates one and puts it in cache
	 private User getOrCreateUser(int id, String username, String pfp) {
		 if(!userMap.containsKey(id))
			 userMap.put(id, new User(id, username, pfp, null, false));
		 else {
			 userMap.get(id).setUsername(username);
			 userMap.get(id).setProfilePicture(pfp);
		 }
		 return userMap.get(id);
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
 
	 // Retrieve user object for current user
	 public User getOwnUser() {
		 return getOrCreateUser(profile.getUserID(), profile.getUsername(), profile.getProfilePicURL());
	 }
 
	 public Match getMatchByID(int id) {
		 if(matchMap.containsKey(id))
			 return matchMap.get(id);
		 return Match.NO_MATCH;
	 }
 
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
		 profile.setPassword(hashPassword(newPassword));
	 }
 
	 // Returns {"Won": (int), "Lost": (int), "Draw": (int), "Current": (int), "Total": (int)}, defaults to all 0s on error
	 public JSONObject getMatchStats() {
		 JSONObject received = post("matchStats", new JSONObject());
		 if(received.getBoolean("error"))
			 return new JSONObject(Map.of("Won", 0, "Lost", 0, "Draw", 0, "Current", 0, "Total", 0));
		 return received.getJSONObject("response");
	 }
 
	 public void updatePassword(String newPassword) throws NetworkException {
		 JSONObject received = post("updatePassword", Map.of("newPassword", newPassword));
		 if(received.getBoolean("error"))
			 throw new NetworkException(received.opt("response").toString());
 
		 profile.setPassword(hashPassword(newPassword));
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
			 matches.add(getOrCreateMatch(o.getInt("MatchID"), o.getInt("Chat"), whitePlayer, blackPlayer, o.getString("Status")));
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
			 result.add(getOrCreateMatch(o.getInt("MatchID"), o.getInt("Chat"), whitePlayer, blackPlayer, o.getString("Status")));
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
			 result.add(getOrCreateMatch(o.getInt("MatchID"), o.getInt("Chat"), whitePlayer, blackPlayer, o.getString("Status")));
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
			 result.add(getOrCreateMatch(o.getInt("MatchID"), o.getInt("Chat"), whitePlayer, blackPlayer, o.getString("Status")));
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
		 public void forfeitMatch(Match match){
			 //The other player wins
			 setGameWinner(match, !getOwnUser().getUsername().equals(match.getWhite().getUsername()));
		 }
		 public void setGameTurn(Match match, Boolean turn){
			 setMatchStatus(match,turn ? //set game status
				 Match.Status.WHITE_TURN.toString() :
				 Match.Status.BLACK_TURN.toString()
			 );
		 }
		 public void setGameWinner(Match match, Boolean winner){
			 setMatchStatus(match, winner ? //set game status
				 Match.Status.WHITE_WIN.toString() :
				 Match.Status.BLACK_WIN.toString()
			 );
			 match.makeMove(this, "end");
		 }
		 public void setGameDraw(Match match){
			 setMatchStatus(match, Match.Status.DRAW.toString());
			 match.makeMove(this, "end");
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
	 public JSONObject post(String function, JSONObject message) {
		 // Sent JSON Object to server and retrieve response
		 message.put("username", profile.getUsername());
		 message.put("password", profile.getPassword());
		 return post(function, message.toString());
	 }
 
	 public static byte[] encrypt(String input) {
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
			System.out.println("Could not decode with base64: " + new String(data));
			e.printStackTrace();
		}
		return new String(CLIENT_DECRYPT.doFinal(base64Decoded));
	}

 
	 // Send a string to a given function in the web api, return a JSON result
	 public JSONObject post(String function, String message) {
		 long lockBypass = System.nanoTime();
		 while(LOCK) {if (System.nanoTime() - lockBypass > 200000000) break;}
		 LOCK = true;
 
		 // Send arbitrary string as bytes
 
		 // byte[] data = message.getBytes(StandardCharsets.UTF_8);
		 byte[] data = encrypt(message);
 
		 // Set up a connection to the server
		 HttpsURLConnection con = getConnection(function);
		 if(con == null) {
			 System.err.println("Could not connect to server!");
			 JSONObject out = new JSONObject();
			 out.put("response", "Error: Could not connect to server!");
			 out.put("error", true);
			 LOCK = false;
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
			 LOCK = false;
			 return out;
		 }
 
		 // Read response into `builder`
		 String input = "";
		 try {
			 input = decrypt(con.getInputStream().readAllBytes());
		 } catch (Exception ex) {
			 System.err.println("Could not read server response!");
			 JSONObject out = new JSONObject();
			 out.put("response", "Unable to reach or communicate with the server!");
			 out.put("error", true);	
			 ex.printStackTrace();
			 LOCK = false;
			 return out;
		 }
 
		 // Try to parse as straight JSON Object,
		 try {
			 LOCK = false;
			 return new JSONObject(input);
		 } catch (JSONException e) {
			 // If it's not a JSON Object, it probably contains error data
			 JSONObject jo = new JSONObject();
			 jo.put("error", true);
			 jo.put("response", input);
			 LOCK = false;
			 return jo;
		 }
	 }
 
	 // Create a HTTPS connection based on a web API function, filling out the required headers
	 private static HttpsURLConnection getConnection(String function) {
		 try {
			 HttpsURLConnection con = (HttpsURLConnection)new URI("https://www.zandgall.com/chessbug/" + function).toURL().openConnection();
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
 
	 private boolean is2FAEnabled = false;
	 private String secretKey;
 
	 public boolean is2FAEnabled(){
		 return is2FAEnabled;
	 }
 
	 public String get2FASecretKey() {
		 return secretKey;
	 }
 
	 public boolean verify2FACode(int code) {
		 if (is2FAEnabled && secretKey != null && !secretKey.isEmpty()) {
			 GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
			 return googleAuthenticator.authorize(secretKey, code);
		 }
		 return false;
	 }
 }
 
