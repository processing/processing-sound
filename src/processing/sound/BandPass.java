package processing.sound;

import com.jsyn.unitgen.FilterBandPass;

import processing.core.PApplet;

/**
 * This is a band pass filter.
 * @webref Effects:BandPass
 * @webBrief This is a band pass filter.
 * @param parent PApplet: typically use "this"
 **/
public class BandPass extends Effect<FilterBandPass> {

	public BandPass(PApplet parent) {
		super(parent);
	}

	@Override
	protected FilterBandPass newInstance() {
		return new FilterBandPass();
	}

	/**
	 * Set the bandwidth for the filter.
	 * @webref Effects:BandPass
	 * @webBrief Set the bandwidth for the filter.
	 * @param freq Bandwidth in Hz
	 **/
	public void bw(float bw) {
		// TODO check filter quality
		this.left.Q.set(this.left.frequency.get() / bw);
		this.right.Q.set(this.right.frequency.get() / bw);
	}

	/**
	 * Set the cutoff frequency for the filter.
	 * @webref Effects:BandPass
	 * @webBrief Set the cutoff frequency for the filter.
	 * @param freq Cutoff frequency between 0 and 20000
	 **/
	public void freq(float freq) {
		this.left.frequency.set(freq);
		this.right.frequency.set(freq);
	}
	
	public void process(SoundObject input, float freq) {
		this.freq(freq);
		this.process(input);
	}

	/**
	 * Start applying this bandpass filter to an input signal.
	 * @webref Effects:BandPass
	 * @param input the sound source to apply the filter to
	 * @param freq Cutoff frequency between 0 and 20000
	 * @param bw Set the bandwidth
	 **/
	public void process(SoundObject input, float freq, float bw) {
		this.freq(freq);
		this.bw(bw);
		this.process(input);
	}

	/**
	 * Sets frequency and bandwidth of the filter with one method. 
	 * @webref Effects:BandPass
	 * @webBrief Sets frequency and bandwidth of the filter with one method.
	 * @param freq Set the frequency
	 * @param bw Set the bandwidth
	 **/
	public void set(float freq, float bw) {
		this.freq(freq);
		this.bw(bw);
	}
}
