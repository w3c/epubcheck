#!/bin/bash

echo "Copying Maven settings to '$HOME/.m2/settings.xml'"
cp $TRAVIS_BUILD_DIR/.travis/settings.xml $HOME/.m2/settings.xml
