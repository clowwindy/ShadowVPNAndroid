#!/bin/sh

if [ a == a$ANDROID_NDK_HOME ]; then
    echo ANDROID_NDK_HOME is not set
    exit 1
fi

pushd jni/ShadowVPN || exit 1
./autogen.sh || exit 1
dist-build/android-armv7.sh || exit 1
popd

pushd jni
$ANDROID_NDK_HOME/ndk-build || exit 1
popd

install -d app/src/main/jniLibs/armeabi
install libs/armeabi/libvpn.so app/src/main/jniLibs/armeabi

