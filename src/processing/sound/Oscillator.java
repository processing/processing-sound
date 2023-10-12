package processing.sound;

import com.jsyn.ports.UnitOutputPort;
import com.jsyn.unitgen.UnitOscillator;

import processing.core.PApplet;

/**
 * For advanced users: common superclass of all oscillator sound sources
 * @webref Oscillators
 */
public abstract class Oscillator<JSynOscillator extends UnitOscillator> extends SoundObject implements Modulator {

	protected JSynOscillator oscillator;

	protected Oscillator(PApplet theParent, JSynOscillator oscillator) {
		super(theParent);
		this.oscillator = oscillator;
		this.oscillator.frequency.setValueAdded(true);
		this.oscillator.amplitude.setValueAdded(true);
		this.circuit = new JSynCircuit(this.oscillator.getOutput());
		this.amplitude = this.oscillator.amplitude;
	}

	public UnitOutputPort getModulator() {
		// add to the synth
		this.circuit.setSynthesisEngine(Sound.getSynthesisEngine());
		return this.circuit.getOutput();
	}

	/**
	 * Sets the frequency of the oscillator.
	 * @webref Oscillators:Oscillator
	 * @param freq the desired oscillator frequency in Hertz
	 */
	public void freq(float freq) {
		// TODO check positive?
		this.oscillator.frequency.set(freq);
	}
	
 /**
	* Modulates the frequency of this oscillator using another generator, 
	* typically a (low frequency) oscillator. The effective frequency of the 
	* oscillator will be the sum of the static <code>float</code> value passed to 
	* <code>freq()</code>, and the dynamic value produced by the modulator (which 
	* fluctuates around 0).
	* @param modulator an oscillator or noise object
	*/
	public void freq(Modulator modulator) {
		Engine.setModulation(this.oscillator.frequency, modulator);
	}

	public void amp(Modulator modulator) {
		Engine.setModulation(this.oscillator.amplitude, modulator);
	}

	public void play() {
		super.play();
	}

	public void play(float freq, float amp) {
		this.freq(freq);
		this.amp(amp);
		this.play();
	}

	/**
	 * @deprecated
	 * @nowebref
	 */
	public void play(float freq, float amp, float add) {
		this.add(add);
		this.play(freq, amp);
	}

	/**
	 * Starts the oscillator
	 * @webref Oscillators:Oscillator
	 * @param freq The frequency value of the oscillator in Hz.
	 * @param amp The amplitude of the oscillator as a value between 0.0 and 1.0.
	 * @param pos The panoramic position of the oscillator as a float from -1.0 to 1.0.
	 **/
	public void play(float freq, float amp, float add, float pos) {
		this.set(freq, amp, add, pos);
		this.play();
	}

	/**
	 * Set multiple parameters at once
	 * @webref Oscillators:Oscillator
	 * @param freq The frequency value of the oscillator in Hz.
	 * @param amp The amplitude of the oscillator as a value between 0.0 and 1.0.
	 * @param pos The panoramic position of the oscillator as a float from -1.0 to 1.0.
	 **/
	public void set(float freq, float amp, float pos) {
		this.freq(freq);
		this.amp(amp);
		this.pan(pos);
	}

	/**
	 * @deprecated
	 * @nowebref
	 */
	public void set(float freq, float amp, float add, float pos) {
		this.freq(freq);
		this.amp(amp);
		this.add(add);
		this.pan(pos);
	}

	/**
	 * Stop the oscillator from playing back
	 * @webref Oscillators:Oscillator
	 */
	public void stop() {
		super.stop();
	}
}
