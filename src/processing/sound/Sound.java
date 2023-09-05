package processing.sound;

import com.jsyn.Synthesizer;
import com.jsyn.data.FloatSample;
import com.jsyn.devices.AudioDeviceManager;
import com.jsyn.engine.SynthesisEngine;

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
 * @see MultiChannel
 */
public class Sound {

	public Sound(PApplet parent) {
		Engine.getEngine(parent);
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
	 * @param listAll whether to include all devices in the output listing. By default,
	 * sound devices without any inputs or outputs are omitted from the output listing
	 * for clarity. Pass `true` here if you want a complete list of all devices (for debugging).
	 * @return an array giving the names of all audio devices available on this
	 *         computer
	 * @webref Configuration:Sound
	 * @webBrief Print and return information on available audio devices and their number of input/output channels.
	 */
	public static String[] list() {
		return Sound.list(false);
	}

	public static AudioDeviceManager getAudioDeviceManager() {
		return Engine.getAudioDeviceManager();
	}

	public static String[] list(boolean listAll) {
		AudioDeviceManager audioManager = Engine.getAudioDeviceManager();
		int numDevices = audioManager.getDeviceCount();
		String[] devices = new String[numDevices];
		for (int i = 0; i < numDevices; i++) {
			String deviceName = audioManager.getDeviceName(i);
			devices[i] = audioManager.getDeviceName(i);
			int maxInputs = audioManager.getMaxInputChannels(i);
			int maxOutputs = audioManager.getMaxOutputChannels(i);
			boolean isDefaultInput = (i == audioManager.getDefaultInputDeviceID());
			boolean isDefaultOutput = (i == audioManager.getDefaultOutputDeviceID());
			if (listAll || maxInputs > 0 || maxOutputs > 0) {
				System.out.println("device id " + i + ": " + deviceName);
				if (listAll || maxInputs > 0) {
					System.out.println("  input channels : " + maxInputs + (isDefaultInput ? "   (default)" : ""));
				}
				if (listAll || maxOutputs > 0) {
					System.out.println("  output channels: " + maxOutputs + (isDefaultOutput ? "   (default)" : ""));
				}
			}
		}
		return devices;
	}

	public static int sampleRate() {
		return Engine.getEngine().getSampleRate();
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
	public static int sampleRate(int sampleRate) {
		Engine.getEngine().setSampleRate(sampleRate);
		return Sound.sampleRate();
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
	 * @param deviceName
	 *            the device name obtained from Sound.list()
	 * @see Sound#list()
	 * @webref Configuration:Sound
	 * @webBrief Choose the device (sound card) which should be used for grabbing audio input using AudioIn.
	 */
	public static int inputDevice(int deviceId) {
		return Engine.getEngine().selectInputDevice(deviceId);
	}

	public static int inputDevice(String deviceName) {
		return Engine.getEngine().selectInputDevice(Engine.getDeviceIdByName(deviceName));
	}

	/**
	 * Choose the device (sound card) which the Sound library's audio output should
	 * be sent to. The output device should support stereo output (2 channels).
	 * 
	 * @param deviceId
	 *            the device id obtained from Sound.list()
	 * @param deviceName
	 *            the device name obtained from Sound.list()
	 * @see Sound#list()
	 * @webref Configuration:Sound
	 * @webBrief Choose the device (sound card) which the Sound library's audio output should be sent to.
	 */
	public static int outputDevice(int deviceId) {
		return Engine.getEngine().selectOutputDevice(deviceId);
	}

	public static int outputDevice(String deviceName) {
		return Engine.getEngine().selectOutputDevice(Engine.getDeviceIdByName(deviceName));
	}

	public static int defaultOutputDevice() {
		return Sound.outputDevice(Engine.getAudioDeviceManager().getDefaultOutputDeviceID());
	}

	public static int defaultInputDevice() {
		return Sound.inputDevice(Engine.getAudioDeviceManager().getDefaultInputDeviceID());
	}

	/**
	 * Set the overall output volume of the Processing sound library.
	 *
	 * @param volume
	 *            the desired output volume, normally between 0.0 and 1.0 (default
	 *            is 1.0)
	 * @webref Configuration:Sound
	 */
	public static void volume(float volume) {
		Engine.getEngine().setVolume(volume);
	}

	/**
	 * Prints information about the sound library's current memory and CPU usage to the console.
	 * @webref Configuration:Sound
	 */
	public static void status() {
		Engine.printMessage(String.format("%.2f", Engine.getEngine().synth.getCurrentTime()) + " seconds elapsed, generated " + Engine.getEngine().synth.getFrameCount() + " frames (framerate " + Engine.getEngine().synth.getFrameRate() + ")");
		Engine.printMessage("  CPU usage: " + Math.round(100 * Engine.getEngine().synth.getUsage()) + "%");
		Engine.printMessage("  elements in synthesizer network: " + Engine.getEngine().nCircuits);
		Engine.printMessage("  sound sources currently playing: " + Engine.getEngine().nPlayingCircuits);
		long nSamples = 0;
		for (FloatSample s : SoundFile.SAMPLECACHE.values()) {
			nSamples += s.getNumFrames() * s.getChannelsPerFrame();
		}
		Engine.printMessage("  decoded audio samples held in cache: " + SoundFile.SAMPLECACHE.size() + " (" + nSamples + " frames total)");
		// might return something useful later
		Sound.printConnections();
	}

	public static void printConnections() {
		((SynthesisEngine) Engine.getEngine().synth).printConnections();
	}

	/**
	 * Direct access to the underlying JSyn Synthesizer object. Use at your own risk.
	 */
	public static Synthesizer getSynthesizer() {
		return Engine.getEngine().synth;
	}
}
