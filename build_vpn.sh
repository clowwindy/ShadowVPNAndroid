#!/bin/sh

if [ a == a$ANDROID_NDK_HOME ]; then
    echo ANDROID_NDK_HOME is not set
    exit 1
fi

pushd jni/ShadowVPN || exit 1
./autogen.sh || exit 1

if [ ! -f shadowvpn-android-arm/lib/libshadowvpn.a ]; then
    dist-build/android-arm.sh || exit 1
fi
if [ ! -f shadowvpn-android-armv7/lib/libshadowvpn.a ]; then
    dist-build/android-armv7.sh || exit 1
fi
if [ ! -f shadowvpn-android-mips/lib/libshadowvpn.a ]; then
    dist-build/android-mips.sh || exit 1
fi
if [ ! -f shadowvpn-android-x86/lib/libshadowvpn.a ]; then
    dist-build/android-x86.sh || exit 1
fi

popd

pushd jni

install -d armeabi
install -d armeabi-v7a
install -d mips
install -d x86

install ShadowVPN/shadowvpn-android-arm/lib/libsodium.a armeabi
install ShadowVPN/shadowvpn-android-armv7/lib/libsodium.a armeabi-v7a
install ShadowVPN/shadowvpn-android-mips/lib/libsodium.a mips
install ShadowVPN/shadowvpn-android-x86/lib/libsodium.a x86

install ShadowVPN/shadowvpn-android-arm/lib/libshadowvpn.a armeabi
install ShadowVPN/shadowvpn-android-armv7/lib/libshadowvpn.a armeabi-v7a
install ShadowVPN/shadowvpn-android-mips/lib/libshadowvpn.a mips
install ShadowVPN/shadowvpn-android-x86/lib/libshadowvpn.a x86


$ANDROID_NDK_HOME/ndk-build || exit 1
popd

install -d app/src/main/jniLibs/armeabi
install -d app/src/main/jniLibs/armeabi-v7a
install -d app/src/main/jniLibs/mips
install -d app/src/main/jniLibs/x86

install libs/armeabi/libvpn.so app/src/main/jniLibs/armeabi
install libs/armeabi-v7a/libvpn.so app/src/main/jniLibs/armeabi-v7a
install libs/mips/libvpn.so app/src/main/jniLibs/mips
install libs/x86/libvpn.so app/src/main/jniLibs/x86

