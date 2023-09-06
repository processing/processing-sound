package processing.sound;

import com.jsyn.unitgen.UnitGenerator;
import com.jsyn.unitgen.UnitSource;

import processing.core.PApplet;

/**
 * Common superclass of all noise generators
 * @webref Noise
 */
public abstract class Noise<JSynNoise extends UnitGenerator> extends SoundObject {

	protected JSynNoise noise;

	protected Noise(PApplet theParent, JSynNoise noise) {
		super(theParent);
		this.noise = noise;
		this.circuit = new JSynCircuit(((UnitSource) this.noise).getOutput());
	}

	public void play(float amp) {
		this.amp(amp);
		this.play();
	}

	/**
	 * Starts the noise
	 * @webref Noise:Noise
	 * @param amp The amplitude of the noise as a value between 0.0 and 1.0.
	 * @param pos The panoramic position of the noise as a float from -1.0 to 1.0.
	 **/
	public void play(float amp, float pos) {
		this.pan(pos);
		this.play(amp);
	}

	public void play(float amp, float add, float pos) {
		this.set(amp, add, pos);
		this.play();
	}

	/**
	 * Set the amplitude and panoramic position with one method.
	 * @webref Noise:Noise
	 * @param amp The amplitude of the noise as a value between 0.0 and 1.0.
	 * @param pos The panoramic position of the noise as a float from -1.0 to 1.0.
	 **/
	public void set(float amp, float pos) {
		this.amp(amp);
		this.pan(pos);
	}

	/**
	 * @deprecated
	 */
	public void set(float amp, float add, float pos) {
		this.amp(amp);
		this.add(add);
		this.pan(pos);
	}

	/**
	 * Stop the noise from playing back
	 * @webref Noise:Noise
	 */
	public void stop() {
		super.stop();
	}

}
