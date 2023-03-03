package processing.sound;

import java.util.Arrays;

import com.jsyn.data.FloatSample;
import com.jsyn.unitgen.FixedRateMonoWriter;
import com.softsynth.math.FourierMath;

/**
 * This class copies all input to an audio buffer of the given size and performs
 * an FFT on it when required.
 * @author kevin
 */
class JSynFFT extends FixedRateMonoWriter {

	private FloatSample buffer;
	private double[] real;
	private double[] imaginary;
	private double[] magnitude;

	protected JSynFFT(int bufferSize) {
		super();
		this.buffer = new FloatSample(bufferSize);
		this.real = new double[bufferSize];
		this.imaginary = new double[bufferSize];
		this.magnitude = new double[bufferSize / 2];

		// write any connected input into the output buffer ad infinitum
		this.dataQueue.queueLoop(this.buffer);
	}

	protected void calculateMagnitudes(float[] target) {
		// get position currently being written to
		int pos = (int) this.dataQueue.getFrameCount() % this.buffer.getNumFrames();
		for (int i = 0; i < this.buffer.getNumFrames(); i++) {
			// TODO could apply window?
			this.real[i] = this.buffer.readDouble((pos + i) % this.buffer.getNumFrames());
		}
		Arrays.fill(this.imaginary, 0);
		FourierMath.fft(this.real.length, this.real, this.imaginary);
		FourierMath.calculateMagnitudes(this.real, this.imaginary, this.magnitude);

		for (int i = 0; i < target.length; i++) {
			target[i] = (float) (2 * this.magnitude[i]);
		}
	}

	protected void calculateMagnitudesFromSample(float[] sample, float[] target) {

		double[] real = new double[sample.length];
		double[] imaginary = new double[sample.length];
		double[] magnitude = new double[target.length];
		Arrays.fill(imaginary, 0);

		for (int i = 0; i < sample.length; i++) {
			real[i] = sample[i];
		}

		FourierMath.fft(target.length, real, imaginary);
		FourierMath.calculateMagnitudes(real, imaginary, magnitude);

		for (int i = 0; i < target.length; i++) {
			target[i] = (float) magnitude[i];
		}
	}
}
