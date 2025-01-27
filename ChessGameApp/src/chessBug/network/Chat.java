package chessBug.network;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;

public class Chat {
	private int chatID;

	private int messageNumber = 0;

	private ArrayList<Message> messages;

	public Chat(int chatID) {
		this.chatID = chatID;
		messages = new ArrayList<>();
	}

	public Stream<Message> poll(Client client) {
		// Ask the server how many messages exist in the current chat
		JSONObject numMessagesPoll = new JSONObject();
		numMessagesPoll.put("chat", chatID);
		JSONObject numMessagesResponse = client.post("getMessageCount", numMessagesPoll);
		if(numMessagesResponse.getBoolean("error")) {
			System.err.println("Could not retrieve message count! Chat: " + chatID);
			System.err.println(numMessagesResponse.opt("response"));
			return Stream.empty();
		}

		// If the number of messages if more than the amount we've already counted, grab al new messages
		int currentNumber;
		currentNumber = numMessagesResponse.getInt("response");

		// No new messages
		if(currentNumber <= messageNumber)
			return Stream.empty();

		// Ask for all new messages
		JSONObject getMessagesPoll = new JSONObject();
		getMessagesPoll.put("chat", chatID);
		getMessagesPoll.put("num", currentNumber - messageNumber); // getting current number of messages minus the previous count

		JSONObject getMessagesResponse = client.post("getNMessages", getMessagesPoll);

		if(getMessagesResponse.getBoolean("error")) {
			System.err.println("Could not retrieve " + (currentNumber - messageNumber) + " messages from chat: " + chatID);
			System.err.println(getMessagesResponse.opt("response"));
			return Stream.empty();
		}

		// Poll through new messages
		JSONArray retrievedMessages = getMessagesResponse.getJSONArray("response");
		for(int i = 0; i < currentNumber - messageNumber; i++) {
			JSONObject o = retrievedMessages.getJSONObject(i);
			messages.add(new Message(o.getInt("MessageID"), o.getString("Content"), o.getInt("Sender"), Timestamp.valueOf(o.getString("Time")), o.getInt("Chat"), o.getString("Author")));
		}

		// Return a stream of all new messages
		Stream<Message> out = messages.stream().skip(messageNumber);
		messageNumber = currentNumber;
		return out;
	}

	public ArrayList<Message> getAllMessages() {
		return messages;
	}

	public void send(Client from, String content) {
		JSONObject message = new JSONObject();
		message.put("chat", chatID);
		message.put("content", content);
		from.post("sendMessage", message);
	}
}
