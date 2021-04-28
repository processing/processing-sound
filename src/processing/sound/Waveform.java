package processing.sound;

import com.jsyn.data.FloatSample;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.unitgen.FixedRateMonoWriter;

import processing.core.PApplet;

/**
 * This is a Waveform analyzer. It returns the waveform of the 
 * of an audio stream the moment it is queried with the <b>analyze()</b>
 * method.
 * 
 * @author icalvin102
 * 
 * @webref analysis
 * @webBrief This is a Waveform analyzer.
 **/
public class Waveform extends Analyzer {

	public float[] data;

	private FixedRateMonoWriter writer;
	private FloatSample buffer;
	private int lastAnalysisOffset;
	
	/**
	 * @param parent
	 *            typically use "this"
	 * @param nsamples
	 *            number of waveform samples that you want to be able to read at once (a positive integer).
	 */
	public Waveform(PApplet parent, int nsamples) {
		super(parent);
		if (nsamples <= 0) {
			// TODO throw RuntimeException?
			Engine.printError("number of waveform frames needs to be greater than 0");
		} else {
			this.data = new float[nsamples];

			this.writer = new FixedRateMonoWriter();
			this.buffer = new FloatSample(nsamples);
			// write any connected input into the output buffer ad infinitum
			this.writer.dataQueue.queueLoop(this.buffer);
		}
	}

	protected void removeInput() {
		this.writer.input.disconnectAll();
		this.input = null;
	}

	protected void setInput(UnitOutputPort input) {
		// superclass makes sure that input unit is actually playing, just connect it
		Engine.getEngine().add(this.writer);
		this.writer.input.connect(input);
		this.writer.start();
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
	 * @webref waveform
	 * @webBrief Gets the content of the current audiobuffer from the input source.
	 **/
	public float[] analyze(float[] value) {
		if (this.input == null) {
			Engine.printWarning("this Waveform has no sound source connected to it, nothing to analyze");
		}

		this.lastAnalysisOffset = (int) this.writer.dataQueue.getFrameCount() % this.buffer.getNumFrames();
		// if initiating this read takes too long the first couple samples might actually
		// already be overwritten by the next loop, so fingers crossed...
		this.buffer.read(lastAnalysisOffset, value, 0, this.buffer.getNumFrames() - lastAnalysisOffset);
		this.buffer.read(0, value, this.buffer.getNumFrames() - lastAnalysisOffset, lastAnalysisOffset);
		// the original implementation did a *2 on all values...?
		return value;
	}

/*
	public float[] analyzeCircular() {
		return this.analyzeCircular(this.data);
	}

	public float[] analyzeCircular(float[] value) {
		if (this.input == null) {
			Engine.printWarning("this Waveform has no sound source connected to it, nothing to analyze");
		}

		this.lastAnalysisOffset = (int) this.writer.dataQueue.getFrameCount() % this.buffer.getNumFrames();
		this.buffer.read(value);
		return value;
	}

	public int getLastAnalysisOffset() {
		return this.lastAnalysisOffset;
	}
*/

	// Below are just duplicated methods from superclasses which are required
	// for the online reference to build the corresponding pages.

	/**
	 * Define the audio input for the analyzer.
	 * 
	 * @param input
	 *            the input sound source. Can be an oscillator, noise generator,
	 *            SoundFile or AudioIn.
	 * @webref waveform
	 * @webBrief Define the audio input for the analyzer.
	 **/
	public void input(SoundObject input) {
		super.input(input);
	}
}
