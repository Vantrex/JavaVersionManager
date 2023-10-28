#!/bin/bash
echo 'Cloning repository'
git clone git@github.com:Vantrex/JavaVersionManager.git JavaVersionManager > /dev/null 2>&1
cd JavaVersionManager || exit &&
bash install.sh
cd '..'
rm -rf 'JavaVersionManager'