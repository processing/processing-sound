package processing.sound;

import com.jsyn.data.SegmentedEnvelope;
import com.jsyn.unitgen.VariableRateMonoReader;
import com.softsynth.shared.time.TimeStamp;

import processing.core.PApplet;

/**
* This is an ASR (Attack Sustain Release) Envelope Generator.
* @webref Envelopes
* @webBrief This is an ASR (Attack Sustain Release) Envelope Generator.
* @param parent PApplet: typically use "this"
**/
public class Env {

	public Env(PApplet parent) {
		Engine.getEngine(parent);
	}

	/**
	* Triggers the envelope.
	* @webref Envelopes:Env
	* @webBrief Triggers the envelope.
	* @param input Input sound source
	* @param attackTime Attack time value as a float.
	* @param sustainTime Sustain time value as a float. 
	* @param sustain Sustain level value as a float. (as fraction of the input amplitude)
	* @param releaseTime Release time value as a float.
	**/
	public void play(SoundObject input, float attackTime, float sustainTime, float sustainLevel, float releaseTime) {
		SegmentedEnvelope env = new SegmentedEnvelope(new double[] {
				attackTime, input.amp, // attack
				// gradual decay towards sustain level across entire sustain period
				sustainTime, sustainLevel * input.amp, // sustain
				releaseTime, 0.0 });

		// TODO re-use player from fixed or dynamic pool
		VariableRateMonoReader player = new VariableRateMonoReader();

		// this would make sense to me but breaks the envelope for some reason
//		input.amplitude.disconnectAll();
		player.output.connect(input.amplitude);
		Engine.getEngine().add(player);

		player.dataQueue.queue(env);
		if (!input.isPlaying()) {
			input.play();
		}

		// disconnect player from amplitude port after finished and set amplitude to 0
		TimeStamp envFinished = Engine.getEngine().synth.createTimeStamp().makeRelative(attackTime + sustainTime + releaseTime);
		player.output.disconnect(0, input.amplitude, 0, envFinished);
		// TODO better: trigger unit stop() so that isPlaying() is set to false as well?
		input.amplitude.set(0, envFinished);
	}
}
