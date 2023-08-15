package processing.sound;

import com.jsyn.unitgen.SineOscillator;

import processing.core.PApplet;

/**
 * This is a simple Sine Wave Oscillator.
 *
 * @webref Oscillators:SinOsc
 * @webBrief This is a simple Sine Wave Oscillator.
 **/
public class SinOsc extends Oscillator<SineOscillator> {

	/**
	 * @param parent
	 *            typically use "this"
	 */
	public SinOsc(PApplet parent) {
		super(parent, new SineOscillator());
	}

}
