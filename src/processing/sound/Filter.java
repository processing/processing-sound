package processing.sound;

import com.jsyn.unitgen.FilterBandPass;
import com.jsyn.unitgen.FilterBiquadCommon;

import processing.core.PApplet;

/**
 * Common superclass for JSyn filters that have a 'frequency' and a 'Q' unitport
 * @webref Effects:Filter
 */
public abstract class Filter<E extends FilterBiquadCommon> extends Effect<E> {

	public Filter(PApplet parent) {
		super(parent);
		this.left.frequency.setValueAdded(true);
		this.right.frequency.setValueAdded(true);
		this.left.Q.setValueAdded(true);
		this.right.Q.setValueAdded(true);
	}

	/**
	 * Sets the resonance (or 'Q factor') of this filter. Increasing Q increases 
	 * the resonance of the filter at its cutoff frequency. Defaults to 1.
	 * @webref Effects:Filter
	 * @webBrief Sets the resonance (or 'Q factor') of this filter.
	 * @param q the desired Q factor, a value between 0.1 and 10
	 */
	public void res(float q) {
		// TODO check for [0.1, 10] range
		this.left.Q.set(q);
		this.right.Q.set(q);
	}

 /**
	* Modulates the resonance of this filter using another generator, typically a 
	* (low frequency) oscillator. The effective resonance of the filter will be 
	* the sum of the static value passed to <code>.res(float)</code>, and the 
	* dynamic value produced by the modulator (which fluctuates around 0).
	* @param modulator an oscillator or noise object
	*/
	public void res(Modulator modulator) {
		Engine.setModulation(this.left.Q, modulator);
		Engine.setModulation(this.right.Q, modulator);
	}

	/**
	 * Sets the cutoff frequency for the filter.
	 * @webref Effects:Filter
	 * @webBrief Sets the cutoff frequency for the filter.
	 * @param freq the cutoff frequency in Hertz
	 **/
	public void freq(float freq) {
		this.left.frequency.set(freq);
		this.right.frequency.set(freq);
	}
	
 /**
	* Modulates the frequency of this filter using another generator, typically a 
	* (low frequency) oscillator. The effective cutoff frequency of the filter 
	* will be the sum of the static value passed to <code>.freq(float)</code>, and 
	* the dynamic value produced by the modulator (which fluctuates around 0).
	* @param modulator an oscillator or noise object
	*/
	public void freq(Modulator modulator) {
		Engine.setModulation(this.left.frequency, modulator);
		Engine.setModulation(this.right.frequency, modulator);
	}
	
	public void process(SoundObject input, float freq) {
		this.freq(freq);
		this.process(input);
	}

	/**
	 * Starts applying this filter to an input signal.
	 * @webref Effects:Filter
	 * @webBrief Starts applying this filter to an input signal.
	 * @param input the sound source to filter
	 * @param freq the cutoff frequency in Hertz
	 * @param q the resonance (or 'Q factor'), a value between 0.1 and 10
	 **/
	public void process(SoundObject input, float freq, float q) {
		this.freq(freq);
		this.res(q);
		this.process(input);
	}

	/**
	 * Sets frequency and bandwidth of the filter with one method.
	 * @webref Effects:Filter
	 * @webBrief Sets frequency and bandwidth of the filter with one method.
	 * @param freq the cutoff frequency in Hertz
	 * @param q the resonance (or 'Q factor'), a value between 0.1 and 10
	 **/
	public void set(float freq, float q) {
		this.freq(freq);
		this.res(q);
	}
}
