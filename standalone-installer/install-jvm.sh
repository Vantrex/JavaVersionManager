#!/bin/bash
git clone git@github.com:Vantrex/JDKSwitcher.git
cd JDKSwitcher || exit &&
bash install.sh
cd '..'
rm -r 'JDKSwitcher'