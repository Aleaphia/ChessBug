// An exception for when you failed to log in or create an account
package chessBug.network;

public class ClientAuthException extends Exception {
	// Not really needed but could be replaced with enums if there's distinct error details to be made from these two types of auth errors
	public static String TYPE_LOGIN = "log in", TYPE_CREATE_ACCOUNT = "create account";
	private String type;
	private Exception response;

	public ClientAuthException(String type, Exception response) {
		this.response = response;
		this.type = type;
	}

	public Exception getServerResponse() { return response; }

	public String getMessage() {
		return "Failed to " + type + ". Received: \"" + response + "\"";
	}
}
