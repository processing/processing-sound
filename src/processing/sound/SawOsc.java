package processing.sound;

import com.jsyn.unitgen.SawtoothOscillator;

import processing.core.PApplet;

/**
 * This is a simple Saw Wave Oscillator.
 * @webref Oscillators:SawOsc
 * @webBrief This is a simple Saw Wave Oscillator.
 **/
public class SawOsc extends Oscillator<SawtoothOscillator> {

	/**
	 * @param parent typically use "this"
	 */
	public SawOsc(PApplet parent) {
		super(parent, new SawtoothOscillator());
	}

}
