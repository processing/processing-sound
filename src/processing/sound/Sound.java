package processing.sound;

import com.jsyn.data.FloatSample;
import com.jsyn.devices.AudioDeviceManager;
import com.jsyn.Synthesizer;

import processing.core.PApplet;

/**
 * This class can be used for configuring the Processing Sound library.
 *
 * The Sound class allows for configuring global properties of the sound
 * library's audio synthesis and playback, such as the output device, sample
 * rate or global output volume.
 *
 * Information on available input and output devices can be obtained by calling
 * <b>Sound.list()</b>
 * 
 * @webref Configuration:Sound
 * @webBrief This class can be used for configuring the Processing Sound library.
 */
public class Sound {

	// could make this static as well, Engine class guarantees it's a singleton
	// anyway
	private Engine engine;

	public Sound(PApplet parent) {
		this.engine = Engine.getEngine(parent);
	}

	/**
	 * 
	 * @param parent
	 *            typically use "this"
	 * @param sampleRate
	 *            the sample rate to be used by the synthesis engine (default 44100)
	 * @param outputDevice
	 *            the device id of the sound card that sound should be played on
	 * @param inputDevice
	 *            the device id of the sound card from which sound should be
	 *            captured
	 * @param volume
	 *            the overall output volume of the library (default 1.0)
	 */
	public Sound(PApplet parent, int sampleRate, int outputDevice, int inputDevice, float volume) {
		this(parent);
		this.sampleRate(sampleRate);
		this.inputDevice(inputDevice);
		this.outputDevice(outputDevice);
		this.volume(volume);
	}

	/**
	 * Print and return information on available audio devices and their number of
	 * input/output channels.
	 * Under normal circumstances you will not want to call <b>Sound.list()</b> in 
	 * your actual sketch code, but only for testing to figure out which sound cards 
	 * are available on a new system and how to select them. However, if the order 
	 * of devices on your system is prone to fluctuate from reboot to reboot, you 
	 * can also use the device name array returned by the function to automate device 
	 * selection by name in your own code.
	 * 
	 * @return an array giving the names of all audio devices available on this
	 *         computer
	 * @webref Configuration:Sound
	 * @webBrief Print and return information on available audio devices and their number of input/output channels.
	 */
	public static String[] list() {
		AudioDeviceManager audioManager = Engine.getAudioManager();
		int numDevices = audioManager.getDeviceCount();
		String[] devices = new String[numDevices];
		for (int i = 0; i < numDevices; i++) {
			String deviceName = audioManager.getDeviceName(i);
			devices[i] = audioManager.getDeviceName(i);
			int maxInputs = audioManager.getMaxInputChannels(i);
			int maxOutputs = audioManager.getMaxOutputChannels(i);
			boolean isDefaultInput = (i == audioManager.getDefaultInputDeviceID());
			boolean isDefaultOutput = (i == audioManager.getDefaultOutputDeviceID());
			System.out.println("device id " + i + ": " + deviceName);
			System.out.println("  max inputs : " + maxInputs + (isDefaultInput ? "   (default)" : ""));
			System.out.println("  max outputs: " + maxOutputs + (isDefaultOutput ? "   (default)" : ""));
		}
		return devices;
	}

	public int sampleRate() {
		return this.engine.getSampleRate();
	}

	/**
	 * Get or set the internal sample rate of the synthesis engine.
	 * 
	 * @param sampleRate
	 *            the sample rate to be used by the synthesis engine (default 44100)
	 * @return the internal sample rate used by the synthesis engine
	 * @webref Configuration:Sound
	 * @webBrief Get or set the internal sample rate of the synthesis engine.
	 */
	public int sampleRate(int sampleRate) {
		this.engine.setSampleRate(sampleRate);
		return this.sampleRate();
	}

	/**
	 * Choose the device (sound card) which should be used for grabbing audio input
	 * using AudioIn.  Note that this setting affects the choice of sound card, which 
	 * is not necessarily the same as the number of the input channel. If your sound 
	 * card has more than one input channel, you can specify which channel to use in
	 * the constructor of the AudioIn class.
	 * 
	 * @param deviceId
	 *            the device id obtained from Sound.list()
	 * @see Sound#list()
	 * @webref Configuration:Sound
	 * @webBrief Choose the device (sound card) which should be used for grabbing audio input using AudioIn.
	 */
	public void inputDevice(int deviceId) {
		this.engine.selectInputDevice(deviceId);
	}

	/**
	 * Choose the device (sound card) which the Sound library's audio output should
	 * be sent to. The output device should support stereo output (2 channels).
	 * 
	 * @param deviceId
	 *            the device id obtained from list()
	 * @see Sound#list()
	 * @webref Configuration:Sound
	 * @webBrief Choose the device (sound card) which the Sound library's audio output should be sent to.
	 */
	public void outputDevice(int deviceId) {
		this.engine.selectOutputDevice(deviceId);
	}

	/**
	 * Set the overall output volume of the Processing sound library.
	 *
	 * @param volume
	 *            the desired output volume, normally between 0.0 and 1.0 (default
	 *            is 1.0)
	 * @webref Configuration:Sound
	 * @webBrief Set the overall output volume of the Processing sound library.
	 */
	public void volume(float volume) {
		this.engine.setVolume(volume);
	}

	/**
	 * Prints information about the sound library's current memory and CPU usage to the console.
	 * @webref Configuration:Sound
	 */
	public void status() {
		Engine.printMessage(String.format("%.2f", this.engine.synth.getCurrentTime()) + " seconds elapsed, generated " + this.engine.synth.getFrameCount() + " frames (framerate " + this.engine.synth.getFrameRate() + ")");
		Engine.printMessage("  CPU usage: " + Math.round(100 * this.engine.synth.getUsage()) + "%");
		Engine.printMessage("  elements in synthesizer network: " + this.engine.nCircuits);
		Engine.printMessage("  sound sources currently playing: " + this.engine.nPlayingCircuits);
		long nSamples = 0;
		for (FloatSample s : SoundFile.SAMPLECACHE.values()) {
			nSamples += s.getNumFrames() * s.getChannelsPerFrame();
		}
		Engine.printMessage("  decoded audio samples held in cache: " + SoundFile.SAMPLECACHE.size() + " (" + nSamples + " frames total)");
		// might return something useful later
	}

	/**
	 * Use at your own risk.
	 */
	public Synthesizer getSynthesizer() {
		return this.engine.synth;
	}
}
