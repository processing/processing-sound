## Processing Sound library

[![Release](https://img.shields.io/github/v/release/processing/processing-sound?sort=semver)](https://github.com/processing/processing-sound/releases) ![License](https://img.shields.io/github/license/processing/processing-sound)

The new Sound library for Processing 3 provides a simple way to work with audio. It can play, analyze, and synthesize sound. The library comes with a collection of oscillators for basic wave forms, a variety of noise generators, and effects and filters to alter sound files and other generated sounds. The syntax is minimal to make it easy for beginners who want a straightforward way to add some sound to their Processing sketches!

### How to use

The easiest way to install the Sound library is through Processing's Contribution Manager. The library comes with many example sketches, the full online reference can be found [here](https://www.processing.org/reference/libraries/sound/).

If you have questions or problems using the library, the best place for help is the [Processing Discourse](https://discourse.processing.org/). Bugs can be reported on the [Github issues page](https://github.com/processing/processing-sound/issues), advanced users can also have a look at the library's full [JavaDoc documentation](https://processing.github.io/processing-sound/index.html?processing/sound/package-summary.html).

For detailed changelogs and to download older releases, have a look at the [Github releases page](https://github.com/processing/processing-sound/releases).

### Known issues

[`SoundFile`](https://processing.org/reference/libraries/sound/SoundFile.html) class for loading audio data from disk:
* Currently no support for decoding WAV files that are in compressed formats such as 8bit unsigned (see [#15](/../../issues/15))
* MP3 decoding is extremely slow on ARM processors (Android and Raspberry Pi). Since all audio samples loaded by the library end up being stored as raw uncompressed data in RAM anyway, we generally recommend using WAV files for loading audio samples from disk
* Some MP3 files with meta-information (especially where large amounts of data such as the album cover image is stored in the ID3 header) fail to load (see [#32](/../../issues/32))

### Contributing

Pull requests for bug fixes as well as new features and example sketches are always welcome! Check the open [issues](https://github.com/processing/processing-sound/issues) if you don't know where to start.

Thanks to the following community members for their contributions:

* @Calsign for improved Android support
* @alexdmiller for the `BeatDetection` analyzer class
* @icalvin102 for the `Waveform` analyzer class
* @defrost256
* @cluder

### How to build

1. `git clone git@github.com:processing/processing-sound.git`
2. (optional: copy (or soft-link) `processing-core.zip` from your local [Processing for Android mode](https://github.com/processing/processing-android/releases/tag/latest) as well as your Android SDK's `android.jar`, API level 26 or higher, into the `library/` folder. If you don't do this, these will be downloaded from GitHub instead. Note that as of version 2.2 the sound library is compiled against Processing's Android mode rather than the normal Processing `core.jar` in order to more smoothly support `AudioIn` on Android. Other dependencies, in particular Phil Burk's [JSyn](http://www.softsynth.com/jsyn/) engine on which this library is based, are also all downloaded automatically by ant.)
3. `ant dist` (or, alternatively, run `build.xml` from within Eclipse)

The resulting `sound.zip` can be extracted into your Processing installation's `libraries/` folder.

### License

LGPL v2.1
