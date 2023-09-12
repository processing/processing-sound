import processing.sound.*;

import com.jsyn.devices.*;
import com.jsyn.Synthesizer;
import com.jsyn.devices.jportaudio.JPortAudioDevice;

void setup() {

	// get hardware device information
  Sound.list();

	AudioDeviceManager m = Sound.getAudioDeviceManager();
	println("Audio device manager: " + m);
	if (m instanceof JPortAudioDevice) {
		println("Using the PortAudio device for 24 bit support on Windows");
	}


	// get synthesis runtime information
	Synthesizer s = Sound.getSynthesizer();
	// a lot of this information can be gleaned with one look by calling Sound.status();
	println("Current CPU usage: " + s.getUsage());

}

