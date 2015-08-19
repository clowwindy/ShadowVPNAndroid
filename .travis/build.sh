#!/bin/bash
git submodule update --init --recursive

sed -i '1 i\SHELL := $(shell echo $$SHELL)' Makefile
sed -i 's/TARGET_ARCH=arm/TARCH=arm TARGET_ARCH=arm/g' jni/ShadowVPN/dist-build/android-arm.sh
sed -i 's/TARGET_ARCH=armv7/TARCH=arm TARGET_ARCH=armv7/g' jni/ShadowVPN/dist-build/android-armv7.sh
sed -i 's/TARGET_ARCH=mips/TARCH=mips TARGET_ARCH=mips/g' jni/ShadowVPN/dist-build/android-mips.sh
sed -i 's/TARGET_ARCH=x86/TARCH=x86 TARGET_ARCH=x86/g' jni/ShadowVPN/dist-build/android-x86.sh
sed -i 's/--arch="$TARGET_ARCH"/--arch="$TARCH"/g' jni/ShadowVPN/dist-build/android-build.sh

make || exit 1

#gradle clean build
gradle clean assembleDebug

ls -lR app/build/outputs

cp app/build/outputs/apk/app-debug.apk $HOME/ShadowVPN-v1.0.apk

