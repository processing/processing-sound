package processing.sound;

import com.jsyn.unitgen.FilterHighPass;

import processing.core.PApplet;

/**
 * This is a high pass filter.
 * @webref Effects:HighPass
 * @webBrief This is a high pass filter.
 * @param parent PApplet: typically use "this"
 **/
public class HighPass extends Effect<FilterHighPass> {

	public HighPass(PApplet parent) {
		super(parent);
	}

	@Override
	protected FilterHighPass newInstance() {
		return new FilterHighPass();
	}

	/**
	 * Set the cut off frequency for the filter.
	 * @webref Effects:HighPass
	 * @webBrief Set the cut off frequency for the filter.
	 * @param freq the cutoff frequency in Hertz
	 */
	public void freq(float freq) {
		this.left.frequency.set(freq);
		this.right.frequency.set(freq);
	}

	/**
	 * Start applying this highpass filter to an input signal.
	 * @webref Effects:HighPass
	 * @param input the sound source to apply the filter to
	 * @param freq cutoff frequency
	 **/
	public void process(SoundObject input, float freq) {
		this.freq(freq);
		this.process(input);
	}
}
