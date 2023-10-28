#!/bin/bash
echo 'Installing JVM for Linux..'
DIRECTORY="$HOME/.jvm"
if [ ! -e "$DIRECTORY" ]; then
    mkdir "$DIRECTORY"
fi
if [ ! -e "$DIRECTORY/bin" ]; then
    mkdir "$DIRECTORY/bin"
fi
cd ".."
echo 'Building jar..'
#./gradlew shadowJar -q &> /dev/ null
./gradlew shadowJar
echo 'Built jar!'
echo 'Moving jar into home dir'
mv "build/libs/JavaVersionManager-1.0-SNAPSHOT-all.jar" "$DIRECTORY/bin/jvm.jar"
echo 'Cleanup..'
rm -r build
echo 'Creating start file'
cd "$DIRECTORY" || exit
# shellcheck disable=SC2016
echo '#!/bin/sh
java -jar '"$DIRECTORY"'/bin/jvm.jar $@
if [ "$#" -ge 1 ]; then
      source ~/.profile
fi' > 'bin/jvm'
chmod 777 jvm
echo 'Modifying .profile..'
# shellcheck disable=SC2016
if ! grep -q '# JVM installation' "$HOME/.profile"; then
    echo '
# JVM installation
JVM_DIR="$HOME/.jvm"
PATH="$JVM_DIR/bin:$PATH"' >> "$HOME/.profile"
fi
source "$HOME/.profile"
echo 'Modified .profile!'

if ! grep -q '# JVM Alias' "$HOME/.bashrc"; then
  echo 'Adding ". jvm" alias..'
  echo '
# JVM Alias
alias jvm=". jvm"' >> "$HOME/.bashrc"
echo "Added alias!"
fi


echo 'Installed JVM for Linux! Use "jvm" to run.'