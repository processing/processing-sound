package processing.sound;

import com.jsyn.ports.UnitOutputPort;

import processing.core.PApplet;

/**
 * This is a Fast Fourier Transform (FFT) analyzer. It calculates the normalized
 * power spectrum of an audio stream the moment it is queried with the analyze()
 * method.
 * 
 * @webref Analysis:FFT
 * @webBrief This is a Fast Fourier Transform (FFT) analyzer.
 **/
public class FFT extends Analyzer {

	public float[] spectrum;

	private JSynFFT fft;

	public FFT(PApplet parent) {
		this(parent, 512);
	}

	/**
	 * @param parent
	 *            typically use "this"
	 * @param bands
	 *            number of frequency bands for the FFT as an integer. This 
	 *            parameter needs to be a power of 2 (e.g. 16, 32, 64, 128,
	 *            ...). The default is 512.
	 */
	public FFT(PApplet parent, int bands) {
		super(parent);
		if (bands < 0 || Integer.bitCount(bands) != 1) {
			// TODO throw RuntimeException?
			Engine.printError("number of FFT bands needs to be a power of 2");
		} else {
			// FFT buffer size is twice the number of frequency bands
			this.fft = new JSynFFT(2 * bands);
			this.spectrum = new float[bands];
		}
	}

	protected void removeInput() {
		this.fft.input.disconnectAll();
		this.input = null;
	}

	protected void setInput(UnitOutputPort input) {
		// superclass makes sure that input unit is actually playing, just connect it
		Engine.getEngine().add(this.fft);
		this.fft.input.connect(input);
		this.fft.start();
	}

	public float[] analyze() {
		return this.analyze(this.spectrum);
	}

	/**
	 * Calculates the current frequency spectrum of the input source and returns
	 * it as an array with as many elements as frequency bands.
	 *
	 * @param value
	 *            an array with as many elements as this FFT analyzer's number of
	 *            frequency bands
	 * @return The current frequency spectrum of the input source. The array has as
	 *         many elements as this FFT analyzer's number of frequency bands.
	 * @webref Analysis:FFT
	 * @webBrief Calculates the current frequency spectrum of the audio input 
	 * signal.
	 **/
	public float[] analyze(float[] value) {
		if (this.input == null) {
			Engine.printWarning("this FFT has no sound source connected to it, nothing to analyze");
		}
		this.fft.calculateMagnitudes(value);
		return value;
	}

	/**
	 * Calculates the frequency spectrum of the given audio sample and returns an 
	 * array of magnitudes, one for each frequency band.
	 *
	 * This version is intended to be used in non-real time processing, particularly when you are
	 * creating an animation in non-real time and want to get the FFT for a particular chunk of an audio sample.
	 *
	 * For stereo samples, you can call this function once for each channel, so you can display the left and right
	 * fft values separately.
	 *
	 * @param sample
	 *            an array with sound samples
	 * @param numBands
	 *            the number of fft bands requested. Must be a power of 2 (one of 2, 4, 8, 16 etc.)
	 * @return The current frequency spectrum of the input source. The array has as
	 *         many elements as this FFT analyzer's number of frequency bands.
	 * @webref Analysis:FFT
	 * @webBrief Calculates the frequency spectrum of the given audio sample.
	 **/
	public float[] analyzeSample(float[] sample, int numBands) {
		float[] target = new float[numBands];
		fft.calculateMagnitudesFromSample(sample, target);
		return target;
	}

}
