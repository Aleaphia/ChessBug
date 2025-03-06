package chessBug.network;

public class Friend extends User {
	private Chat chat;
	public Friend(int id, String username, String pfp, int chatID) {
		super(id, username, pfp);
		chat = new Chat(chatID);
	}

	public Chat getDirectChat() {
		return chat;
	}
}
