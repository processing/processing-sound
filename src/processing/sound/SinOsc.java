package processing.sound;

import com.jsyn.unitgen.SineOscillator;

import processing.core.PApplet;

/**
 * This is a simple Sine Wave Oscillator.
 *
 * @webref oscillators
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
	 *
	 * @param freq
	 *            The frequency value of the oscillator in Hz.
	 * @param amp
	 *            The amplitude of the oscillator as a value between 0.0 and 1.0.
	 * @param add
	 *            Offset the output of the oscillator by given value
	 * @param pos
	 *            The panoramic position of the oscillator as a float from -1.0 to
	 *            1.0.
	 * @webref sinosc
	 * @webBrief Starts the oscillator.
	 **/
	public void play(float freq, float amp, float add, float pos) {
		super.play(freq, amp, add, pos);
	}

	/**
	 * Set multiple parameters at once.
	 *
	 * @webref sinosc
	 * @webBrief Set multiple parameters at once.
	 * @param freq
	 *            The frequency value of the oscillator in Hz.
	 * @param amp
	 *            The amplitude of the oscillator as a value between 0.0 and 1.0.
	 * @param add
	 *            Offset the output of the oscillator by given value
	 * @param pos
	 *            The panoramic position of the oscillator as a float from -1.0 to
	 *            1.0.
	 **/
	public void set(float freq, float amp, float add, float pos) {
		super.set(freq, amp, add, pos);
	}

	/**
	 * Set the frequency of the oscillator in Hz.
	 *
	 * @webref sinosc
	 * @webBrief Set the frequency of the oscillator in Hz.
	 * @param freq
	 *            A floating point value of the oscillator in Hz.
	 **/
	public void freq(float freq) {
		super.freq(freq);
	}

	/**
	 * Changes the amplitude/volume of the sine oscillator. Allowed values are between 0.0 and 1.0.
	 *
	 * @webref sinosc
	 * @webBrief Changes the amplitude/volume of the sine oscillator.
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
	 * @webref sinosc
	 * @webBrief Offset the output of this generator by given value.
	 * @param add
	 *            Offset the output of the oscillator by given value
	 **/
	public void add(float add) {
		super.add(add);
	}

	/**
	 * Pan the oscillator in a stereo panorama. -1.0 pans to the left channel and 1.0 to the right channel.
	 *
	 * @webref sinosc
	 * @webBrief Pan the oscillator in a stereo panorama.
	 * @param pos
	 *            the panoramic position of this sound unit from -1.0 (left) to 1.0
	 *            (right).
	 **/
	public void pan(float pos) {
		super.pan(pos);
	}

	/**
	 * Stop the oscillator.
	 *
	 * @webref sinosc
	 * @webBrief Stops the oscillator.
	 **/
	public void stop() {
		super.stop();
	}
}
