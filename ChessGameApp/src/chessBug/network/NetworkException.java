// Represents a generic network exception, used whenever the server returns an error or invalid data
package chessBug.network;

public class NetworkException extends Exception {
	private String message;
	private Exception nestedException;

	public NetworkException(String message) {
		this.message = message;
		this.nestedException = null;
	}

	public NetworkException(String message, Exception nestedException) {
		this.message = message;
		this.nestedException = nestedException;
	}

	public Exception getNestedException() {
		return nestedException;
	}

	public String getMessage() {
		return "There was an error received from the server. \"" + message + "\"" + 
			(nestedException == null ? "" : (" - thrown: " + nestedException.getMessage()));
	}
}
