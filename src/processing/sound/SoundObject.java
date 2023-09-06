package processing.sound;

import com.jsyn.ports.UnitInputPort;
import com.jsyn.unitgen.UnitFilter;

import processing.core.PApplet;

/**
 * For advanced users: common superclass of all sound sources (oscillators,
 * noise, audio samples and even AudioIn).
 * @webref SoundObject
 */
// Subclasses need to assign the 'amplitude' port, and also initiate a
// JSynCircuit (which effects can be plugged into) with an appropriate
// JSynProcessor if they want to support pan/add.
public abstract class SoundObject {

	// subclasses need to initialise this circuit
	protected JSynCircuit circuit;

	// all subclasses need to set this amplitude port -- either to the amplitude
	// port of the circuit, or directly to an amplitude port of their sound unit
	protected UnitInputPort amplitude;

	protected float amp = 1.0f;
	protected boolean isPlaying = false;

	protected SoundObject(PApplet parent) {
		Engine.getEngine(parent);
	}

	private void setAmplitude() {
		this.amplitude.set(this.amp);
	}

	/*
	 * Offset the output of this generator by given value
	 *
	 * @param add
	 *            A value for offsetting the audio signal.
	 * @deprecated
	 */
	public void add(float add) {
		if (this.circuit.processor == null) {
			Engine.printError("stereo sound sources do not support adding");
		} else {
			this.circuit.processor.add(add);
		}
	}

	/**
	 * Change the amplitude/volume of this sound.
	 *
	 * @param amp
	 *            A float value between 0.0 (complete silence) and 1.0 (full volume)
	 *            controlling the amplitude/volume of this sound.
	 * @webref SoundObject:SoundObject
	 **/
	public void amp(float amp) {
		if (Engine.checkAmp(amp)) {
			this.amp = amp;
			if (this.isPlaying()) {
				this.setAmplitude();
			}
		}
	}

	/**
	 * Check if this sound object is currently playing.
	 *
	 * @return `true` if this sound object is currently playing, `false` if it is
	 *         not.
	 */
	public boolean isPlaying() {
		return this.isPlaying;
	}

	/**
	 * Move the sound in a stereo panorama.
	 *
	 * @param pos
	 *            The panoramic position of this sound unit as a float from -1.0
	 *            (left) to 1.0 (right).
	 * @webref SoundObject:SoundObject
	 **/
	public void pan(float pos) {
		if (this.circuit.processor == null) {
			Engine.printError("stereo sound sources do not support panning");
		} else if (Engine.checkPan(pos)) {
			this.circuit.processor.pan(pos);
		}
	}

	/**
	 * Starts the generator
	 **/
	public void play() {
		// TODO print info message if it's already playing?
		if (!this.isPlaying) {
			Engine.getEngine().add(this.circuit);
			Engine.getEngine().play(this.circuit);
			this.setAmplitude();
			this.isPlaying = true;
			// TODO rewire effect if one was set previously (before stopping)?
		}
	}

	/**
	 * Stops this sound from playing back.
	 *
	 * @webref SoundObject:SoundObject
	 **/
	public void stop() {
		this.isPlaying = false;
		this.amplitude.set(0);
		if (this.circuit.effect != null) {
			this.removeEffect(this.circuit.effect);
		}
		Engine.getEngine().stop(this.circuit);
		Engine.getEngine().remove(this.circuit);
	}


	/**
	 * The 'true' number of underlying channels of this sound. All SoundObjects are put into
	 * a stereo-pannable wrapper, but for multi-channel purposes, anything that's not a true
	 * stereo sample should be considered to be mono.
	 * @see MultiChannel
	 */
	public int channels() {
		return 1;
	}

	protected void setEffect(Effect<? extends UnitFilter> effect) {
		if (this.circuit.effect == effect) {
			Engine.printWarning("this effect is already processing the given sound source");
		} else {
			if (this.circuit.effect != null) {
				this.removeEffect(this.circuit.effect);
			}

			Engine.getEngine().add(effect.left);
			Engine.getEngine().add(effect.right);
			this.circuit.setEffect(effect);
		}
	}

	protected void removeEffect(Effect<? extends UnitFilter> effect) {
		if (this.circuit.effect != effect) {
			// possibly a previous effect that's being stopped here, ignore call
			Engine.printError("this effect is not currently processing any signals.");

		} else {
			this.circuit.removeEffect();
		}
	}
}
