package processing.sound;

import com.jsyn.unitgen.UnitGenerator;
import com.jsyn.unitgen.UnitSource;

import processing.core.PApplet;

/**
 * For advanced users: common superclass of all noise generators
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

	public void play(float amp, float pos) {
		this.pan(pos);
		this.play(amp);
	}

	public void play(float amp, float add, float pos) {
		this.set(amp, add, pos);
		this.play();
	}

	public void set(float amp, float add, float pos) {
		this.amp(amp);
		this.add(add);
		this.pan(pos);
	}
}
