#!/bin/bash


cd "scripts" || exit
if [[ "$OSTYPE" == "linux-gnu" ]]; then
    bash linux-install.sh
elif [[ "$OSTYPE" == "darwin"* ]]; then
    echo "macOS"
elif [[ "$OSTYPE" == "cygwin" || "$OSTYPE" == "msys" ]]; then
    echo "Windows (Cygwin/MSYS)"
elif [[ "$OSTYPE" == "win32" ]]; then
    echo "Windows"
else
    echo "Betriebssystem nicht erkannt: $OSTYPE"
fi
