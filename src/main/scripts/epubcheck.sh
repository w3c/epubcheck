#!/bin/bash

# If JAVA_HOME is not set, use the java in the execution path
if [ -z "$JAVA_HOME" ] ; then
  JAVA=java
else
  JAVA="$JAVA_HOME/bin/java"
fi

EPUBCHECK_HOME=$(dirname "$0")

"${JAVA}" -jar "$EPUBCHECK_HOME/epubcheck-${project.version}.jar" "$@"

