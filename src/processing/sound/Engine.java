package processing.sound;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.devices.AudioDeviceFactory;
import com.jsyn.devices.AudioDeviceManager;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.Multiply;
import com.jsyn.unitgen.UnitGenerator;
import com.jsyn.unitgen.UnitSource;

import processing.core.PApplet;

class Engine {

	private static AudioDeviceManager audioManager;
	private static Engine singleton;

	protected Synthesizer synth;
	// the stereo lineout
	private LineOut lineOut;
	// two multipliers for controlling the global output volume
	private Multiply leftOut;
	private Multiply rightOut;

	private int sampleRate = 44100;

	// set in constructor
	private int inputDevice;
	private int outputDevice;

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

		this.lineOut = new LineOut(); // stereo lineout by default
		this.synth.add(lineOut);
		this.lineOut.start();

		this.leftOut = new Multiply();
		this.rightOut = new Multiply();
		this.setVolume(1.0f);
		this.leftOut.output.connect(0, this.lineOut.input, 0);
		this.rightOut.output.connect(0, this.lineOut.input, 1);
		this.synth.add(this.leftOut);
		this.synth.add(this.rightOut);

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
				// TODO limit number of output channels to 2?
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

	private static boolean checkDeviceHasInputs(int deviceId) {
		return Engine.getAudioManager().getMaxInputChannels(deviceId) > 0;
	}

	private static boolean checkDeviceHasOutputs(int deviceId) {
		// require stereo output
		return Engine.getAudioManager().getMaxOutputChannels(deviceId) > 1;
	}

	protected void selectInputDevice(int deviceId) {
		if (Engine.checkDeviceHasInputs(deviceId)) {
			Engine.singleton.inputDevice = deviceId;
			Engine.singleton.startSynth();
		} else {
			Engine.printError("audio device #" + deviceId + " has no input channels");
		}
	}

	protected void selectOutputDevice(int deviceId) {
		if (Engine.checkDeviceHasOutputs(deviceId)) {
			Engine.singleton.outputDevice = deviceId;
			Engine.singleton.startSynth();
		} else {
			Engine.printError("audio device #" + deviceId + " has no stereo output channel");
		}
	}

	protected void setVolume(double volume) {
		if (Engine.checkRange(volume, "volume")) {
			this.leftOut.inputB.set(volume);
			this.rightOut.inputB.set(volume);
		}
	}

	protected int getSampleRate() {
		return this.synth.getFrameRate();
	}

	protected void add(UnitGenerator generator) {
		if (generator.getSynthesisEngine() == null) {
			this.synth.add(generator);
		}
	}

	protected void remove(UnitGenerator generator) {
		this.synth.remove(generator);
	}

	protected void play(UnitSource source) {
		// TODO check if unit is already connected
		source.getOutput().connect(0, this.leftOut.inputA, 0);
		source.getOutput().connect(1, this.rightOut.inputA, 0);
	}

	protected void stop(UnitSource source) {
		source.getOutput().disconnect(0, this.leftOut.inputA, 0);
		source.getOutput().disconnect(1, this.rightOut.inputA, 0);
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
			lineOut.stop();
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
