package processing.sound;

import processing.core.PApplet;

/**
 * Brown noise (also called red noise) has higher energy at lower frequencies. Its power density
 * decreases 6dB per octave.
 * 
 * Please be aware that, because most of its power resides in the bass frequencies, the subjective
 * loudness of brown noise relative to other sounds can vary dramatically depending on how well
 * your sound system can reproduce low frequency sounds!
 * @webref Noise:BrownNoise
 * @webBrief Brown noise (also called red noise) has higher energy at lower frequencies.
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

	/**
	 * {@inheritDoc}
	 */
	public void amp(float amp) {
		// the JSyn Brownian noise generator can drift to exceed one, so tone down the volume a bit
		super.amp(amp / 16);
	}

}
