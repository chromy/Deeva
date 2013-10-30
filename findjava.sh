#!/bin/bash

# Find the `javac' executable - => requires JDK
JAVAC=$(command -v javac)
if [ -z $JAVAC ]; then
    exit 10; # exit code 10 => no JDK
fi 

# Get the JDK home directory
JAVA_HOME=$(readlink -e $JAVAC | sed "s:bin/javac::")
if [ -z $JAVA_HOME ]; then
    exit 11; # exit code 11 => jdk home is weird somehow
fi

# Find the JDI tools 
JDI_TOOLS=$(find $JAVA_HOME -iname 'tools.jar' | head -n 1)
if [ -z $JDI_TOOLS ]; then
    exit 12; # exit code 12 => no tools.jar
fi
echo $JDI_TOOLS


