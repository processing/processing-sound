package processing.sound;

import com.jsyn.unitgen.PulseOscillator;

import processing.core.PApplet;

/**
 * This is a simple Pulse oscillator.
 * @webref oscillators
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
	 * @webref pulse
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
	 * @webref pulse
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

	// Below are just duplicated methods from superclasses which are required
	// for the online reference to build the corresponding pages.

	public void play() {
		super.play();
	}

	public void play(float freq, float amp) {
		super.play(freq, amp);
	}

	public void play(float freq, float amp, float add) {
		super.play(freq, amp, add);
	}

	/**
	 * Starts the oscillator.
	 * @param freq The frequency value of the oscillator in Hz.
	 * @param amp The amplitude of the oscillator as a value between 0.0 and 1.0.
	 * @param add Offset the output of the oscillator by given value
	 * @param pos The panoramic position of the oscillator as a float from -1.0 to 1.0.
	 * @webref pulse 
	 * @webBrief Starts the oscillator
	 **/
	public void play(float freq, float amp, float add, float pos) {
		super.play(freq, amp, add, pos);
	}

	public void set(float freq, float amp, float add, float pos) {
		super.set(freq, amp, add, pos);
	}
	
	/**
	 * Changes the frequency of the pulse oscillator in Hz.
	 * @webref pulse
	 * @webBrief Changes the frequency of the pulse oscillator in Hz.
	 * @param freq A floating point value of the oscillator in Hz.
	 **/
	public void freq(float freq) {
		super.freq(freq);
	}

	/**
	 * Changes the amplitude/volume of the pulse oscillator. Allowed values are between 0.0 and 1.0.
	 *
	 * @webref pulse
	 * @webBrief Changes the amplitude/volume of the pulse oscillator.
	 * @param amp
	 *            A float value between 0.0 (complete silence) and 1.0 (full volume)
	 *            controlling the amplitude/volume of this sound.
	 **/
	public void amp(float amp) {
		super.amp(amp);
	}

	/**
	 * Offset the output of this generator by given value.
	 *
	 * @webref pulse
	 * @webBrief Offset the output of this generator by given value.
	 * @param add Offset the output of the oscillator by given value
	 **/
	public void add(float add) {
		super.add(add);
	}

	/**
	 * Pan the oscillator in a stereo panorama. -1.0 pans to the left channel and 1.0 to the right channel.
	 *
	 * @webref pulse
	 * @webBrief Pan the oscillator in a stereo panorama.
	 * @param pos
	 *            The panoramic position of this sound unit as a float from -1.0
	 *            (left) to 1.0 (right).
	 **/
	public void pan(float pos) {
		super.pan(pos);
	}

	/**
	 * Stops the Sine Oscillator generator.
	 *
	 * @webref pulse
	 * @webBrief Stops the Sine Oscillator generator.
	 **/
	public void stop() {
		super.stop();
	}
}
