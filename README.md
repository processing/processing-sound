## Processing Sound library

The new Sound library for Processing 3 provides a simple way to work with audio. It can play, analyze, and synthesize sound. The library comes with a collection of oscillators for basic wave forms, a variety of noise generators, and effects and filters to alter sound files and other generated sounds. The syntax is minimal to make it easy for beginners who want a straightforward way to add some sound to their Processing sketches!

### How to use

The easiest way to install the Sound library is through Processing's Contribution Manager. The library comes with many example sketches, the full online reference can be found [here](https://www.processing.org/reference/libraries/sound/). Please report bugs [https://github.com/processing/processing-sound/issues](here).

For detailed changelogs and to download older releases, have a look at the [Github releases page](https://github.com/processing/processing-sound/releases).

### How to build

1. `git clone git@github.com:processing/processing-sound.git`
2. into the `library/` folder copy (or soft-link) your Processsing's `core.jar` (and, optionally, also your Android SDK's `android.jar`, API level 26 or higher). Other dependencies (in particular Phil Burk's [JSyn](http://www.softsynth.com/jsyn/) engine on which this library is based) are downloaded automatically.
3. `ant dist` (or, alternatively, run build.xml from within Eclipse)

The resulting `sound.zip` can be extracted into your Processing installation's `libraries/` folder.

### License

LGPL v2.1
