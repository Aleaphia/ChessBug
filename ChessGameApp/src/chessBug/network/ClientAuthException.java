package chessBug.network;

public class ClientAuthException extends Exception {
	// Not really needed but could be replaced with enums if there's distinct error details to be made from these two types of auth errors
	public static String TYPE_LOGIN = "log in", TYPE_CREATE_ACCOUNT = "create account";
	private String type, response;

	public ClientAuthException(String type, String response) {
		this.response = response;
		this.type = type;
	}

	public String getMessage() {
		return "Failed to " + type + ". Received: \"" + response + "\"";
	}
}
