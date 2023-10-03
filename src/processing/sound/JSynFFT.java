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
	private float[] real;
	private float[] imaginary;

	protected JSynFFT(int bufferSize) {
		super();
		this.buffer = new FloatSample(bufferSize);
		this.real = new float[bufferSize];
		this.imaginary = new float[bufferSize];

		// write any connected input into the output buffer ad infinitum
		this.dataQueue.queueLoop(this.buffer);
	}

	// TODO check that target is half size of the buffer?
	protected void calculateMagnitudes(float[] target) {
		// get position currently being written to
		int pos = (int) this.dataQueue.getFrameCount() % this.buffer.getNumFrames();
		// TODO could apply smoothing window?
		this.buffer.read(pos, this.real, 0, this.buffer.getNumFrames() - pos);
		this.buffer.read(0, this.real, this.buffer.getNumFrames() - pos, pos);
		FourierMath.transform(1, this.real.length, this.real, this.imaginary);
		FourierMath.calculateMagnitudes(this.real, this.imaginary, target);
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
