#!/bin/bash

## This script takes one argument: the subdirectory name to which latex shall be installed.
#
BASEDIR=$(dirname $0)
cd "$BASEDIR"
echo "Install LaTeX"
sedString="s|\\\${latexFolder}|"$BASEDIR"/"$1"|g"
sed -i "$sedString" texlive.profile
wget http://mirror.ctan.org/systems/texlive/tlnet/install-tl-unx.tar.gz
tar xvfz install-tl-unx.tar.gz
mkdir latex
cd install-tl*
./install-tl --profile ../texlive.profile
cd ..
rm -rf install-tl*

