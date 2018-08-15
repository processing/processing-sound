package processing.sound;

import com.jsyn.engine.SynthesisEngine;
import com.jsyn.unitgen.Circuit;
import com.jsyn.unitgen.InterpolatingDelay;
import com.jsyn.unitgen.MultiplyAdd;
import com.jsyn.unitgen.UnitFilter;

/**
 * A custom JSyn delay circuit with feedback.
 */
class JSynDelay extends UnitFilter {

	private Circuit feedbackCircuit;

	private InterpolatingDelay delay = new InterpolatingDelay();
	private MultiplyAdd feedback = new MultiplyAdd();
	
	public JSynDelay() {
		super();
		this.feedbackCircuit = new Circuit();
		this.feedbackCircuit.add(this.delay);
		this.feedbackCircuit.add(this.feedback);

		// put the feedback multiplier unit before the delay -- this way
		// the original signal is not played back immediately, but playback
		// will be delayed for the length of the delay time
		// TODO could add 'mix' parameter which allows direct passthrough of
		// the original signal?
		this.input = this.feedback.inputC;
		this.feedback.inputA.set(0.0);

		this.feedback.inputB.connect(this.delay.output);
		this.feedback.output.connect(this.delay.input);
		this.output = this.delay.output;
	}

	@Override
    public void setSynthesisEngine(SynthesisEngine synthesisEngine) {
		this.feedbackCircuit.setSynthesisEngine(synthesisEngine);
    }

	public void generate(int start, int limit) {
		// not called
	}

	protected void setDelayTime(float delayTime) {
		this.delay.delay.set(delayTime);
	}

	protected void setFeedback(float feedback) {
		// TODO check range
		this.feedback.inputA.set(feedback);
	}

	protected void setMaxDelayTime(float maxDelayTime) {
		int maxSamples = (int) (Engine.getEngine().getSampleRate() * maxDelayTime);
		this.delay.allocate(maxSamples);
	}
}
