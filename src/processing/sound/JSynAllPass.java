package processing.sound;

import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.unitgen.Circuit;
import com.jsyn.unitgen.Delay;
import com.jsyn.unitgen.MultiplyAdd;

// https://ccrma.stanford.edu/~jos/pasp/Allpass_Two_Combs.html
// y = b0*x + x(-N) - b0*y(-N)
class JSynAllPass extends Circuit {

	protected UnitInputPort input;
	protected UnitOutputPort output;

	public JSynAllPass(double g, int N) {
		MultiplyAdd pre = new MultiplyAdd();
		this.add(pre);
		this.input = pre.inputC;

		Delay delay = new Delay();
		delay.allocate(N);
		this.add(delay);
		delay.input.connect(pre.output);
		pre.inputA.set(-g);
		pre.inputB.connect(delay.output);

		MultiplyAdd post = new MultiplyAdd();
		post.inputA.set(g);
		post.inputB.connect(pre.output);
		post.inputC.connect(delay.output);
		
		this.output = post.output;
	}

}
