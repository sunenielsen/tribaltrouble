#!/bin/sh
#
# NOTE: The Huge Island will require: -Xmx400m

java -ea -Djava.library.path=${PWD}/lib/native -Dorg.lwjgl.util.Debug=false -Xmx400m -cp .:./lib/'*' com.oddlabs.tt.Main --silent
