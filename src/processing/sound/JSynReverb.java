package processing.sound;

import com.jsyn.engine.SynthesisEngine;
import com.jsyn.unitgen.Circuit;
import com.jsyn.unitgen.MixerMono;
import com.jsyn.unitgen.PassThrough;
import com.jsyn.unitgen.UnitFilter;

/**
 * A JSyn implementation of the classic Freeverb design.
 * @seealso https://ccrma.stanford.edu/~jos/pasp/Freeverb.html
 */
class JSynReverb extends UnitFilter {

	private Circuit reverbCircuit;

	// see https://ccrma.stanford.edu/~jos/pasp/Freeverb.html
	private static int[] Ns = new int[] { 1557, 1617, 1491, 1422, 1277, 1356, 1188, 1116 };
	private JSynLBCF[] lbcfs = new JSynLBCF[JSynReverb.Ns.length];

	private static int[] As = new int[] { 225, 556, 441, 341 };

	private MixerMono mixer;

	public JSynReverb() {
		this.reverbCircuit = new Circuit();
		PassThrough in = new PassThrough();
		this.reverbCircuit.add(in);
		this.input = in.input;

		JSynAllPass first = new JSynAllPass(0.5, JSynReverb.As[0]);
		this.reverbCircuit.add(first);
		JSynAllPass ap = first;
		for (int i = 1; i < JSynReverb.As.length; i++) {
			JSynAllPass next = new JSynAllPass(0.5, JSynReverb.As[i]);
			ap.output.connect(next.input);
			ap = next;
			this.reverbCircuit.add(ap);
		}

		for (int i = 0; i < JSynReverb.Ns.length; i++) {
			this.lbcfs[i] = new JSynLBCF(0.84, 0.2, JSynReverb.Ns[i]);
			this.reverbCircuit.add(this.lbcfs[i]);
			this.lbcfs[i].input.connect(in.output);

			// multiple connected inputs to first AllPass are summed automatically
			this.lbcfs[i].output.connect(first.input);
		}

		this.mixer = new MixerMono(2);
		this.mixer.amplitude.set(1.0);
		this.setWet(0.5f);

		in.output.connect(0, this.mixer.input, 0);
		ap.output.connect(0, this.mixer.input, 1);
		this.output = this.mixer.output;
	}

	@Override
	public void setSynthesisEngine(SynthesisEngine synthesisEngine) {
		this.reverbCircuit.setSynthesisEngine(synthesisEngine);
	}

	@Override
	public void generate(int start, int limit) {
		// not called
	}

	protected void setDamp(float damp) {
		// damp = initialdamp * 0.4
		for (JSynLBCF lbcf : this.lbcfs) {
			lbcf.setD(damp * 0.4);
		}
	}

	protected void setRoom(float room) {
		// roomsize = initialroom * 0.28 + 0.7
		for (JSynLBCF lbcf : this.lbcfs) {
			lbcf.setF(room * 0.28 + 0.7);
		}
	}

	protected void setWet(float wet) {
		this.mixer.gain.set(0, 1 - wet);
		this.mixer.gain.set(1, wet);
	}
}
