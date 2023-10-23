#!/bin/bash
mkdir ~/jvm
cd ".."
./gradlew shadowJar
mv "build/libs/JDKSwitcher-1.0-SNAPSHOT-all.jar" "$HOME/jvm/jvm.jar"
#rm -r build