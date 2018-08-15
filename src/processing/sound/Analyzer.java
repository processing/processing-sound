package processing.sound;

import com.jsyn.ports.UnitOutputPort;

import processing.core.PApplet;

abstract class Analyzer {

	protected SoundObject input;

	protected Analyzer(PApplet parent) {
		Engine.getEngine(parent);
	}

	/**
	 * Define the audio input for the analyzer.
	 * 
	 * @param input The input sound source
	 **/
	public void input(SoundObject input) {
		if (this.input == input) {
			Engine.printWarning("This input was already connected to the analyzer");
		} else {
			if (this.input != null) {
				if (!this.input.isPlaying()) {
					// unit was only analyzed but not playing out loud - remove from synth
					Engine.getEngine().remove(this.input.circuit);
				}

				this.removeInput();
			}

			this.input = input;
			if (!this.input.isPlaying()) {
				Engine.getEngine().add(input.circuit);
			}

			this.setInput(input.circuit.output.output);
		}
	}

	// remove the current input
	protected abstract void removeInput();

	// connect sound source in subclass AND add analyser unit to Engine
	protected abstract void setInput(UnitOutputPort input);
}
