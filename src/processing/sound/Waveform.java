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
	
	/**
	 * @param parent
	 *            typically use "this"
	 * @param nsamples
	 *            number of waveform samples that you want to be able to read at once (a positive integer).
	 * @webref sound
	 */
	public Waveform(PApplet parent, int nsamples) {
		super(parent);
		if (nsamples <= 0) {
			// TODO throw RuntimeException?
			Engine.printError("number of waveform frames needs to be greater than 0");
		} else {
			this.waveform = new JSynWaveform(nsamples);
			this.data = new float[nsamples];
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
