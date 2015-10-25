#!/bin/sh

if [[ "$1" == "-h" || "$1" == "--help" ]]
then
    echo "-h shows this help text"
    echo "-d starts dragon after building"
    echo "-t copies product to /tmp/"
    exit
fi

set -e

echo "Building $(git describe --tags)..."
gradle --daemon clean assembleRelease
cd build/outputs/apk/
cp xedroid-release-unsigned.apk xedroid-release-unaligned.apk
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore ~/xedroid-release-key.keystore xedroid-release-unaligned.apk xedroid
/opt/android-sdk/build-tools/23.0.0/zipalign -f -v 4 xedroid-release-unaligned.apk xedroid-release-aligned.apk

[[ "$1" == "-d" ]] && dragon xedroid-release-aligned.apk
[[ "$1" == "-t" ]] && cp xedroid-release-aligned.apk /tmp/xedroid-release.apk
