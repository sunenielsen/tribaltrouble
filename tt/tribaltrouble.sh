#!/bin/sh
#
# NOTE: The Huge Island will require: -Xmx400m

if [ "$1" != "" ] && [ -e $1 ]; then
	cd $1
fi

JPATH=$(find /opt -maxdepth 1 -type d -name icedtea-bin-6\*)

${JPATH}/jre/bin/java -ea -Djava.library.path=${HOME}/tt-ant/native -Dorg.lwjgl.util.Debug=false -Xmx400m -cp ${HOME}/tt-ant/'*':. com.oddlabs.tt.Main --silent
