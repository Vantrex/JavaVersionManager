#!/bin/bash

DIRECTORY="$HOME/.jvm"

function download_and_set_java_home() {
    # shellcheck disable=SC2155
    local arch=$(uname -m)
    local download_url=""
    local target_directory="$DIRECTORY/temp_jdk"  # Specify the target directory here
    mkdir "$target_directory"
    case "$arch" in
        "x86_64")
            download_url="https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u382-b05/OpenJDK8U-jdk_x64_linux_hotspot_8u382b05.tar.gz"
            ;;
        "aarch64")
            download_url="https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u382-b05/OpenJDK8U-jdk_aarch64_linux_hotspot_8u382b05.tar.gz"
            ;;
        "arm")
            download_url="https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u382-b05/OpenJDK8U-jdk_arm_linux_hotspot_8u382b05.tar.gz"
            ;;
        *)
            echo "Unsupported architecture: $arch"
            exit 1
            ;;
    esac

    # Download the OpenJDK binary
    wget -q --show-progress "$download_url"

    # Unpack the downloaded tar.gz file
    tar -xzvf OpenJDK*.tar.gz -C "$target_directory" >/dev/null 2>&1
    #local java_home="$target_directory/$(tar -tzf OpenJDK*.tar.gz | head -1 | cut -f1 -d'/')"
    # Set JAVA_HOME environment variable to the unpacked directory
    # shellcheck disable=SC2155
    export JAVA_HOME="$target_directory/$(tar -tzf OpenJDK*.tar.gz | head -1 | cut -f1 -d'/')"
    # Add JAVA_HOME to PATH
    export PATH="$JAVA_HOME/bin:$PATH"
    # Cleanup: Remove the downloaded tar.gz file
    rm OpenJDK*.tar.gz
    rm -rf "$target_directory"
}

echo 'Installing JVM for Linux..'
rm -rf "$DIRECTORY"
echo 'Creating directory..'
if [ ! -e "$DIRECTORY" ]; then
    mkdir "$DIRECTORY"
fi
if [ ! -e "$DIRECTORY/bin" ]; then
    mkdir "$DIRECTORY/bin"
fi
echo 'Created directory!'

cd ".."

# JDK 8 DOWNLOAD
# Check if the system is 64-bit x86 architecture
download_and_unpack_openjdk
if [ "$(uname -m)" == "x86_64" ]; then
    echo "64-bit x86 Operating System"
# Check if the system is 64-bit ARM architecture (aarch64)
elif [ "$(uname -m)" == "aarch64" ]; then
    echo "64-bit ARM (aarch64) Operating System"
# Check if the system is 32-bit ARM architecture (arm)
elif [ "$(uname -m)" == "arm" ]; then
    echo "32-bit ARM Operating System"
else
    echo "Unknown Architecture"
fi

echo 'Downloading OpenJDK..'
download_and_set_java_home
echo 'Downloaded OpenJDK!'
echo 'Building jar..'
#./gradlew shadowJar -q &> /dev/ null
./gradlew shadowJar
echo 'Built jar!'
echo 'Moving jar into home dir'
mv "build/libs/JavaVersionManager-1.0-SNAPSHOT-all.jar" "$DIRECTORY/bin/jvm.jar"
echo 'Cleanup..'
rm -rf build
echo 'Cleaned up!'
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
export JVM_DIR
PATH="$JVM_DIR/bin:$PATH"
export PATH' >> "$HOME/.profile"
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