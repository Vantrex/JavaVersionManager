#!/bin/bash
echo 'Cloning repository'
git clone git@github.com:Vantrex/JDKSwitcher.git JDKSwitcher > /dev/null 2>&1
cd JDKSwitcher || exit &&
bash install.sh
cd '..'
rm -rf 'JDKSwitcher'