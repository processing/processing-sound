package processing.sound;

import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.unitgen.Circuit;
import com.jsyn.unitgen.Delay;
import com.jsyn.unitgen.FilterOnePole;
import com.jsyn.unitgen.MultiplyAdd;
import com.jsyn.unitgen.PassThrough;

// see https://ccrma.stanford.edu/~jos/pasp/Lowpass_Feedback_Comb_Filter.html
class JSynLBCF extends Circuit {

	protected UnitInputPort input;
	protected UnitOutputPort output;

	private MultiplyAdd mixer;
	private Delay delay;
	private FilterOnePole filter;

	public JSynLBCF(double f, double d, int N) {
		PassThrough in = new PassThrough();
		this.add(in);

		this.add(this.mixer = new MultiplyAdd());
		this.setF(f);

		this.add(this.filter = new FilterOnePole());
		this.setD(d);

		this.add(this.delay = new Delay());
		this.delay.allocate(N);

		this.input = in.input;
//		in.output.connect(this.mixer.inputC);
		in.output.connect(this.delay.input);
		this.delay.output.connect(this.filter.input);
		this.filter.output.connect(this.mixer.inputB);
		this.output = this.mixer.output;
	}

	// see https://ccrma.stanford.edu/~jos/fp/One_Pole.html
	protected void setD(double d) {
		this.filter.a0.setValue(1 - d);
		this.filter.b1.setValue(-d);
	}

	protected void setF(double f) {
		this.mixer.inputA.set(f);
	}
}
