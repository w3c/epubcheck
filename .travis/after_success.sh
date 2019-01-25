#!/bin/bash

if [[ "$TRAVIS_PULL_REQUEST" == "false" && "$TRAVIS_JOB_NUMBER" == *.1 ]]; then
  mvn deploy -DskipTests -Dinvoker.skip=true
else
  echo "Skipping Maven deploy."
fi
