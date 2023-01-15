package processing.sound;

import com.jsyn.unitgen.FilterAllPass;

import processing.core.PApplet;

/**
 * This is a all pass filter.
 * @webref effects
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

	public void gain(float g) {
		this.left.gain.set(g);
		this.right.gain.set(g);
	}

	public void process(SoundObject input, float g) {
		this.gain(g);
		this.process(input);
	}
}
