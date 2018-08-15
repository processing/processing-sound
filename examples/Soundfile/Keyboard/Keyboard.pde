/**
 * This example shows how to make a simple keyboard-triggered sampler with the Sound
 * library. In this sketch 5 different short samples are loaded and played back at
 * different speeds, which also changes their perceived pitch by one or two octaves.
 */

import processing.sound.*;

SoundFile[] file;

// Define the number of samples 
int numsounds = 5;

// Define a variable to store the randomly generated background color in
int backgroundColor[] = {255, 255, 255};

void setup() {
  size(640, 360);

  // Create a Sound renderer and an array of empty soundfiles
  file = new SoundFile[numsounds];

  // Load 5 soundfiles from a folder in a for loop. By naming
  // the files 1.aif, 2.aif, 3.aif, ..., n.aif it is easy to iterate
  // through the folder and load all files in one line of code.
  for (int i = 0; i < numsounds; i++) {
    file[i] = new SoundFile(this, (i+1) + ".aif");
  }
}

void draw() {
  background(backgroundColor[0], backgroundColor[1], backgroundColor[2]);
}

void keyPressed() {
  // We use a boolean helper variable to determine whether one of the branches
  // of the switch-statement was activated or not
  boolean validKey = true;

  switch(key) {
  case 'a':
    file[0].play(0.5, 1.0);
    break;

  case 's':
    file[1].play(0.5, 1.0);
    break;

  case 'd':
    file[2].play(0.5, 1.0);
    break;

  case 'f':
    file[3].play(0.5, 1.0);
    break;

  case 'g':
    file[4].play(0.5, 1.0);
    break;

  case 'h':
    file[0].play(1.0, 1.0);
    break;

  case 'j':
    file[1].play(1.0, 1.0);
    break;

  case 'k':
    file[2].play(1.0, 1.0);
    break;

  case 'l':
    file[3].play(1.0, 1.0);
    break;

  case ';':
    file[4].play(1.0, 1.0);
    break;

  case '\'':
    file[0].play(2.0, 1.0);
    break;

  case 'q':
    file[1].play(2.0, 1.0);
    break;

  case 'w':
    file[2].play(2.0, 1.0);
    break;    

  case 'e':
    file[3].play(2.0, 1.0);
    break;

  case 'r':
    file[4].play(2.0, 1.0);
    break; 

  case 't':
    file[0].play(3.0, 1.0);
    break;

  case 'y':
    file[1].play(3.0, 1.0);
    break;

  case 'u':
    file[2].play(3.0, 1.0);
    break;    

  case 'i':
    file[3].play(3.0, 1.0);
    break;

  case 'o':
    file[4].play(3.0, 1.0);
    break;

  case 'p':
    file[0].play(4.0, 1.0);
    break;    

  case '[':
    file[1].play(4.0, 1.0);
    break;

    // no valid key was pressed, store that information
  default:
    validKey = false;
  }

  // If a new sample playback was triggered, change the background color
  if (validKey) {
    for (int i = 0; i < 3; i++) {
      backgroundColor[i] = int(random(255));
    }
  }
}
