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
	 * @see MultiChannel#availableChannels()
	 */
	public static int activeChannel(int channel) {
		Engine.getEngine().selectOutputChannel(channel);
		return MultiChannel.activeChannel();
	}

	public static int activeChannel() {
		return Engine.getEngine().outputChannel;
	}

	/**
	 * Gets the number of output channels available on an output device
	 * 
	 * @param deviceId if none is given, gets information about the current device.
	 * @return the number of output channels available on the current output device
	 *
	 * @webref I/O:MultiChannel
	 * @see Sound#outputDevice(int)
	 */
	public static int availableChannels(int deviceId) {
		return Engine.getAudioDeviceManager().getMaxOutputChannels(deviceId);
	}

	public static int availableChannels() {
		return MultiChannel.availableChannels(Engine.getEngine().outputDevice);
	}

	/**
	 * Force using PortAudio instead of JavaSound (Windows only).
	 *
	 * Support for 24 bit audio interfaces on Windows requires using the native
	 * PortAudio bindings instead of the default JavaSound one. The Sound library 
	 * will automatically check for and load PortAudio when it is necessary to do 
	 * so. However, when <code>Sound.list()</code> is called before selecting an 
	 * output device, it might show an incorrect number of channels for 
	 * multi-channel interfaces. By explicitly loading PortAudio ahead of time you 
	 * can ensure that <code>Sound.list()</code> will show accurate channel 
	 * numbers from the start.
	 *
	 * Returns <code>true</code> if PortAudio was successfully loaded.
	 *
	 * @webref I/O:MultiChannel
	 * @webBrief Force using PortAudio instead of JavaSound (Windows only).
	 * @see Sound#list()
	 */
	public static boolean usePortAudio() {
		return Engine.getEngine().usePortAudio(true);
	}

}
