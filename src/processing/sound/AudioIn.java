package processing.sound;

import com.jsyn.unitgen.ChannelIn;
import com.jsyn.unitgen.Multiply;

import android.Manifest;
import processing.core.PApplet;

/**
 * AudioIn lets you grab the audio input from your sound card.
 * 
 * @webref sound
 **/
public class AudioIn extends SoundObject {

	// ChannelIn for mono, LineIn for stereo
	private ChannelIn input;

	// unlike the oscillator classes, ChannelIn does not have an amplitude
	// port, so we need to control the amplitude via an extra multiplier unit
	private Multiply multiplier;

	private String ANDROID_PERMISSION_WARNING_MESSAGE =
		"sketch does not have permission to record audio from microphone,\n" +
		"please request the permission in your AndroidManifest.xml and in\n" +
		"your sketch initialization code (the Sound library's\n" +
		"AudioInputAndroid example demonstrates how to do both)\n";

	private boolean permissionGranted;

	public AudioIn(PApplet parent) {
		this(parent, 0);
	}

	/**
	 * @param parent
	 *            typically use "this"
	 * @param in
	 *            input channel number (optional, default 0)
	 */
	public AudioIn(PApplet parent, int in) {
		super(parent);

		permissionGranted = true;

		if (Engine.getAudioManager() instanceof JSynAndroidAudioDeviceManager) {
			// we're on Android, check if the sketch has permission to capture Audio
			if (!parent.hasPermission(Manifest.permission.RECORD_AUDIO)) {
				Engine.printWarning(ANDROID_PERMISSION_WARNING_MESSAGE);
				permissionGranted = false;
			}
		}

		if (permissionGranted) {
			this.input = new ChannelIn(in);
			this.multiplier = new Multiply();
			this.multiplier.inputA.connect(this.input.output);
			this.amplitude = this.multiplier.inputB;
			// set default amplitude
			this.multiplier.inputB.set(1.0);

			this.circuit = new JSynCircuit(this.multiplier.output);
			this.circuit.add(this.input);
		} else {
			// What to do here?
			// Need some type of dummy microphone
		}
	}

	public void play() {
		if (permissionGranted) {
			super.play();
		}
	}

	public void play(float amp) {
		if (permissionGranted) {
			this.amp(amp);
			this.play();
		}
	}

	public void play(float amp, float add) {
		if (permissionGranted) {
			this.add(add);
			this.play(amp);
		}
	}

	/**
	 * Start capturing the input stream and route it to the audio output
	 *
	 * @param amp
	 *            the volume to grab the input at as a value from 0.0 (complete
	 *            silence) to 1.0 (full volume)
	 * @param add
	 *            offset the audio input by the given value
	 * @param pos
	 *            pan the audio input in a stereo panorama. Allowed values are
	 *            between -1.0 (left) and 1.0 (right)
	 * @webref sound
	 **/
	public void play(float amp, float add, float pos) {
		if (permissionGranted) {
			this.set(amp, add, pos);
			this.play();
		}
	}

	public void start() {
		if (permissionGranted) {
			Engine.getEngine().add(this.circuit);
		}
	}

	public void start(float amp) {
		if (permissionGranted) {
			this.amp(amp);
			this.start();
		}
	}

	public void start(float amp, float add) {
		if (permissionGranted) {
			this.add(add);
			this.start(amp);
		}
	}

	/**
	 * Start the input stream without routing it to the audio output. This is useful
	 * if you only want to perform audio analysis based on the microphone input.
	 *
	 * @param amp
	 *            the volume to grab the input at as a value from 0.0 (complete
	 *            silence) to 1.0 (full volume)
	 * @param add
	 *            offset the audio input by the given value
	 * @param pos
	 *            pan the audio input in a stereo panorama. Allowed values are
	 *            between -1.0 (left) and 1.0 (right)
	 * @webref sound
	 */
	public void start(float amp, float add, float pos) {
		if (permissionGranted) {
			this.set(amp, add, pos);
			this.start();
		}
	}

	/**
	 * Sets amplitude, add and pan position with one method.
	 * 
	 * @webref sound
	 * @param amp
	 *            the volume to grab the input at as a value from 0.0 (complete
	 *            silence) to 1.0 (full volume)
	 * @param add
	 *            offset the audio input by the given value
	 * @param pos
	 *            pan the audio input in a stereo panorama. Allowed values are
	 *            between -1.0 (left) and 1.0 (right)
	 **/
	public void set(float amp, float add, float pos) {
		if (permissionGranted) {
			this.amp(amp);
			this.add(add);
			this.pan(pos);
		}
	}
}
