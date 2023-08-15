package processing.sound;

import java.util.HashSet;
import java.util.Set;

import com.jsyn.ports.UnitOutputPort;
import com.jsyn.unitgen.TwoInDualOut;
import com.jsyn.unitgen.UnitFilter;

import processing.core.PApplet;

/**
 * For advanced users: common superclass of all effect types
 */
// helper class for applying the same effect (with the same parameters) on two channels.
// a basic design question is what to do if the same effect is applied to several different
// input sources -- do we consider them all to feed into the same effect
// unit(s), or should we instantiate new units every time process() is called?
// presently all input sources get connected to the same two left/right effect
// units, where their input signals are automatically added together. calling
// stop() on the effect also disconnects all input sources before removing the
// effect from the synthesis.
public abstract class Effect<EffectType extends UnitFilter> {

	// store references to all input sources
	protected Set<SoundObject> inputs = new HashSet<SoundObject>();

	protected EffectType left;
	protected EffectType right;
	protected UnitOutputPort output;

	// invoked by subclasses
	protected Effect(PApplet parent) {
		Engine.getEngine(parent);
		this.left = this.newInstance();
		this.right = this.newInstance();
		TwoInDualOut merge = new TwoInDualOut();
		merge.inputA.connect(this.left.output);
		merge.inputB.connect(this.right.output);
		this.output = merge.output;
	}

	protected abstract EffectType newInstance();

	/**
	 * Get information on whether this effect is currently active.
	 * @return true if this effect is currently processing at least one sound source
	 */
	public boolean isProcessing() {
		return ! this.inputs.isEmpty();
	}

	/**
	 * Start the effect.
	 * @param input Input sound source
	 * @webref Effects
	 */
	public void process(SoundObject input) {
		if (this.inputs.add(input)) {
			// attach effect to circuit until removed with effect.stop()
			input.setEffect(this);
		} else {
			Engine.printWarning("the effect is already processing this sound source");
		}
	}

	/**
	 * Stop the effect.
	 * @webref Effects
	 */
	public void stop() {
		if (this.inputs.isEmpty()) {
			Engine.printWarning("this effect is not currently processing any signals.");
		} else {
			for (SoundObject o : this.inputs) {
				o.removeEffect(this);
			}
			this.inputs.clear();
			Engine.getEngine().remove(this.left);
			Engine.getEngine().remove(this.right);
		}
	}
}
