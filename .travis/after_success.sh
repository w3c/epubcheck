#!/bin/bash

if [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
  mvn deploy -DskipTests -Dinvoker.skip=true
else
  echo "Skipping Maven deploy."
fi
