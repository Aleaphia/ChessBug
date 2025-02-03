package chessBug.network;

public class User {
	private int id;
	private String username;
	
	public User(int id, String username) {
		this.id = id;
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public int getID() {
		return id;
	}
}
