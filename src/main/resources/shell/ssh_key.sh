#!/bin/sh
ssh-keygen -b 2048 -t rsa -C "codola" -f $1 -q -N ""
