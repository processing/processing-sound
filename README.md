## Processing Sound library

[![Release](https://img.shields.io/github/v/release/processing/processing-sound?sort=semver)](https://github.com/processing/processing-sound/releases) ![License](https://img.shields.io/github/license/processing/processing-sound) [![Documentation](https://img.shields.io/badge/Docs-processing.org-black)](https://www.processing.org/reference/libraries/sound/) [![Javadoc](https://img.shields.io/badge/Docs-Javadoc-lightgray)](https://processing.github.io/processing-sound/)

The new Sound library for Processing provides a simple way to work with audio. It can play, analyze, and synthesize sound. The library comes with a collection of oscillators for basic wave forms, a variety of noise generators, and effects and filters to alter sound files and other generated sounds. The syntax is minimal to make it easy for beginners who want a straightforward way to add some sound to their Processing sketches!

### How to use

The easiest way to install the Sound library is through Processing's Contribution Manager. The library comes with many example sketches, the full online reference can be found [here](https://www.processing.org/reference/libraries/sound/).

If you have questions or problems using the library, the best place for help is the [Processing Discourse](https://discourse.processing.org/). Bugs can be reported on the [Github issues page](https://github.com/processing/processing-sound/issues), advanced users can also have a look at the library's full [JavaDoc documentation](https://processing.github.io/processing-sound/index.html?processing/sound/package-summary.html).

For detailed changelogs, have a look at the [Github releases page](https://github.com/processing/processing-sound/releases).

### Playing back sound files

Audio files loaded with the [`SoundFile`](https://processing.org/reference/libraries/sound/SoundFile.html) class are fully loaded into raw memory. That means your sketch will require ~20MB of RAM per minute of stereo audio.

- if you get `OutOfMemoryError: Java heap space` errors on resource-lean platforms (e.g. Raspberry Pi), make sure to increase the heap size in Processing's `File > Preferences > Running` menu. As a rough rule, your heap size should be at least twice as much as the largest audio sample used in your sketch.
- depending on the format and length of the audio files, loading them for the first time with `sf = new SoundFile("yourfilename.ext")` might block the sketch for several seconds, while subsequent calls to `sf.play()` execute instantly. It is generally advisable to create all SoundFile objects in your `setup()`.
- decoding of compressed formats (mp3, ogg, etc) can be quite slow on Raspberry Pi (20 seconds for a 3 minute mp3, 14 seconds for ogg on a Raspberry Pi 3B 32bit). Since all audio samples loaded by the library end up being stored as raw uncompressed data in RAM anyway, we generally recommend using WAV format for loading audio files

### Multi-channel audio interface support

The newest release of the Sound library adds support for [multi-channel audio output](https://github.com/processing/processing-sound/blob/main/examples/IO/MultiChannelOutput/MultiChannelOutput.pde). Most audio interfaces should work out of the box, for sake of completeness we have assembled a list of devices that have been tested to be working (for the devices marked with `*`, see below). If you have troubles getting any audio interface to be recognized correctly, please report them in [this Github issue](https://github.com/processing/processing-sound/issues/87).

- Focusrite Scarlett 2i4
- Motu Mk5 *
- Presonus Studio 26c
- Roland Rubix24
- RME Fireface 802 *
  - output is through the 30 channel device, not the 8 channel device
  - on Windows, select a Buffer Size of 512 samples or less in the Fireface USB Settings

Devices marked with a `*` work out of the box on MacOS, on Windows they are recognized but show up as several stereo devices, rather than one multi-channel device. To be able to use them as one multi-channel devices, you will need to install ASIO drivers and add an explicit call to `MultiChannel.usePortAudio()` at the beginning of your sketch.

### Contributing

Pull requests for bug fixes as well as new features and example sketches are always welcome! Check [CONTRIBUTING.md](CONTRIBUTING.md) for help on how to get started.

Thanks to the following community members for their contributions:

* [@Calsign](https://github.com/Calsign) for improved Android support
* [@alexdmiller](https://github.com/alexdmiller) for the `BeatDetection` analyzer class
* [@icalvin102](https://github.com/icalvin102) for the `Waveform` analyzer class
* [@defrost256](https://github.com/defrost256)
* [@cluder](https://github.com/cluder)
* [@pixmusix](https://github.com/pixmusix) for the `AllPass` filter
* [@damaru-inc](https://github.com/damaru-inc)
* [@trackme518](https://github.com/trackme518)

### License

[LGPL v2.1](LICENSE)
