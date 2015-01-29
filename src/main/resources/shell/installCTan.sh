#!/bin/bash

## This script navigates to the tlmgr script and installs the given parameters
#
BASEDIR=$(dirname $0)
cd "$BASEDIR"
cd $1
cd bin/x86*
cat "$2" | xargs ./tlmgr install