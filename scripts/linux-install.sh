#!/bin/bash
echo 'Installing JVM for Linux..'
if [ ! -e "$HOME/jvm" ]; then
    mkdir ~/jvm
fi
cd ".."
echo 'Building jar..'
./gradlew shadowJar -q &> /dev/null
echo 'Built jar!'
echo 'Moving jar into home dir'
mv "build/libs/JDKSwitcher-1.0-SNAPSHOT-all.jar" "$HOME/jvm/jvm.jar"
echo 'Cleanup..'
rm -r build
echo 'Creating start file'
cd "$HOME/jvm" || exit
touch "test"
# shellcheck disable=SC2016
echo '#!/bin/sh
java -jar $HOME/jvm/jvm.jar $@
if [ "$#" -ge 1 ]; then
      source ~/.profile
fi' > 'test'
echo 'Modifying .bashrc..'
# shellcheck disable=SC2016
echo '
export JVM_DIR="$HOME/.jvm"
[ -s "$JVM_DIR/jvm" ] && \. "JVM_DIR/jvm" # This loads jvm' >> "$HOME/.bashrc"

source "$HOME/.bashrc"
echo 'Installed JVM for Linux!'
