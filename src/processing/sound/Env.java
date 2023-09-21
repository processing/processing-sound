package processing.sound;

import com.jsyn.data.SegmentedEnvelope;
import com.jsyn.ports.QueueDataCommand;
import com.jsyn.ports.QueueDataEvent;
import com.jsyn.ports.UnitDataQueueCallback;
import com.jsyn.unitgen.VariableRateMonoReader;

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
	* @webref Envelopes
	* @webBrief Triggers the envelope.
	* @param input Input sound source
	* @param attackTime Attack time value as a float.
	* @param sustainTime Sustain time value as a float. 
	* @param sustainLevel Sustain level value as a float. (as fraction of the input amplitude)
	* @param releaseTime Release time value as a float.
	**/
	public void play(SoundObject input, float attackTime, float sustainTime, float sustainLevel, float releaseTime) {
		SegmentedEnvelope env = new SegmentedEnvelope(new double[] {
				attackTime, input.amp, // attack
				// gradual decay towards sustain level across entire sustain period
				sustainTime, sustainLevel * input.amp, // sustain
				releaseTime, 0.0 });

		// fire-and-forget envelope player
		VariableRateMonoReader player = new VariableRateMonoReader();
		Engine.getEngine().add(player);
		// we need to start the player explicitly, otherwise if it gets disconnected 
		// by another envelope kicking in before it has completed, it would stop 
		// prematurely and the callback (which removes it from the synth for garbage 
		// collection) would never get called!
		player.start();

		input.amplitude.disconnectAll();
		player.output.connect(input.amplitude);
		if (!input.isPlaying()) {
			input.play();
		}

		QueueDataCommand cmd = player.dataQueue.createQueueDataCommand(env, 0, env.getNumFrames());
		// need to set auto stop for the remove() inside the callback to work
		cmd.setAutoStop(true);
		cmd.setCallback(new UnitDataQueueCallback() {
			public void finished(QueueDataEvent event) {
				// check if this output has maybe already been disconnected (by a new 
				// envelope that has taken over)
				if (player.output.isConnected()) {
					player.output.disconnectAll();
					// TODO what to do with the input soundobject after the envelope is 
					// finished? just silence it, but then it isn't automatically garbage 
					// collected? should we trigger the object's stop() as well?
					input.amplitude.set(0);
				}
				Engine.getEngine().remove(player);
			}
			public void looped(QueueDataEvent event) {
			}
			public void started(QueueDataEvent event) {
			}
		});

		player.getSynthesizer().queueCommand(cmd);
	}
}
