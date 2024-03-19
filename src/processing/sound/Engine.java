package processing.sound;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.devices.AudioDeviceFactory;
import com.jsyn.devices.AudioDeviceManager;
import com.jsyn.devices.AudioDeviceOutputStream;
import com.jsyn.devices.javasound.JavaSoundAudioDevice;
import com.jsyn.devices.jportaudio.JPortAudioDevice;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.unitgen.ChannelOut;
import com.jsyn.unitgen.Multiply;
import com.jsyn.unitgen.UnitGenerator;
import com.jsyn.unitgen.UnitSource;

import processing.core.PApplet;

/**
 * Wrapper around the JSyn `Synthesizer` and its `AudioDeviceManager`.
 */
class Engine {

	static boolean verbose = false;

	private static AudioDeviceManager createDefaultAudioDeviceManager() {
		try {
			Class.forName("javax.sound.sampled.AudioSystem");
			// create a JavaSound device first
			return AudioDeviceFactory.createAudioDeviceManager(true);
		} catch (ClassNotFoundException e) {
			return new JSynAndroidAudioDeviceManager();
		}
	}

	private static AudioDeviceManager createAudioDeviceManager(boolean portAudio) {
		if (!portAudio) {
			AudioDeviceManager a = Engine.createDefaultAudioDeviceManager();
			if (a.getDefaultOutputDeviceID() != -1) {
				return a;
			}
			// if the default device manager lists no output devices, go straight for 
			// portaudio
			Engine.printMessage("Didn't find any output devices with the default driver, trying PortAudio...");
		}
		// hide JPortAudio init messages from console
		PrintStream originalStream = System.out;
		PrintStream originalErr = System.err;
		if (!Engine.verbose) {
			System.setOut(new PrintStream(new OutputStream(){
				public void write(int b) { }
			}));
			System.setErr(new PrintStream(new OutputStream(){
				public void write(int b) { }
			}));
		}
		// JPortAudio takes care of loading all native libraries -- except the 
		// dependent portaudio dll on Windows for some reason. try loading it no 
		// matter what platform we're on and ignore any errors, if it's really not 
		// supported on this system then the JPortAudio device further down will 
		// blow up anyway
		try {
			System.loadLibrary("portaudio_x64");
		} catch (UnsatisfiedLinkError e) {
		}

		try {
			return new JPortAudioDevice();
		} catch (UnsatisfiedLinkError e) {
			// on loading PortAudio the first time on Mac, an exception with the 
			// following message is thrown:
			// no suitable image found.  Did find:
			// ~/Documents/Processing/libraries/sound/library/macos-x86_64/libjportaudio.jnilib: 
			// code signature in 
			// (~/Documents/Processing/libraries/sound/library/macos-x86_64/libjportaudio.jnilib) 
			// not valid for use in process using Library Validation: library load 
			// disallowed by system policy at 
			// java.base/jdk.internal.loader.NativeLibraries.load(Native Method)
			if (e.getMessage().contains("disallowed")) {
				throw new RuntimeException("in order to use the PortAudio drivers, you need to give Processing permission to open the PortAudio library file.\n\n============================== ENABLING PORTAUDIO ON MAC OS X ==============================\n\nPlease follow these steps to enable PortAudio (dont worry, you only need to do this once):\n\n  - if you pressed 'Move to Bin' in the previous popup, you will need first need to restore the\n    library file: please find libjportaudio.jnilib in your Bin, right click and select 'Put Back'\n\n  - go to System Preferences > Security & Privacy> General. At the bottom you will see\na message saying that 'libjportaudio.jnilib was blocked'. Press 'Allow Anyway'. When you\nrun this sketch again you should get another popup, just select 'Open' and you're done!\n\n============================================================================================");
			} else if (Engine.verbose) {
				e.printStackTrace();
			}
			throw new RuntimeException("PortAudio is not supported on this operating system/architecture");
		} finally {
			System.setOut(originalStream);
			System.setErr(originalErr);
		}
	}

	/**
	 * Singleton instance that is created by the first method call to or creation 
	 * of any Sound library class.
	 * Any calls to configuration, start() or play() methods will be passed on to 
	 * this engine. In theory it's possible to have multiple instances of the 
	 * library run on several different sound devices simultaneously, by first 
	 * setting this variable to null, forcing a (second) singleton to be created, 
	 * and then swapping them out manually at will.
	 */
	private static Engine singleton;

	// static Engine getEngine(boolean portAudio) {
	// 	return Engine.

	static Engine getEngine() {
		return Engine.getEngine(null);
	}

	static Engine getEngine(PApplet parent) {
		return Engine.getEngine(parent, false);
	}

	static Engine getEngine(PApplet parent, boolean portAudio) {
		if (Engine.singleton == null) {
			// this might throw a RuntimeException, which is fine
		 	Engine.singleton = new Engine(Engine.createAudioDeviceManager(portAudio));
		}
		if (parent != null) {
			Engine.singleton.registerWithParent(parent);
		}
		return Engine.singleton;
	}

	static AudioDeviceManager getAudioDeviceManager() {
		return Engine.getEngine().synth.getAudioDeviceManager();
	}

	protected Synthesizer synth;
	boolean hasBeenUsed = false;
	protected final Set<UnitGenerator> addedUnits = new HashSet<UnitGenerator>();

	// multi-channel lineouts
	protected ChannelOut[] output;
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

	/**
	 * Create a new synthesizer and connect it to the default output device.
	 */
	private Engine(AudioDeviceManager audioDeviceManager) {
		this(audioDeviceManager, -1);
	}

	private Engine(AudioDeviceManager audioDeviceManager, int outputDevice) {
		// suppress JSyn's INFO log messages to stop them from showing
		// up as redtext in the Processing console
		Logger logger = Logger.getLogger(com.jsyn.engine.SynthesisEngine.class.getName());
		logger.setLevel(Level.WARNING);

		this.createSynth(audioDeviceManager);
		// this method starts the synthesizer -- if the output fails, it might 
		// create a new PortAudio synth on the fly and try to start that
		this.selectOutputDevice(outputDevice);
		this.selectInputDevice(-1);
	}
	
	private void createSynth(AudioDeviceManager deviceManager) {
		if (this.synth != null) {
			this.stopSynth();
			// TODO disconnect EVERYTHING so it can be garbage collected
			if (this.hasBeenUsed) {
				Engine.printWarning("Switching audio device drivers. Any previously created Sound library objects can not be used any more!");
				Engine.printWarning("To remove this error messge, make sure to call Sound.outputDevice(...) at the very top of your setup()");
				this.hasBeenUsed = false;
			}
		}
		this.synth = JSyn.createSynthesizer(deviceManager);
		// try {
			// this might be -1 if there is no device with inputs
			// this.inputDevice = deviceManager.getDefaultInputDeviceID();
		// } catch (RuntimeException e) {
			// JPortAudioDevice even throws an exception if none of the devices have 
			// inputs...
		// }
	}

	public boolean isUsingPortAudio() {
		return this.synth.getAudioDeviceManager() instanceof JPortAudioDevice;
	}

	/**
	 * Switch to/from using PortAudio.
	 * Called in two different cases:
	 * 1. explicitly by the user (through MultiChannel.usePortAudio())
	 * 2. automatically by selectOutputDevice() when it fails to open a line using 
	 * JavaSound
	 */
	protected boolean usePortAudio(boolean portAudio) {
		if (portAudio != this.isUsingPortAudio()) {
			this.createSynth(Engine.createAudioDeviceManager(portAudio));
			// if this was called by the user (from the MultiChannel class), its their 
			// responsibilit to select output device and start the synth!
			this.inputDevice = -1;
		}
		return this.isUsingPortAudio();
	}

	/**
	 * Stop the synthesizer and remove all ChannelOuts
	 */
	private void stopSynth() {
		if (this.synth.isRunning()) {
			this.synth.stop();
			// TODO clean up old outputs/volumes/entire synth network (if any)?
			for (ChannelOut c : this.output) {
				c.stop();
				c.input.disconnectAll();
				this.synth.remove(c);
			}
			this.output = null;
			for (Multiply m : this.volume) {
				m.stop();
				m.inputA.disconnectAll();
				this.synth.remove(m);
			}
			this.volume = null;
		}
	}

	private void startSynth() {
		// it looks like some synth errors (such as Blocking API not implemented on 
		// Windows PortAudio) are unrecoverable, so it would actually be good to 
		// *always* purge the entire synth and not just stop/start it...
		this.stopSynth();

		this.output = new ChannelOut[this.synth.getAudioDeviceManager().getMaxOutputChannels(this.outputDevice)];
		this.volume = new Multiply[this.synth.getAudioDeviceManager().getMaxOutputChannels(this.outputDevice)];
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

		// prevent IndexOutOfBoundsException on input-less devices
		int inputChannels = this.inputDevice >= 0 ?
			this.synth.getAudioDeviceManager().getMaxInputChannels(this.inputDevice) : 0;
		this.synth.start(this.sampleRate,
				this.inputDevice, inputChannels,
				this.outputDevice, this.synth.getAudioDeviceManager().getMaxOutputChannels(this.outputDevice));
	}


	protected void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
		// TODO check if the sample rate works by calling this.selectOutputDevice?
		this.startSynth();
	}

	private boolean isValidDeviceId(int deviceId) {
		if (deviceId >= 0 && deviceId < this.synth.getAudioDeviceManager().getDeviceCount()) {
			return true;
		}
		Engine.printError("not a valid device id: " + deviceId);
		return false;
	}

	private boolean checkDeviceHasInputs(int deviceId) {
		return this.synth.getAudioDeviceManager().getMaxInputChannels(deviceId) > 0;
	}

	protected int selectInputDevice(int deviceId) {
		if (deviceId == -1) {
			int defaultInputDevice = this.synth.getAudioDeviceManager().getDefaultInputDeviceID();
			if (defaultInputDevice == -1) {
				Engine.printWarning("Did not find any sound devices with input channels, you won't be able to use the AudioIn class");
			} else {
				// if the default device is a WDM-KS binding better not touch it, 
				// selecting it might ruin the synth object for good
				if (!this.getDeviceName(defaultInputDevice).contains("WDM-KS")) {
					// otherwise, give it a shot
					try {
						this.selectInputDevice(this.synth.getAudioDeviceManager().getDefaultInputDeviceID());
					} catch (RuntimeException e) {
						Engine.printWarning("failed to initialise default input device '" + this.getDeviceName(deviceId) + "' (" + e.getMessage() + ")");
						this.inputDevice = -1;
						this.startSynth();
					}
				}
			}
		} else if (this.isValidDeviceId(deviceId)) {
			if (this.checkDeviceHasInputs(deviceId)) {
				int oldInputDevice = this.inputDevice;
				this.inputDevice = deviceId;
				// might throw a RuntimeException (see above)
				this.startSynth();
			} else {
				Engine.printError("audio device #" + deviceId + " has no input channels");
			}
		}
		return this.inputDevice;
	}

	private boolean checkDeviceHasOutputs(int deviceId) {
		// require a working stereo output
		return this.synth.getAudioDeviceManager().getMaxOutputChannels(deviceId) > 1;
	}

	private void probeDeviceOutputLine(int deviceId, int sampleRate) throws LineUnavailableException {
		// based on 
		// https://github.com/philburk/jsyn/blob/06f9a9a4d6aa4ddabde81f77878826a62e5d79ab/src/main/java/com/jsyn/devices/javasound/JavaSoundAudioDevice.java#L141-L174
		// TODO actually call manager.createOutputStream(deviceId, this.sampleRate) 
		// (hiding stdout) to avoid strange channel numbering weirdness
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, new AudioFormat(sampleRate, 16, 2, true, false));
		// this one just checks whether the AudioSystem generally supports that 
		// sampleRate
		// if (!AudioSystem.isLineSupported(info)) {
		Line line = AudioSystem.getMixer(AudioSystem.getMixerInfo()[deviceId]).getLine(info);
		line.open();
		line.close();
	}

	/**
	 * Go through the list of candidate device ids until the first one that works
	 * @throws RuntimeException if none of the output devices work
	 */
	protected int selectOutputDevice(int[] candidates) {
		for (int i : candidates) {
			try {
				return this.selectOutputDevice(i);
			} catch (RuntimeException e) {
			}
		}
		throw new RuntimeException("failed to play to any of the output devices");
	}

	/**
	 * After calling this method, the synth is running.
	 * @param deviceId device index, or -1 to select/find an appropriate stereo 
	 * output device
	 */
	protected int selectOutputDevice(int deviceId) {
		if (deviceId == -1) {
			// if the default device does not work, loop through
			try {
				return this.selectOutputDevice(IntStream.concat(
							// FIXME sometimes the JPortAudioDevice throws a RuntimeException 
							// "-1, possibly no available default device"
							IntStream.of(this.synth.getAudioDeviceManager().getDefaultOutputDeviceID()),
							IntStream.range(0, this.synth.getAudioDeviceManager().getDeviceCount())).toArray());
			} catch (RuntimeException e) {
				// fatal
				throw new RuntimeException("Could not find any supported audio devices with a stereo output");
			}
		} else if (!this.isValidDeviceId(deviceId) || this.outputDevice == deviceId) {
			// prints an error or does nothing
			return this.outputDevice;
		}

		// if the synth is still JavaSound-based, probe the new output device early 
		// to provoke a LineUnavailableException, in which case we should try to 
		// switch to PortAudio.
		// there is no point probing the channel on a JPortAudioDevice (which seems 
		// to throw IllegalArgumentException no matter what you probe it with), or 
		// the JSynAndroidAudioDeviceManager (which does not support the JavaSound 
		// classes used for probing)
		if (this.synth.getAudioDeviceManager() instanceof JavaSoundAudioDevice) {
			// check for a working line first (since using PortAudio might change the 
			// number of available channels)
			try {
				// TODO does this also work as expected if the device is currently 
				// listed as having 0 output channels?
				// if (this.synth.getAudioDeviceManager().getMaxOutputChannels(deviceId) == 0) {
				// 	 Engine.printMessage(...);
				// } else {
				this.probeDeviceOutputLine(deviceId, this.sampleRate);
				// all is well, move along to the bottom...
			} catch (LineUnavailableException e) {
				// try portaudio access to the same device -- need get the name of the 
				// old output device and re-select it on the new device manager
				String targetDeviceName = this.getDeviceName(deviceId);
				Engine.printMessage("Output device '" + targetDeviceName + "' did not work with the default audio driver, trying again with PortAudio...");
				try {
					this.usePortAudio(true);
				} catch (RuntimeException ee) {
					throw new RuntimeException(e);
				}
				int newDeviceIdForOldDevice = this.synth.getAudioDeviceManager().getDefaultOutputDeviceID();
				try {
					// TODO also loop through candidates
					newDeviceIdForOldDevice = this.getDeviceIdByName(targetDeviceName, true);
					if (newDeviceIdForOldDevice != deviceId) {
						Engine.printMessage("Note that the device id of '" + targetDeviceName + "' has changed from " + deviceId + " to " + newDeviceIdForOldDevice + ".");
						Engine.printMessage("If output is working as expected, you can safely ignore this message.");
						Engine.printMessage("If something is awry, check the output of Sound.list() *after* the call to Sound.selectOutputDevice()");
					}
				} catch (RuntimeException ee) {
					// probably a generic device name like 'Primary Sound Device'
					Engine.printMessage("Switched to new default output device '" + this.getDeviceName(newDeviceIdForOldDevice) + "'");
				}
				// recursive fun
				return this.selectOutputDevice(newDeviceIdForOldDevice);
			}
		}

		// finally made it to the 'normal' output device selection code
		if (this.checkDeviceHasOutputs(deviceId)) {
			this.outputDevice = deviceId;
			this.startSynth();
		} else {
			Engine.printWarning("audio device '" + this.getDeviceName(deviceId) + "' has no stereo output channel");
		}
		return this.outputDevice;
	}

	protected String getDeviceName(int deviceId) {
		return this.isValidDeviceId(deviceId) ? this.synth.getAudioDeviceManager().getDeviceName(deviceId).trim() : "";
	}

	protected int getDeviceIdByName(String deviceName) {
		for (int i = 0; i < this.synth.getAudioDeviceManager().getDeviceCount(); i++) {
			if (deviceName.equalsIgnoreCase(this.getDeviceName(i))) {
				return i;
			}
		}
		throw new RuntimeException("No audio device with name '" + deviceName + "' found.");
	}

	protected int getDeviceIdByName(String deviceName, boolean fuzzy) {
		try {
			return this.getDeviceIdByName(deviceName);
		} catch (RuntimeException e) {
			if (fuzzy) {
				for (int i = 0; i < this.synth.getAudioDeviceManager().getDeviceCount(); i++) {
					if (this.getDeviceName(i).startsWith(deviceName)) {
						return i;
					}
				}
			}
			throw e;
		}
	}

	protected int selectOutputChannel(int channel) {
		if (channel == -1) {
			// disable multi-channel mode
			this.outputChannel = 0;
			this.multiChannelMode = false;
		} else if (channel < 0 || channel > this.synth.getAudioDeviceManager().getMaxOutputChannels(this.outputDevice)) {
			Engine.printError("Invalid channel #" + channel + ", current output device only has " + this.synth.getAudioDeviceManager().getMaxOutputChannels(this.outputDevice) + " channels");
		} else {
			this.outputChannel = channel;
			this.multiChannelMode = true;
		}
		return this.outputChannel;
	}

	protected String getSelectedInputDeviceName() {
		return this.getDeviceName(this.inputDevice);
	}

	protected String getSelectedOutputDeviceName() {
		return this.getDeviceName(this.outputDevice);
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
		if (!this.addedUnits.contains(generator)) {
			this.synth.add(generator);
			this.addedUnits.add(generator);
		}
	}

	protected void remove(UnitGenerator generator) {
		if (this.addedUnits.contains(generator)) {
			this.synth.remove(generator);
			this.addedUnits.remove(generator);
		}
	}

	protected void connectToOutput(int channel, UnitSource source) {
		this.connectToOutput(channel, source, 0);
	}

	protected void connectToOutput(int channel, UnitSource source, int part) {
		source.getOutput().connect(part, this.volume[channel].inputA, 0);
	}

	protected void disconnectFromOutput(int channel, UnitSource source) {
		this.disconnectFromOutput(channel, source, 0);
	}

	protected void disconnectFromOutput(int channel, UnitSource source, int part) {
		source.getOutput().disconnect(part, this.volume[channel].inputA, 0);
	}

	protected void play(UnitSource source) {
		// add unit to synth
		UnitGenerator generator = source.getUnitGenerator();
		this.add(generator);
		// and connect to output(s)
		for (int i = 0; i < source.getOutput().getNumParts(); i++) {
			this.connectToOutput((this.outputChannel + i) % this.synth.getAudioDeviceManager().getMaxOutputChannels(this.outputDevice), source, i);
				// source.getOutput().connect(i, this.volume[(this.outputChannel + i) % this.synth.getAudioDeviceManager().getMaxOutputChannels(this.outputDevice)].inputA, 0);
			if (this.multiChannelMode) {
				// only add the first (left) channel
				break;
			}
		}
	}

	protected void stop(UnitSource source) {
		if (this.addedUnits.contains(source.getUnitGenerator())) {
			// this is usually just the two-part output of a JSynCircuit, but let's 
			// keep it generic just in case
			for (int i : IntStream.range(0, source.getOutput().getNumParts()).toArray()) {
				source.getOutput().disconnectAll(i);
			}
			// removal happens inside
			this.remove(source.getUnitGenerator());
		}
	}

	/**
	 * Internal helper class for Processing library callbacks
	 */
	public class Callback {
		public void dispose() {
			synth.stop();
			// TODO suppress shutdown messages on Mac, like:
			// JPortAudio: 64-bit
			// requestedFramesPerBuffer = 128, coreAudioBufferSizeFrames = 384
			// ringBufferSize after = 1024
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

	protected static void setModulation(UnitInputPort port, Modulator modulator) {
		if (modulator == null) {
			port.disconnectAll();
		} else {
			port.setValueAdded(true);
			port.connect(modulator.getModulator());
		}
	}

	// static helper methods that do stuff like checking argument values or 
	// printing library messages

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

	protected static void println(String message) {
		PApplet.println(message);
	}

	protected static void println() {
		Engine.println("");
	}

	protected static void printMessage(String message) {
		Engine.println("Sound library: " + message);
	}

	protected static void printWarning(String message) {
		Engine.println("Sound library warning: " + message);
	}

	protected static void printError(String message) {
		Engine.println("Sound library error: " + message);
	}

}
