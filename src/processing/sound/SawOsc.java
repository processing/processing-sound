package processing.sound;

import com.jsyn.unitgen.SawtoothOscillator;

import processing.core.PApplet;

/**
 * This is a simple Saw Wave Oscillator.
 * @webref oscillators
 * @webBrief This is a simple Saw Wave Oscillator.
 **/
public class SawOsc extends Oscillator<SawtoothOscillator> {

	/**
	 * @param parent typically use "this"
	 */
	public SawOsc(PApplet parent) {
		super(parent, new SawtoothOscillator());
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
	 * @webref sawosc
	 * @webBrief Starts the oscillator. 
	 **/
	public void play(float freq, float amp, float add, float pos) {
		super.play(freq, amp, add, pos);
	}

	/**
	 * Set multiple parameters at once.
	 * @webref sawosc
	 * @webBrief Set multiple parameters at once.
	 * @param freq The frequency value of the oscillator in Hz.
	 * @param amp The amplitude of the oscillator as a value between 0.0 and 1.0.
	 * @param add Offset the output of the oscillator by given value
	 * @param pos The panoramic position of the oscillator as a float from -1.0 to 1.0.
	 **/
	public void set(float freq, float amp, float add, float pos) {
		super.set(freq, amp, add, pos);
	}
	
	/**
	 * Set the frequency of the oscillator in Hz.
	 * @webref sawosc
	 * @webBrief Set the frequency of the oscillator in Hz.
	 * @param freq A floating point value of the oscillator in Hz.
	 **/
	public void freq(float freq) {
		super.freq(freq);
	}

	/**
	 * Changes the amplitude/volume of the saw oscillator. Allowed values are between 0.0 and 1.0.
	 *
	 * @webref sawosc
	 * @webBrief Changes the amplitude/volume of the saw oscillator.
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
	 * @webref sawosc
	 * @webBrief Offset the output of this generator by given value.
	 * @param add Offset the output of the oscillator by given value
	 **/
	public void add(float add) {
		super.add(add);
	}

	/**
	 * Move the sound in a stereo panorama.
	 *
	 * @webref sawosc
	 * @webBrief Move the sound in a stereo panorama.
	 * @param pos Move the sound in a stereo panorama.
	 *            The panoramic position of this sound unit as a float from -1.0
	 *            (left) to 1.0 (right).
	 **/
	public void pan(float pos) {
		super.pan(pos);
	}

	/**
	 * Stop the oscillator.
	 *
	 * @webref sawosc
	 * @webBrief Move the sound in a stereo panorama.
	 **/
	public void stop() {
		super.stop();
	}
}
