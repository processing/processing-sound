package processing.sound;

import processing.core.PApplet;

/**
 * Old Sound library configuration class, deprecated. Have a look at the Sound class instead.
 * @deprecated
 * @see Sound
 */
public class AudioDevice {
	public AudioDevice(PApplet theParent, int sampleRate, int bufferSize) {
		Engine.printWarning("the AudioDevice class is deprecated and will be removed in future versions of the library. For configuration, please have a look at the new Sound class instead.");
		// bufferSize is ignored - the parameter was necessary for the original library's FFT to work
		new Sound(theParent).sampleRate(sampleRate);
	}
}
