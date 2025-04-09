/**
 * Holds a list of messages, and can poll new messages from the server
 *
 * Use poll(Client) in order to update the list of messages with any new messages received since last poll()
 * Call poll(Client) after creating chat in order to populate the list with all preexisting messages
 */

package chessBug.network;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;

public class Chat {
	public static final Chat NO_CHAT = new Chat(0);

	private int chatID;

	private int messageNumber = 0;

	private ArrayList<Message> messages;

	public Chat(int chatID) {
		this.chatID = chatID;
		messages = new ArrayList<>();
	}

	public Stream<Message> poll(Client client) throws NetworkException {
		// Ask the server how many messages exist in the current chat
		JSONObject numMessagesResponse = client.post("getMessageCount", Map.of("chat", chatID));

		// If the number of messages if more than the amount we've already counted, grab al new messages
		int currentNumber;
		currentNumber = numMessagesResponse.getInt("response");

		// No new messages
		if(currentNumber <= messageNumber)
			return Stream.empty();

		// Ask for all new messages (the current number of messages in chat, minus the ones we've already collected)
		JSONObject getMessagesResponse = client.post("getNMessages", Map.of("chat", chatID, "num", currentNumber - messageNumber));

		// Poll through list of new messages
		JSONArray retrievedMessages = getMessagesResponse.getJSONArray("response");
		for(int i = 0; i < currentNumber - messageNumber; i++) {
			// Create a new Message for every JSON representation
			JSONObject o = retrievedMessages.getJSONObject(i);
			messages.add(new Message(o.getInt("MessageID"), o.getString("Content"), o.getInt("Sender"), Timestamp.valueOf(o.getString("Time")), o.getInt("Chat"), client.getUserByID(o.getInt("Sender"))));
		}

		// Return a stream of all new messages
		Stream<Message> out = messages.stream().skip(messageNumber);
		messageNumber = currentNumber;
		return out;
	}

	public ArrayList<Message> getAllMessages() {
		return messages;
	}

	public int getID() {
		return chatID;
	}

	public void send(Client from, String content) throws NetworkException {
		from.post("sendMessage", Map.of("chat", chatID, "content", content));
	}
}
