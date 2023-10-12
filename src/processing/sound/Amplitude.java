package processing.sound;

import com.jsyn.ports.UnitOutputPort;
import com.jsyn.unitgen.PeakFollower;

import processing.core.PApplet;

/**
 * This is a volume analyzer. It tracks the peaks of an input signal, which is a 
 * simple measure of the overall amplitude of that signal.
 * 
 * @webref Analysis:Amplitude
 * @webBrief This is a volume analyzer.
 */
public class Amplitude extends Analyzer implements Modulator {

	private PeakFollower follower;

	/**
	 * @param parent typically use "this"
	 * @webref Analysis:Amplitude
	 */
	public Amplitude(PApplet parent) {
		super(parent);
		this.follower = new PeakFollower();
		this.halfLife(0.1f);
	}

	/**
	 * Sets the half-life of this amplitude analyzer. The output approaches zero 
	 * based on the value on halfLife. The default value is <code>0.1</code>.
	 * @webBrief Sets the half-life of this amplitude analyzer.
	 */
	public void halfLife(float value) {
		this.follower.halfLife.set(value);
	}

	protected void removeInput() {
		this.follower.input.disconnectAll();
		this.input = null;
	}

	protected void setInput(UnitOutputPort input) {
		Engine.getEngine().add(this.follower);
		this.follower.start();
		this.follower.input.connect(input);
	}

	/**
	 * Queries a value from the analyzer and returns a float between 0. and 1.
	 * 
	 * @webref Analysis:Amplitude
	 * @webBrief Queries a value from the analyzer and returns a float between 0. and 1.
	 * @return amp An amplitude value between 0-1.
	 **/
	public float analyze() {
		// TODO check if input exists, print warning if not
		return (float) this.follower.current.getValue();
	}


	// Below are just duplicated methods from superclasses which are required
	// for the online reference to build the corresponding pages.

	/**
	 * Define the audio input for the analyzer.
	 * 
	 * @param input
	 *            the input sound source. Can be an oscillator, noise generator,
	 *            SoundFile or AudioIn.
	 * @webref Analysis:Amplitude
	 * @webBrief Define the audio input for the analyzer.
	 **/
	public void input(SoundObject input) {
		super.input(input);
	}

	public UnitOutputPort getModulator() {
		return this.follower.output;
	}
}
