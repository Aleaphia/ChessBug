package chessBug.network;

import java.sql.Timestamp;

public class Message {
	private int id;
	private String content;
	private int senderID;
	private Timestamp timestamp;
	private int chat;
	private String author;
	
	protected Message(int id, String content, int senderID, Timestamp timestamp, int chat, String author) {
		this.id = id;
		this.content = content;
		this.senderID = senderID;
		this.timestamp = timestamp;
		this.chat = chat;
		this.author = author;
	}

	public String getContent() {
		return content;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public String getAuthor() {
		return author;
	}
}
