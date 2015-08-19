#!/bin/sh

mkdir -p $HOME/ndk

cd $HOME/ndk

if [ ! -f "$HOME/ndk/android-ndk-r10e-linux-x86_64.bin" ]; then
    wget http://dl.google.com/android/ndk/android-ndk-r10e-linux-x86_64.bin
    chmod +x android-ndk-r10e-linux-x86_64.bin
fi
./android-ndk-r10e-linux-x86_64.bin >$HOME/ndk.log 2>&1
mv android-ndk-r10e $HOME
cd -
