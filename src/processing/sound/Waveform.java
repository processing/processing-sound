package processing.sound;

import com.jsyn.ports.UnitOutputPort;

import processing.core.PApplet;

/**
 * This is a Waveform analyzer. It returns the waveform of the 
 * of an audio stream the moment it is queried with the analyze()
 * method.
 * 
 * @author icalvin102
 * 
 * @webref sound
 **/
public class Waveform extends Analyzer {

	public float[] data;

	private JSynWaveform waveform;
	
	public PApplet app;
	
	public Waveform(PApplet parent) {
		this(parent, 512);
	}

	/**
	 * @param parent
	 *            typically use "this"
	 * @param samples
	 *            number of waveform samples as an integer (default 512).
	 *            This parameter needs to be a power of 2 (e.g. 16, 32, 64, 128,
	 *            ...).
	 * @webref sound
	 */
	public Waveform(PApplet parent, int samples) {
		super(parent);
		app = parent;
		if (samples < 0 || Integer.bitCount(samples) != 1) {
			// TODO throw RuntimeException?
			Engine.printError("number of waveform frames needs to be a power of 2");
		} else {
			// FFT buffer size is twice the number of frequency bands
			this.waveform = new JSynWaveform(samples);
			this.data = new float[samples];
		}
	}

	protected void removeInput() {
		this.waveform.input.disconnectAll();
		this.input = null;
	}

	protected void setInput(UnitOutputPort input) {
		// superclass makes sure that input unit is actually playing, just connect it
		Engine.getEngine().add(this.waveform);
		this.waveform.input.connect(input);
		this.waveform.start();
	}

	/**
	 * Gets the content of the current audiobuffer from the input source, writes it
	 * into this Waveform's `data` array, and returns it.
	 *
	 * @return the current audiobuffer of the input source. The array has as
	 *         many elements as this Waveform analyzer's number of samples
	 */
	public float[] analyze() {
		return this.analyze(this.data);
	}

	/**
	 * Gets the content of the current audiobuffer from the input source.
	 *
	 * @param value
	 *            an array with as many elements as this Waveform analyzer's number of
	 *            samples
	 * @return the current audiobuffer of the input source. The array has as
	 *         many elements as this Waveform analyzer's number of samples
	 * @webref sound
	 **/
	public float[] analyze(float[] value) {
		if (this.input == null) {
			Engine.printWarning("this Waveform has no sound source connected to it, nothing to analyze");
		}
		this.waveform.calculateWaveform(value);
		return value;
	}

	// Below are just duplicated methods from superclasses which are required
	// for the online reference to build the corresponding pages.

	/**
	 * Define the audio input for the analyzer.
	 * 
	 * @param input
	 *            the input sound source. Can be an oscillator, noise generator,
	 *            SoundFile or AudioIn.
	 * @webref sound
	 **/
	public void input(SoundObject input) {
		super.input(input);
	}
}
