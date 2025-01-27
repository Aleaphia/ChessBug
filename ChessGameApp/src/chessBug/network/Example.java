package chessBug.network;

import java.util.List;
import java.util.stream.Stream;

public class Example {
	public static void main(String[] args) {
		// User enters login information to log in to the application
		// ~~~~~~~~~~~~~~
		Client client;
		try {
			client = new Client("user", "p@ssw0rd!"); // (example user)
		} catch (ClientAuthException e) {
			// Shouldn't happen unless server is offline or something
			e.printStackTrace();
			return;
		}
		// ~~~~~~~~~~~~~~
		// See also:
		// Client newAccount = Client.createAccount(username, password, email)
		
		// User looks at friend list
		// ~~~~~~~~~~~~~~
		List<Friend> friends = client.getFriends();
		for(Friend f : friends)
			System.out.println("I am friends with " + f.getUsername());
		// ~~~~~~~~~~~~~~

		// User opens a chat with their first friend
		// ~~~~~~~~~~~~~~
		Chat chat = friends.get(0).getDirectChat();
		// ~~~~~~~~~~~~~~

		// Application grabs all messages from the chat
		// ~~~~~~~~~~~~~~
		Stream<Message> newMessages = chat.poll(client); // An empty stream means no new messages!
		System.out.println("Received the following messages:");
		newMessages.forEach((m) -> {
			System.out.println("\t" + m.getAuthor() + " (" + m.getTimestamp() + ") > " + m.getContent());
		});
		// ~~~~~~~~~~~~~~
		// See also:
		// List<Message> allMessages = dm.getAllMessages();
		//     - Chat.poll(Client) must be called first, otherwise will return empty list
		//      (Chat.poll(Client) should be called to update messages periodically)

		// User sends something in the chat
		// ~~~~~~~~~~~~~~
		chat.send(client, "Hello! Sent from the example code");

		chat.poll(client).forEach((m) -> System.out.println("New message: " + m.getAuthor() + " (" + m.getTimestamp() + ") > " + m.getContent()));
		// ~~~~~~~~~~~~~~

		// TODO:
		// User challenges friend to match
		// Application retrieves a Chat instance from a Match
		// User updates own information (i.e. change username, password, email)
	}
}
