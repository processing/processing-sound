package processing.sound;

import java.util.Arrays;

import com.jsyn.data.FloatSample;
import com.jsyn.unitgen.FixedRateMonoWriter;

/**
 * This class copies all input to an audio buffer of the given size and returns it.
 * 
 * @author icalvin102
 */
class JSynWaveform extends FixedRateMonoWriter {

	private FloatSample buffer;

	protected JSynWaveform(int bufferSize) {
		super();
		this.buffer = new FloatSample(bufferSize);

		// write any connected input into the output buffer ad infinitum
		this.dataQueue.queueLoop(this.buffer);
	}

	protected void calculateWaveform(float[] target) {
		// get position currently being written to
		int pos = (int) this.dataQueue.getFrameCount() % this.buffer.getNumFrames();
		for (int i = 0; i < this.buffer.getNumFrames(); i++) {
			target[i] = (float)(2*this.buffer.readDouble((pos + i) % this.buffer.getNumFrames()));
		}
	}
}
