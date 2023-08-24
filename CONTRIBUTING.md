## Contributing

Pull requests for bug fixes as well as new features and example sketches are always welcome! Check the open [issues](https://github.com/processing/processing-sound/issues) if you don't know where to start.

## Developing locally

### How to build

1. `git clone git@github.com:processing/processing-sound.git`
2. (optional: copy (or soft-link) `processing-core.zip` from your local [Processing for Android mode](https://github.com/processing/processing-android/releases/tag/latest) as well as your Android SDK's `android.jar`, API level 26 or higher, into the `library/` folder. If you don't do this, these will be downloaded from GitHub instead. Note that as of version 2.2 the sound library is compiled against Processing's Android mode rather than the normal Processing `core.jar` in order to more smoothly support `AudioIn` on Android. Other dependencies, in particular Phil Burk's [JSyn](http://www.softsynth.com/jsyn/) engine on which this library is based, are also all downloaded automatically by ant.)
3. `ant dist` (or, alternatively, run `build.xml` from within Eclipse)

The resulting `sound.zip` can be extracted into your Processing installation's `libraries/` folder.

For faster developing, we recommend using the `ant quickinstall` target.

## For maintainers: how to create new releases

### Preparing the sound.zip and sound.txt files

In the `library.properties` file:

- [ ] increase the `version` field by one
- [ ] change the `prettyVersion` field to whatever you like/is meaningful

Before pushing a new `v*` tag to Github, create and test a local library build with

```
ant dist
```

### Automatically creating a new version release with Github actions

```
git tag v<prettyVersion> [<commit>]
git push --tags
```

A Github action will take care of packaging up the library and create a new draft release. Go to the [releases page](https://github.com/processing/processing-sound/releases) page to double-check that the files have been added correctly, edit the release text to your liking, and set the release to `published`.

### Making the new release the default download from Processing's Contribution Manager

#### Manually

If you prefer, the assets of the [`latest`](https://github.com/processing/processing-sound/releases/tag/latest) tag can simply be uploaded manually, without changing the underlying git tag reference.

#### Automatically

```
git tag -f latest [<commit>]
git push --tags --force
```

Check the library's [`latest`](https://github.com/processing/processing-sound/releases/tag/latest) tag
