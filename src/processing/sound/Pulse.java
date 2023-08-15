package processing.sound;

import com.jsyn.unitgen.PulseOscillator;

import processing.core.PApplet;

/**
 * This is a simple Pulse oscillator.
 * @webref Oscillators:Pulse
 * @webBrief This is a simple Pulse oscillator.
 **/
public class Pulse extends Oscillator<PulseOscillator> {

	/**
	 * @param parent typically use "this"
	 */
	public Pulse(PApplet parent) {
		super(parent, new PulseOscillator());
	}

	/**
	 * Changes the pulse width of the pulse oscillator. Allowed values are between 0.0 and 1.0.
	 * 
	 * @webref Oscillators:Pulse
	 * @webBrief Changes the pulse width of the pulse oscillator.
	 * @param width
	 *            The relative pulse width of the oscillator as a float value
	 *            between 0.0 and 1.0 (exclusive)
	 **/
	public void width(float width) {
		this.oscillator.width.set(width);
	}

	/**
	 * Set multiple parameters at once
	 * 
	 * @webref Oscillators:Pulse
	 * @param freq
	 *            The frequency value of the oscillator in Hz.
	 * @param width
	 *            The pulse width of the oscillator as a value between 0.0 and 1.0.
	 * @param amp
	 *            The amplitude of the oscillator as a value between 0.0 and 1.0.
	 * @param add Offset the output of the oscillator by given value
	 * @param pos
	 *            The panoramic position of the oscillator as a float from -1.0 to
	 *            1.0.
	 **/
	public void set(float freq, float width, float amp, float add, float pos) {
		this.width(width);
		this.set(freq, amp, add, pos);
	}

}
