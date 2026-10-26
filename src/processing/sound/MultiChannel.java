package processing.sound;

import java.util.stream.IntStream;

import com.jsyn.unitgen.ChannelOut;

/**
 * This class provides methods for querying the multi-channel output 
 * capabilities of audio devices, and for providing fine-grained control for the 
 * routing of sounds when outputting on a device with more than 2 channels.
 * 
 * @webref I/O:MultiChannel
 */
public abstract class MultiChannel {

	/**
	 * Finds the audio device (sound card) with the highest number of output 
	 * channels, and selects it as the output device.<br>
	 * This method is just a convenient shorthand for what is probably the most 
	 * frequent multi-channel device selection use case. You could implement the 
	 * same (and even more precise) programmatic control over device selection in 
	 * your own sketch using <code>Sound.list()</code>, 
	 * <code>MultiChannel.availableChannels()</code> and 
	 * <code>Sound.outputDevice()</code>.
	 *
	 * @webBrief Selects the output device with the highest number of channels.
	 *
	 * @webref I/O:MultiChannel
	 * @see Sound#list()
	 * @see Sound#outputDevice()
	 * @see MultiChannel#availableChannels()
	 */
	public static int autoSelectDevice(int minChannels) {
		int[] candidates = IntStream.range(0, Sound.getAudioDeviceManager().getDeviceCount())
			.filter(i -> MultiChannel.availableChannels(i) >= minChannels).toArray();
		// TODO throw Exception when 0 candidates
		return Engine.getEngine().selectOutputDevice(candidates);
	}

	public static int autoSelectDevice() {
		// there might be duplicate listings with different drivers, try them all
		int nChannels = IntStream.range(0, Sound.getAudioDeviceManager().getDeviceCount())
			.map(i -> MultiChannel.availableChannels(i)).max().getAsInt();
		return MultiChannel.autoSelectDevice(nChannels);
	}

	/**
	 * Controls which output channel sounds will be played back to.
	 *
	 * After selecting a new output channel, all sounds that start `play()`ing 
	 * will be sent to that channel.
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
	 * Connects a SoundObject to the given output channel.
	 *
	 * Use this only for SoundObjects that are already playing back on some 
	 * channel, to have them play back on another channel at the same time.
	 */
	public static void connectToOutput(SoundObject o, int channel) {
		Engine.getEngine().connectToOutput(channel, o.circuit);
	}

	/**
	 * Disconnects a SoundObject from the given output channel.
	 *
	 * Only use on SoundObjects that were previously connected using 
	 * connectToOutput()
	 *
	 * @see connectToOutput()
	 */
	public static void disconnectFromOutput(SoundObject o, int channel) {
		Engine.getEngine().disconnectFromOutput(channel, o.circuit);
	}

	/**
	 * Gets the number of output channels available on a given device
	 * 
	 * @param deviceId the id of the device whose number of output channels should 
	 * be returned. If none is given, gets information about the current device.
	 * @return the number of output channels available on the given device
	 *
	 * @webref I/O:MultiChannel
	 * @see Sound#outputDevice(int)
	 * @see Sound#list()
	 */
	public static int availableChannels(int deviceId) {
		return Engine.getAudioDeviceManager().getMaxOutputChannels(deviceId);
	}

	public static int availableChannels() {
		return MultiChannel.availableChannels(Engine.getEngine().outputDevice);
	}

	/**
	 * Returns the JSyn <code>ChannelOut</code> objects that are being played to 
	 * by the synthesizer
	 * @see ChannelOut
	 */
	public static ChannelOut[] outputs() {
		return Engine.getEngine().output;
	}

	/**
	 * Force using PortAudio instead of JavaSound.
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
	 * @webBrief Force using PortAudio instead of JavaSound.
	 * @see Sound#list()
	 */
	public static boolean usePortAudio() {
		Engine engine = Engine.getEngine(null, true);
		try {
			if (engine.usePortAudio(true)) {
				// all good, go for default device
				engine.selectOutputDevice(-1);
			}
			return engine.isUsingPortAudio();
		} catch (RuntimeException e) {
			Engine.printError(e.getMessage());
		}
		return engine.isUsingPortAudio();
	}

	/**
	 * Force using the native ASIO driver interface instead of JavaSound or PortAudio.
	 *
	 * While the PortAudio bindings that the Sound library falls back to do expose
	 * ASIO drivers on Windows, in practice they fail to open many ASIO devices.
	 * This method switches the library to the JAsioHost bindings which talk to the
	 * ASIO drivers directly, giving you the lowest possible output latency and
	 * reliable access to all channels of your audio interface. This is a Windows-
	 * only feature: on other operating systems (or if no ASIO driver is installed)
	 * the call prints an error and returns <code>false</code>.
	 *
	 * Call it at the very top of your <code>setup()</code>, before any other Sound
	 * library object is created or any other device is selected, for example:
	 * <pre>
	 * MultiChannel.useJASIO();
	 * Sound.outputDevice("ASIO4ALL v2");
	 * </pre>
	 * After the switch, <code>Sound.list()</code> lists all ASIO drivers
	 * registered in the system. Select the one you want to use with
	 * <code>Sound.outputDevice()</code>. Note that the ASIO API only allows one
	 * driver to be in use at a time.
	 *
	 * Returns <code>true</code> if ASIO support was successfully loaded.
	 *
	 * @webref I/O:MultiChannel
	 * @webBrief Force using native ASIO drivers instead of JavaSound or PortAudio.
	 * @see MultiChannel#usePortAudio()
	 * @see Sound#list()
	 */
	public static boolean useJASIO() {
		Engine engine;
		try {
			// do not select an output device yet: ASIO only allows one driver to
			// be loaded at a time, and Sound.list() needs to probe each driver
			// to report its channel counts
			engine = Engine.getEngine(null, Engine.DRIVER_JASIO, false);
		} catch (RuntimeException e) {
			Engine.printError(e.getMessage());
			return false;
		}
		try {
			engine.useJAsio();
			return engine.isUsingJAsio();
		} catch (RuntimeException e) {
			Engine.printError(e.getMessage());
		}
		return engine.isUsingJAsio();
	}

}
