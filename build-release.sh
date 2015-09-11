#!/bin/sh

set -e

gradle --daemon assembleRelease
cd build/outputs/apk/
cp xedroid-release-unsigned.apk xedroid-release-unaligned.apk
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore ~/xedroid-release-key.keystore xedroid-release-unaligned.apk xedroid
/opt/android-sdk/build-tools/23.0.0/zipalign -f -v 4 xedroid-release-unaligned.apk xedroid-release-aligned.apk
