package processing.sound;

import java.util.Arrays;

import com.jsyn.data.FloatSample;
import com.jsyn.unitgen.FixedRateMonoWriter;

/**
 * This class copies all input to an audio buffer of the given size and performs
 * an FFT on it when required.
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

	protected void calculateMagnitudes(float[] target) {
		// get position currently being written to
		int pos = (int) this.dataQueue.getFrameCount() % this.buffer.getNumFrames();
		this.buffer.read(pos, this.real, 0, this.buffer.getNumFrames() - pos);
		this.buffer.read(0, this.real, this.buffer.getNumFrames() - pos, pos);
		FFT.calculateMagnitudesFromSample(this.real, this.imaginary, target);
	}

}
