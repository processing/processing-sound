package processing.sound;

import com.jsyn.unitgen.SquareOscillator;

import processing.core.PApplet;

/**
 * This is a simple Square Wave Oscillator.
 * @webref Oscillators:SqrOsc
 * @webBrief This is a simple Square Wave Oscillator.
 **/
public class SqrOsc extends Oscillator<SquareOscillator> {

	/**
	 * @param parent typically use "this"
	 */
	public SqrOsc(PApplet parent) {
		super(parent, new SquareOscillator());
	}

}
