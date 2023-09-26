package processing.sound;

import com.jsyn.unitgen.FilterAllPass;

import processing.core.PApplet;

/**
 * This is an all pass filter. For signals processed, all frequencies hold the 
 * same amplitude but have their phase relationship modified using a delayline 
 * of one sample,<br />
 * <br />
 * <code>y(k) = -z * x(k) + x(k - 1) + z * y(k - 1)</code><br />
 * <br />
 * where <code>y</code> is the output, <code>x</code> is the input, 
 * <code>z</code> is the gain coefficient, and <code>k</code> is the signal.
 * @webref Effects:AllPass
 * @webBrief Outputs all input frequencies at the same amplitude but changes 
 * their phase relationship.
 * @param parent PApplet: typically use "this"
 **/
public class AllPass extends Effect<FilterAllPass> {

	public AllPass(PApplet parent) {
		super(parent);
	}

	@Override
	protected FilterAllPass newInstance() {
		return new FilterAllPass();
	}

	/**
	 * Set the gain for the filter.
	 * @webref Effects:AllPass
	 * @webBrief Set the gain for the filter. Takes float from 0.0 - 1.0 where larger values increase phase displacement.
	 * @param g Phase displacement as float 0.0 - 1.0
	 **/
	public void gain(float g) {
		// Keep the user from throwing bad output signals.
		if (g < 0.0) { g = 0.0f; }
		if (g > 1.0) { g = 1.0f; }

		//Set the gain of the effect.
		this.left.gain.set(g);
		this.right.gain.set(g);
	}

	public void process(SoundObject input, float g) {
		this.gain(g);
		this.process(input);
	}
}
