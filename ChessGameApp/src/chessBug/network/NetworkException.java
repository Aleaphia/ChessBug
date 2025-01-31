package chessBug.network;

public class NetworkException extends Exception {
	private String message;

	public NetworkException(String message) {
		this.message = message;
	}

	public String getMessage() {
		return "There was an error received from the server. \"" + message + "\"";
	}
}
