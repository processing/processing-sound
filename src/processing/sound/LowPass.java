package processing.sound;

import com.jsyn.unitgen.FilterLowPass;

import processing.core.PApplet;

/**
 * This is a low pass filter.
 * @webref Effects:LowPass
 * @param parent PApplet: typically use "this"
 **/
public class LowPass extends Filter<FilterLowPass> {

	public LowPass(PApplet parent) {
		super(parent);
	}

	@Override
	protected FilterLowPass newInstance() {
		return new FilterLowPass();
	}
}
