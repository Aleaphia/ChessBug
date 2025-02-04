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
        @Override
        public boolean equals(Object o){
            if (o instanceof User user2){
                return user2.getID() == id;
            }
            return false;
        }
}
