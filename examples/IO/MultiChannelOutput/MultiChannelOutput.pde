import processing.sound.*;

SinOsc sines[];
int initialised;
float frequency;

void setup() {
  size(640, 360);
  background(255);

  // some multi-channel USB audio interfaces don't show the correct number of 
  // output channels using the default audio drivers on Windows. If this is the 
  // case, try loading PortAudio support at the very top of your sketch with the 
  // following command (see the LowLevelEngine example for details):
//  MultiChannel.usePortAudio();

  boolean foundMultiChannel = false;
  String[] deviceNames = Sound.list();
  for (int i = 0; i < deviceNames.length; i++) {
    if (MultiChannel.availableChannels(i) > 2) {
      println("Found a multi-channel device: " + deviceNames[i]);
      Sound.outputDevice(i);
      foundMultiChannel = true;
      break;
    }
  }
  if (!foundMultiChannel) {
    println("Did not find any output devices with more than 2 channels!");
  }
  
  println("Playing back different sine waves on the " + MultiChannel.availableChannels() + " different channels");
  
  sines = new SinOsc[MultiChannel.availableChannels()];
  initialised = 0;
  frequency = 100;
  
  textSize(128);
  fill(0);
  textAlign(CENTER);
}

void draw() {
  // loop through all channels and start one sine wave on each
  if (initialised < sines.length) {
    // add a nice theatrical break
    delay(1000);

    background(255);
    text((initialised + 1) + " of " + sines.length, width/2, height/2);

    MultiChannel.activeChannel(initialised);
    // create and start the sine oscillator.
    sines[initialised] = new SinOsc(this);
    sines[initialised].freq(frequency);
    sines[initialised].play();

    // increase frequency on the next channel by one semitone
    frequency = frequency * 1.05946;
    initialised = initialised + 1;
    return;
  }

  // as long as the oscillators are not stopped they will 'stick'
  // to the channel that they were originally added to, and we can
  // change their parameters freely
  frequency = map(mouseX, 0, width, 80.0, 1000.0);
  
  for (SinOsc sin : sines) {
    sin.freq(frequency);
    
    // increase frequency on the next channel by one semitone (a factor of 2^(1/12))
    frequency = frequency * 1.05946;
  }
}
