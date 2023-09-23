package processing.sound;

import com.jsyn.unitgen.FilterBandPass;

import processing.core.PApplet;

/**
 * This is a band pass filter.
 * @webref Effects:BandPass
 * @webBrief This is a band pass filter.
 * @param parent PApplet: typically use "this"
 **/
public class BandPass extends Filter<FilterBandPass> {

	// when set to a positive value (desired bandpass bandwidth in Hertz), any 
	// change to freq() will be followed by a re-calculation of Q that will 
	// produce the given bandwidth
	private float bandwidth = -1;

	public BandPass(PApplet parent) {
		super(parent);
	}

	@Override
	protected FilterBandPass newInstance() {
		return new FilterBandPass();
	}

	/**
	 * Sets the bandwidth of this BandPass filter.
	 * @webref Effects:BandPass
	 * @webBrief Sets the bandwidth for the filter.
	 * @param bw the filter bandwidth in Hertz
	 * @see BandPass#res()
	 **/
	public void bw(float bw) {
		this.bandwidth = bw;
		this.updateQ();
	}

	private void updateQ() {
		if (this.bandwidth > 0) {
			// TODO check if the value is still in the [0.1, 10] range?
			this.res((float) this.left.frequency.get() / this.bandwidth);
		}
	}

	/**
	 * Sets a fixed Q factor for this filter. If you want to specify a fixed 
	 * bandwidth for this bandpass filter (in Hertz) that is maintained even as 
	 * the center frequency of the filter changes, use <code>bw(float)</code> 
	 * instead.
	 * @webref Effects:BandPass
	 * @webBrief Sets the resonance (or 'Q factor') of this filter.
	 * @param q the desired Q factor, a value between 0.1 and 10
	 * @see BandPass#bw()
	 */
	public void res(float q) {
		super.res(q);
		this.bandwidth = -1;
	}

	/**
	 * Sets the center frequency of the filter.
	 * @webref Effects:BandPass
	 * @webBrief Sets the center frequency of the filter.
	 * @param freq the center frequency in Hertz
	 **/
	public void freq(float freq) {
		super.freq(freq);
		this.updateQ();
	}

	/**
	 * Start applying this bandpass filter to an input signal.
	 * @webref Effects:BandPass
	 * @param input the sound source to filter
	 * @param freq the center frequency in Hertz
	 * @param bw the filter bandwidth in Hertz
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
	 * @param freq the center frequency in Hertz
	 * @param bw the filter bandwidth in Hertz
	 **/
	public void set(float freq, float bw) {
		this.freq(freq);
		this.bw(bw);
	}
}
