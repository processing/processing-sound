## Processing Sound library

[![Release](https://img.shields.io/github/v/release/processing/processing-sound?sort=semver)](https://github.com/processing/processing-sound/releases) ![License](https://img.shields.io/github/license/processing/processing-sound) [![Documentation](https://img.shields.io/badge/Docs-processing.org-black)](https://www.processing.org/reference/libraries/sound/) [![Javadoc](https://img.shields.io/badge/Docs-Javadoc-lightgray)](https://processing.github.io/processing-sound/)

The new Sound library for Processing provides a simple way to work with audio. It can play, analyze, and synthesize sound. The library comes with a collection of oscillators for basic wave forms, a variety of noise generators, and effects and filters to alter sound files and other generated sounds. The syntax is minimal to make it easy for beginners who want a straightforward way to add some sound to their Processing sketches!

### How to use

The easiest way to install the Sound library is through Processing's Contribution Manager. The library comes with many example sketches, the full online reference can be found [here](https://www.processing.org/reference/libraries/sound/).

If you have questions or problems using the library, the best place for help is the [Processing Discourse](https://discourse.processing.org/). Bugs can be reported on the [Github issues page](https://github.com/processing/processing-sound/issues), advanced users can also have a look at the library's full [JavaDoc documentation](https://processing.github.io/processing-sound/index.html?processing/sound/package-summary.html).

For detailed changelogs, have a look at the [Github releases page](https://github.com/processing/processing-sound/releases).

### Known issues

[`SoundFile`](https://processing.org/reference/libraries/sound/SoundFile.html) class for loading audio data from disk:
* Currently no support for decoding WAV files that are in compressed formats such as 8bit unsigned (see [#15](/../../issues/15))
* MP3 decoding is extremely slow on ARM processors (Android and Raspberry Pi). Since all audio samples loaded by the library end up being stored as raw uncompressed data in RAM anyway, we generally recommend using WAV files for loading audio samples from disk
* Some MP3 files with meta-information (especially where large amounts of data such as the album cover image is stored in the ID3 header) fail to load (see [#32](/../../issues/32))

### Contributing

Pull requests for bug fixes as well as new features and example sketches are always welcome! Check [[CONTRIBUTING.md]] for help on how to get started.

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

LGPL v2.1
