package processing.sound;

import com.jsyn.unitgen.FilterHighPass;

import processing.core.PApplet;

/**
 * This is a high pass filter
 * @sound webref
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
	 * Set the cut off frequency for the filter
	 * @webref sound
	 * @param freq the cutoff frequency in Hertz
	 */
	public void freq(float freq) {
		this.left.frequency.set(freq);
		this.right.frequency.set(freq);
	}

	public void process(SoundObject input, float freq) {
		this.freq(freq);
		this.process(input);
	}
}
