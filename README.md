Xedroid
=======

Unofficial Android app for Xedule using the unofficial [Xedule API](https://github.com/Darkwater/xedule-api).


Building
--------

1. Clone repository.
2. Run `./gradlew installDebug` to install a debug build on any connected devices.

Note: `git describe --tags` is used for version number. Make sure `git` is accessible like that and your local
repository has the correct tags set. Try `git pull --tags` if something goes wrong. This might chance in the future.
Also, this might not work (properly) on Windows without Cygwin.
