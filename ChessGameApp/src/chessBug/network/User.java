package chessBug.network;

public abstract class User {
	private int id;
	private String username;
	
	protected User(int id, String username) {
		this.id = id;
		this.username = username;
	}

	public String getUsername() {
		return username;
	}
}
