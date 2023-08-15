package processing.sound;

import com.jsyn.unitgen.TriangleOscillator;

import processing.core.PApplet;

/**
 * This is a simple triangle wave oscillator.
 * @webref Oscillators:TriOsc
 * @webBrief This is a simple triangle wave oscillator.
 **/
public class TriOsc extends Oscillator<TriangleOscillator> {

	/**
	 * @param parent typically use "this"
	 */
	public TriOsc(PApplet parent) {
		super(parent, new TriangleOscillator());
	}

}
