package processing.sound;

import processing.core.PApplet;

/**
 * This is a simple delay effect.
 * 
 * @webref effects
 * @webBrief This is a simple delay effect.
 * @param parent
 *            PApplet: typically use "this"
 **/
public class Delay extends Effect<JSynDelay> {

	public Delay(PApplet parent) {
		super(parent);
	}

	@Override
	protected JSynDelay newInstance() {
		return new JSynDelay();
	}

	/**
	 * Start the delay effect.
	 * 
	 * @webref delay
     * @webBrief Start the delay effect.
	 * @param input
	 *            Input audio source
	 * @param maxDelayTime Maximum delay time in seconds.
	 * @param delayTime Delay time to use when starting to process, in seconds.
	 **/
	public void process(SoundObject input, float maxDelayTime, float delayTime) {
		this.left.setMaxDelayTime(maxDelayTime);
		this.right.setMaxDelayTime(maxDelayTime);
		this.time(delayTime);
		// connect input in superclass method
		super.process(input);
	}

	public void process(SoundObject input, float maxDelayTime) {
		// set delayTime to maximum
		this.process(input, maxDelayTime, maxDelayTime);
	}

	/**
	 * Set delay time and feedback values at once.
	 * 
	 * @webref delay
	 * @webBrief Set delay time and feedback values at once.
	 * @param delayTime
	 *            Maximum delay time in seconds.
	 * @param feedback
	 *            Feedback amount as a float
	 **/
	public void set(float delayTime, float feedback) {
		this.time(delayTime);
		this.feedback(feedback);
	}

	/**
	 * Changes the delay time of the effect.
	 * 
	 * @webref delay
	 * @webBrief Changes the delay time of the effect.
	 * @param delayTime
	 *            Delay time in seconds.
	 **/
	public void time(float delayTime) {
		// TODO check that delayTime is not greater than effect buffer
		this.left.setDelayTime(delayTime);
		this.right.setDelayTime(delayTime);
	}

	/**
	 * Change the feedback of the delay effect.
	 * 
	 * @webref delay
	 * @webBrief Change the feedback of the delay effect.
	 * @param feedback
	 *            Feedback amount as a float.
	 **/
	public void feedback(float feedback) {
		this.left.setFeedback(feedback);
		this.right.setFeedback(feedback);
	}
}
