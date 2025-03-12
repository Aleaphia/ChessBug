// Represents a generic network exception, used whenever the server returns an error or invalid data
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
