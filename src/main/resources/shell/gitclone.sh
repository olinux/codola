#!/bin/sh
mkdir -p $1
cd $1
git clone $2 --single-branch $3