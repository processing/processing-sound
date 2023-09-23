package processing.sound;

import com.jsyn.unitgen.FilterHighPass;

import processing.core.PApplet;

/**
 * This is a high pass filter.
 * @webref Effects:HighPass
 * @webBrief This is a high pass filter.
 * @param parent PApplet: typically use "this"
 **/
public class HighPass extends Filter<FilterHighPass> {

	public HighPass(PApplet parent) {
		super(parent);
	}

	@Override
	protected FilterHighPass newInstance() {
		return new FilterHighPass();
	}
}
