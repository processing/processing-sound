package processing.sound;

import processing.core.PApplet;

/**
 * This is a brown noise generator. Brown noise has a decrease of 6db per octave.
 * @webref noise
 * @webBrief This is a brown noise generator.
 * @param parent typically use "this"	
 **/
public class BrownNoise extends Noise<com.jsyn.unitgen.BrownNoise> {

	/**
	 * @param parent typically use "this"	
	 */
	public BrownNoise(PApplet parent) {
		super(parent, new com.jsyn.unitgen.BrownNoise());
		this.amplitude = this.noise.amplitude;
		// explicitly set amplitude to override default (see amp() below)
		this.amp(1.0f);
	}

	// Below are just duplicated methods from the Noise and SoundObject superclass which
	// are required for the reference to build the corresponding pages.

	public void play() {
		super.play();
	}

	public void play(float amp) {
		super.play(amp);
	}
	
	public void play(float amp, float pos) {
		super.play(amp, pos);
	}

	/**
	 * Start the generator.
	 * @param amp the amplitude of the noise as a value from 0.0 (complete silence) to 1.0 (full volume)
	 * @param add offset the output of the noise by given value
	 * @param pos pan the generator in stereo panorama. Allowed values are between -1.0 and 1.0.
	 * @webref brownnoise
	 * @webBrief Start the generator.
	 **/
	public void play(float amp, float add, float pos) {
		super.play(amp, add, pos);
	}

	/**
	 * Sets <b>amplitude</b>, <b>add</b> and <b>pan</b> position with one method. 
	 * @param amp the amplitude of the noise as a value from 0.0 (complete silence) to 1.0 (full volume)
	 * @param add offset the output of the noise by given value
	 * @param pos pan the generator in stereo panorama. Allowed values are between -1.0 and 1.0.
	 * @webref brownnoise
	 * @webBrief Sets amplitude, add and pan position with one method. 
	 **/
	public void set(float amp, float add, float pos) {
		super.set(amp, add, pos);
	}

	/**
	 * Changes the amplitude/volume of the noise generator. Allowed values are between 0.0 and 1.0.
	 * @param amp the amplitude of the noise as a value from 0.0 (complete silence) to 1.0 (full volume)
	 * @webref brownnoise
	 * @webBrief Changes the amplitude/volume of the noise generator.
	 **/
	public void amp(float amp) {
		// the JSyn Brownian noise generator can drift to exceed one, so tone down the volume a bit
		super.amp(amp / 4);
	}

	/**
	 * Offset the output of this generator by a fixed value.
	 * @param add offset the output of the generator by the given value
	 * @webref brownnoise
	 * @webBrief Offset the output of this generator by a fixed value.
	 **/
	public void add(float add) {
		super.add(add);
	}

	/**
	 * Pan the generator in a stereo panorama. -1.0 pans to the left channel and 1.0 to the right channel.
	 * @param pos
	 *            the panoramic position of this sound unit from -1.0 (left) to 1.0
	 *            (right).
	 * @webref brownnoise
	 * @webBrief Pan the generator in a stereo panorama.
	 **/
	public void pan(float pos) {
		super.pan(pos);
	}

	/**
	 * Stop the generator
	 * @webref brownnoise
	 * @webBrief Stops the Brown Noise generator.
	 **/
	public void stop() {
		super.stop();
	}

}
