package processing.sound;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.devices.AudioDeviceFactory;
import com.jsyn.devices.AudioDeviceManager;
import com.jsyn.unitgen.ChannelOut;
import com.jsyn.unitgen.Multiply;
import com.jsyn.unitgen.UnitGenerator;
import com.jsyn.unitgen.UnitSource;

import processing.core.PApplet;

/**
 * Singleton wrapper around the JSyn `Synthesizer`.
 */
class Engine {

	private static AudioDeviceManager audioManager;
	private static Engine singleton;

	protected Synthesizer synth;
	// multi-channel lineouts
	private ChannelOut[] output;
	// multipliers for controlling the global output volume
	private Multiply[] volume;

	private int sampleRate = 44100;

	// set in constructor
	private int inputDevice;
	private int outputDevice;
	private int outputChannel;

	// keep track of the number of objects connected to the synthesizer circuit
	protected int nCircuits = 0;
	protected int nPlayingCircuits = 0;

	protected static Engine getEngine(PApplet parent) {
		if (Engine.singleton == null) {
			Engine.singleton = new Engine(parent);
		}
		return Engine.singleton;
	}

	protected static Engine getEngine() {
		return Engine.singleton;
	}

	private Engine(PApplet theParent) {
		// only call initalisation steps if not already initialised
		if (Engine.singleton != null) {
			return;
		}

		// suppress JSyn's INFO log messages to stop them from showing
		// up as redtext in the Processing console
		Logger logger = Logger.getLogger(com.jsyn.engine.SynthesisEngine.class.getName());
		logger.setLevel(Level.WARNING);

		// create and start the synthesizer, and set this object as the singleton.
		this.synth = JSyn.createSynthesizer(Engine.getAudioManager());

		// select default devices
		for (int i = 0; i < Engine.getAudioManager().getDeviceCount(); i++) {
			if (Engine.checkDeviceHasOutputs(i)) {
				this.outputDevice = i;
				break;
			}
			if (i == Engine.getAudioManager().getDeviceCount()) {
				Engine.printError("library initalization failed: could not find any audio devices with a stereo output");
				return;
			}
		}
		for (int i = 0; i < Engine.getAudioManager().getDeviceCount(); i++) {
			if (Engine.checkDeviceHasInputs(i)) {
				this.inputDevice = i;
				break;
			}
			if (i == Engine.getAudioManager().getDeviceCount()) {
				Engine.printWarning("could not find any sound devices with input channels, you won't be able to use the AudioIn class");
			}
		}
		
		this.output = new ChannelOut[Engine.getAudioManager().getMaxOutputChannels(this.outputDevice)]; 
		this.volume = new Multiply[Engine.getAudioManager().getMaxOutputChannels(this.outputDevice)]; 
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

		this.startSynth();
		Engine.singleton = this;

		// register Processing library callback methods
		Object callback = new Callback();
		theParent.registerMethod("dispose", callback);
		// Android only
		theParent.registerMethod("pause", callback);
		theParent.registerMethod("resume", callback);
	}

	protected void startSynth() {
		if (this.synth.isRunning()) {
			this.synth.stop();
		}

		this.synth.start(this.sampleRate,
				this.inputDevice, Engine.getAudioManager().getMaxInputChannels(this.inputDevice),
				this.outputDevice, Engine.getAudioManager().getMaxOutputChannels(this.outputDevice));
	}

	protected static AudioDeviceManager getAudioManager() {
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

	protected void setSampleRate(int sampleRate) {
		Engine.singleton.sampleRate = sampleRate;
		Engine.singleton.startSynth();
	}

	private static boolean isValidDeviceId(int deviceId) {
		if (deviceId >= 0 && deviceId < Engine.getAudioManager().getDeviceCount()) {
			return true;
		}
		Engine.printError("not a valid device id: " + deviceId);
		return false;
	}

	private static boolean checkDeviceHasInputs(int deviceId) {
		return Engine.getAudioManager().getMaxInputChannels(deviceId) > 0;
	}

	private static boolean checkDeviceHasOutputs(int deviceId) {
		// require stereo output
		return Engine.getAudioManager().getMaxOutputChannels(deviceId) > 1;
	}

	protected void selectInputDevice(int deviceId) {
		if (!Engine.isValidDeviceId(deviceId)) {
			return;
		}
		if (Engine.checkDeviceHasInputs(deviceId)) {
			Engine.singleton.inputDevice = deviceId;
			Engine.singleton.startSynth();
		} else {
			Engine.printError("audio device #" + deviceId + " has no input channels");
		}
	}

	protected void selectOutputDevice(int deviceId) {
		if (!Engine.isValidDeviceId(deviceId)) {
			return;
		}
		if (Engine.checkDeviceHasOutputs(deviceId)) {
			Engine.singleton.outputDevice = deviceId;
			Engine.singleton.startSynth();
		} else {
			Engine.printError("audio device #" + deviceId + " has no stereo output channel");
		}
	}

	protected void selectOutputChannel(int channel) {
		if (channel < 0 || channel > Engine.getAudioManager().getMaxOutputChannels(Engine.singleton.outputDevice)) {
			Engine.printError("Invalid channel #" + channel + ", current output device only has " + Engine.getAudioManager().getMaxOutputChannels(Engine.singleton.outputDevice) + " channels");
			return;
		}
		this.outputChannel = channel;
	}

	protected static int getDeviceIdByName(String deviceName) {
		for (int i = 0; i < Engine.getAudioManager().getDeviceCount(); i++) {
			if (deviceName.equals(Engine.getAudioManager().getDeviceName(i))) {
				return i;
			}
		}
		Engine.printError("No device with name '" + name + "' found.");
		return -1;
	}

	protected static String getSelectedInputDeviceName() {
		return Engine.getAudioManager().getDeviceName(Engine.singleton.inputDevice);
	}

	protected static String getSelectedOutputDeviceName() {
		return Engine.getAudioManager().getDeviceName(Engine.singleton.outputDevice);
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
			source.getOutput().connect(i, this.volume[this.outputChannel + i].inputA, 0);
		}
		this.nPlayingCircuits++;
	}

	protected void stop(UnitSource source) {
		source.getOutput().disconnectAll();
		this.nPlayingCircuits--;
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
}
