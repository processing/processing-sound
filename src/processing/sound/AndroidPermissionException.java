package processing.sound;

/**
 * AndroidPermissionException is thrown when trying to create an AudioIn on
 * Android when you have not granted the required RECORD_AUDIO permission.
 */
public class AndroidPermissionException extends RuntimeException {
	public AndroidPermissionException(String message) {
		super(message);
	}
}
