package processing.sound;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.LineUnavailableException;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.devices.AudioDeviceFactory;
import com.jsyn.devices.AudioDeviceManager;
import com.jsyn.devices.AudioDeviceOutputStream;
import com.jsyn.devices.jportaudio.JPortAudioDevice;
import com.jsyn.unitgen.ChannelOut;
import com.jsyn.unitgen.Multiply;
import com.jsyn.unitgen.UnitGenerator;
import com.jsyn.unitgen.UnitSource;

import processing.core.PApplet;

/**
 * Singleton wrapper around the JSyn `Synthesizer`.
 */
class Engine {

	// a true system-wide singleton, there is no point having more than one of these
	private static AudioDeviceManager audioManager;

	static AudioDeviceManager getAudioDeviceManager() {
		if (Engine.audioManager == null) {
			try {
				Class.forName("javax.sound.sampled.AudioSystem");
				Engine.audioManager = AudioDeviceFactory.createAudioDeviceManager();
			} catch (ClassNotFoundException e) {
				Engine.audioManager = new JSynAndroidAudioDeviceManager();
			}
		}
		return Engine.audioManager;
	}

	/**
	 * Singleton instance that is created by the first method call to or creation of any Sound library class.
	 * Any calls to configuration, start() or play() methods will be passed on to this engine.
	 * In theory it's possible to have multiple instances of the library run on several different
	 * sound devices simultaneously, by first setting this variable to null, forcing a (second)
	 * singleton to be created, and then swapping them out manually at will.
	 */
	static Engine singleton;

	static Engine getEngine(PApplet parent) {
		if (Engine.singleton == null) {
			Engine.singleton = new Engine();
		}
		if (parent != null) {
			Engine.singleton.registerWithParent(parent);
		}
		return Engine.singleton;
	}

	static Engine getEngine() {
		return Engine.getEngine(null);
	}



	protected Synthesizer synth;
	// multi-channel lineouts
	private ChannelOut[] output;
	// multipliers for each output channel for controlling the global output volume
	private Multiply[] volume;

	private int sampleRate = 44100;

	protected int inputDevice = -1;
	protected int outputDevice = -1;
	protected int outputChannel;

	/**
	 * when multi-channel mode is active, only the first (left) output of any unit
	 * generators is added. the mode is activated by calling selectOutputChannel()
	 */
	protected boolean multiChannelMode = false;

	// keep track of the number of objects connected to the synthesizer circuit
	protected int nCircuits = 0;
	protected int nPlayingCircuits = 0;

	/**
	 * Create a new synthesizer and connect it to the default sound devices.
	 */
	private Engine() {

		// suppress JSyn's INFO log messages to stop them from showing
		// up as redtext in the Processing console
		Logger logger = Logger.getLogger(com.jsyn.engine.SynthesisEngine.class.getName());
		logger.setLevel(Level.WARNING);

		this.createSynthesizer();

		Engine.singleton = this;
	}
	
	private boolean triedPortAudio = false;

	protected boolean usePortAudio() {
		// TODO check if we're actually on Windows?
		if (!this.triedPortAudio) {
			Engine.printMessage("Loading PortAudio");
			this.triedPortAudio = true;
			System.loadLibrary("portaudio_x64");
			AudioDeviceManager newManager = AudioDeviceFactory.createAudioDeviceManager();
			if (newManager instanceof JPortAudioDevice) {
				Engine.printMessage("Using PortAudio");
				Engine.audioManager = newManager;
				this.createSynthesizer();
				// TODO might need to reconnect old entities if possible?
			}
		}
		return Engine.audioManager instanceof JPortAudioDevice;
	}

	private void createSynthesizer() {
		// try {
			AudioDeviceOutputStream o = Engine.audioManager.createOutputStream(AudioDeviceManager.USE_DEFAULT_DEVICE, this.sampleRate, 2);
			o.start();
			o.stop();
		// } catch (LineUnavailableException e) {
			// System.out.println(e);
			// this.usePortAudio();
			// return;
		// }
		// create and start the synthesizer, and set this object as the singleton.
		this.synth = JSyn.createSynthesizer(Engine.getAudioDeviceManager());

		// select default devices
		// TODO should use audioManager.getDefaultInputDeviceID() ??
		for (int i = 0; i < Engine.getAudioDeviceManager().getDeviceCount(); i++) {
			if (Engine.checkDeviceHasOutputs(i)) {
				this.outputDevice = i;
				break;
			}
		}
		if (outputDevice == -1) {
			Engine.printError("could not find any audio devices with a stereo output");
			return;
		}
		for (int i = 0; i < Engine.getAudioDeviceManager().getDeviceCount(); i++) {
			if (Engine.checkDeviceHasInputs(i)) {
				this.inputDevice = i;
				break;
			}
		}
		if (inputDevice == -1) {
			Engine.printWarning("could not find any sound devices with input channels, you won't be able to use the AudioIn class");
		}
		this.startSynth();
	}

	protected void startSynth() {
		if (this.synth.isRunning()) {
			this.synth.stop();
			// TODO clean up old outputs/volumes/entire synth network (if any)?
			for (ChannelOut c : this.output) {
				this.synth.remove(c);
			}
			for (Multiply m : this.volume) {
				this.synth.remove(m);
			}
		}

		this.output = new ChannelOut[Engine.getAudioDeviceManager().getMaxOutputChannels(this.outputDevice)];
		this.volume = new Multiply[Engine.getAudioDeviceManager().getMaxOutputChannels(this.outputDevice)];
		for (int i = 0; i < this.output.length; i++) {
			this.output[i] = new ChannelOut();
			this.output[i].setChannelIndex(i);
			this.synth.add(output[i]);
		  this.output[i].start();

			this.volume[i] = new Multiply();
			this.volume[i].output.connect(this.output[i].input);
			this.synth.add(this.volume[i]);
		}
		this.setVolume(1.0f);

		this.synth.start(this.sampleRate,
				this.inputDevice, Engine.getAudioDeviceManager().getMaxInputChannels(this.inputDevice),
				this.outputDevice, Engine.getAudioDeviceManager().getMaxOutputChannels(this.outputDevice));
	}


	protected void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
		this.startSynth();
	}

	private static boolean isValidDeviceId(int deviceId) {
		if (deviceId >= 0 && deviceId < Engine.getAudioDeviceManager().getDeviceCount()) {
			return true;
		}
		Engine.printError("not a valid device id: " + deviceId);
		return false;
	}

	private static boolean checkDeviceHasInputs(int deviceId) {
		return Engine.getAudioDeviceManager().getMaxInputChannels(deviceId) > 0;
	}

	private static boolean checkDeviceHasOutputs(int deviceId) {
		// require stereo output
		return Engine.getAudioDeviceManager().getMaxOutputChannels(deviceId) > 1;
	}

	protected int selectInputDevice(int deviceId) {
		if (Engine.isValidDeviceId(deviceId)) {
			if (Engine.checkDeviceHasInputs(deviceId)) {
				this.inputDevice = deviceId;
				this.startSynth();
			} else {
				Engine.printError("audio device #" + deviceId + " has no input channels");
			}
		}
		return this.inputDevice;
	}

	protected int selectOutputDevice(int deviceId) {
		if (Engine.isValidDeviceId(deviceId)) {
			if (Engine.checkDeviceHasOutputs(deviceId)) {
				Engine.getEngine().outputDevice = deviceId;
				Engine.getEngine().startSynth();
			} else {
				Engine.printError("audio device #" + deviceId + " has no stereo output channel");
			}
		}
		return this.outputDevice;
	}

	protected int selectOutputChannel(int channel) {
		if (channel == -1) {
			// disable multi-channel mode
			this.outputChannel = 0;
			this.multiChannelMode = false;
		} else if (channel < 0 || channel > Engine.getAudioDeviceManager().getMaxOutputChannels(this.outputDevice)) {
			Engine.printError("Invalid channel #" + channel + ", current output device only has " + Engine.getAudioDeviceManager().getMaxOutputChannels(this.outputDevice) + " channels");
		} else {
			this.outputChannel = channel;
			this.multiChannelMode = true;
		}
		return this.outputChannel;
	}

	protected String getSelectedInputDeviceName() {
		return Engine.getAudioDeviceManager().getDeviceName(this.inputDevice);
	}

	protected String getSelectedOutputDeviceName() {
		return Engine.getAudioDeviceManager().getDeviceName(this.outputDevice);
	}

	protected void setVolume(double volume) {
		if (Engine.checkRange(volume, "volume")) {
			for (Multiply m : this.volume) {
				m.inputB.set(volume);
			}
		}
	}

	protected int getSampleRate() {
		return this.synth.getFrameRate();
	}

	protected void add(UnitGenerator generator) {
		if (generator.getSynthesisEngine() == null) {
			this.synth.add(generator);
			this.nCircuits++;
		}
	}

	protected void remove(UnitGenerator generator) {
		// TODO check generator.getSynthesisEngine
		this.synth.remove(generator);
		this.nCircuits--;
	}

	protected void play(UnitSource source) {
		// TODO check if unit is already connected
		// source.getOutput().isConnected()
		for (int i = 0; i < source.getOutput().getNumParts(); i++) {
			source.getOutput().connect(i, this.volume[(this.outputChannel + i) % this.getAudioDeviceManager().getMaxOutputChannels(this.outputDevice)].inputA, 0);
			if (this.multiChannelMode) {
				// only add the first (left) channel
				break;
			}
		}
		this.nPlayingCircuits++;
	}

	protected void stop(UnitSource source) {
		source.getOutput().disconnectAll();
		this.nPlayingCircuits--;
	}

	/**
	 * Internal helper class for Processing library callbacks
	 */
	public class Callback {
		public void dispose() {
			synth.stop();
		}

		public void pause() {
			// TODO
		}

		public void resume() {
			// TODO
		}
	}

	private Callback registeredCallback;

	/**
	 * Register a callback with the sketch PApplet, so that the synth thread is stopped when the sketch is finished.
	 */
	private void registerWithParent(PApplet theParent) {
		if (this.registeredCallback != null) {
			return;
		}
		// register Processing library callback methods
		this.registeredCallback = new Callback();
		theParent.registerMethod("dispose", this.registeredCallback);
		// Android only
		theParent.registerMethod("pause", this.registeredCallback);
		theParent.registerMethod("resume", this.registeredCallback);
	}



	// static helper methods that do stuff like checking argument values or printing library messages

	protected static int getDeviceIdByName(String deviceName) {
		for (int i = 0; i < Engine.getAudioDeviceManager().getDeviceCount(); i++) {
			if (deviceName.equals(Engine.getAudioDeviceManager().getDeviceName(i))) {
				return i;
			}
		}
		Engine.printError("No device with name '" + deviceName + "' found.");
		return -1;
	}

	protected static boolean checkAmp(float amp) {
		if (amp < -1 || amp > 1) {
			Engine.printError("amplitude has to be in [-1,1]");
			return false;
		} else if (amp == 0.0) {
			Engine.printWarning("an amplitude of 0 means this sound is not audible now");
		}
		return true;
	}

	protected static boolean checkPan(float pan) {
		if (pan < -1 || pan > 1) {
			Engine.printError("pan has to be in [-1,1]");
			return false;
		}
		return true;
	}

	protected static boolean checkRange(double value, String name) {
		if (value < 0 || value > 1) {
			Engine.printError(name + " parameter has to be between 0 and 1 (inclusive)");
			return false;
		}
		return true;
	}

	protected static void printMessage(String message) {
		PApplet.println("Sound library: " + message);
	}

	protected static void printWarning(String message) {
		PApplet.println("Sound library warning: " + message);
	}

	protected static void printError(String message) {
		PApplet.println("Sound library error: " + message);
	}

}
