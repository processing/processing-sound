package processing.sound;

/**
 * Controls the routing of sounds on multi-channel devices
 * 
 * @webref I/O:MultiChannel
 */
public abstract class MultiChannel {

	/**
	 * Controls which output channel sounds will be played back to.
	 *
	 * After selecting a new output channel, all sounds that start `play()`ing will
	 * be sent to that channel.
	 * 
	 * @param channel the channel number to send sounds to
	 * @return the channel number that sounds will be sent to
	 *
	 * @webref I/O:MultiChannel
	 * @see MultiChannel#numOutputChannels()
	 */
	public static int activeChannel(int channel) {
		Engine.getEngine().selectOutputChannel(channel);
		return MultiChannel.activeChannel();
	}

	public static int activeChannel() {
		return Engine.getEngine().outputChannel;
	}

	/**
	 * Gets the number of output channels available on the current output device
	 * @return the number of output channels available on the current output device
	 *
	 * @webref I/O:MultiChannel
	 * @see Sound#outputDevice()
	 */
	public static int availableChannels() {
		return Engine.getAudioManager().getMaxOutputChannels(Engine.getEngine().outputDevice);
	}
}
