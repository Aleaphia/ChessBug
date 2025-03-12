// An example for utilizing the Client and other network functions

package chessBug.network;

import java.util.List;
import java.util.Scanner;
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
		// Scanner s = new Scanner(System.in);
		System.out.print("Send Message > ");
		// chat.send(client, s.nextLine());
		chat.send(client, "Example message!");

		chat.poll(client).forEach((m) -> System.out.println("New message: " + m.getAuthor() + " (" + m.getTimestamp() + ") > " + m.getContent()));
		// s.close();
		// ~~~~~~~~~~~~~~
		
		// User lists all matches they are part of
		// ~~~~~~~~~~~~~~
		List<Match> matches = client.getMatches();
		for(Match m : matches)
			System.out.printf("Match %s -> (%d) %s vs (%d) %s%n", m, m.getWhite().getID(), m.getWhite().getUsername(), m.getBlack().getID(), m.getBlack().getUsername());
		// ~~~~~~~~~~~~~~
		
		// User updates own information
		// ~~~~~~~~~~~~~~
		try {
			client.updateProfile("newUsername", "newUserEmail@email.org", "newP@ssw0rd!");
			client.updateProfile("user", "user@email.org", "p@ssw0rd!");
		} catch (NetworkException e) {
			System.err.println("Uh oh! Example profile update code failed!");
			e.printStackTrace();
		}
		// ~~~~~~~~~~~~~~
	}
}
