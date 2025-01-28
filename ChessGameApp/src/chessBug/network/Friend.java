package chessBug.network;

public class Friend extends User {
	private Chat chat;
	public Friend(int id, String username, int chatID) {
		super(id, username);
		chat = new Chat(chatID);
	}

	public Chat getDirectChat() {
		return chat;
	}
}
