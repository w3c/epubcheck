#!/bin/bash

# Mac only bash script to update and normalize
# Java properties files pulled from Transifex
#
# Author:  Tobias Fischer (https://github.com/tofi86)
# Project: W3C/EPUBCheck (https://github.com/w3c/epubcheck)
#
# Date: 2018-11-26
# License: MIT License

param1=$1


function escapeISO88591() {
  file=$1

  echo "- Escaping ISO-8859-1 encodings with Unicode escapes"
  native2ascii -encoding ISO-8859-1 ${file} ${file}

  # replace ' -> '' (#748) but not in lines starting with # (comments)
  sed -E -i -- "/^#/! s/'/''/g" ${file}
}

function removeJavaEscapes() {
  file=$1

  # replace \\ -> \, \` -> `, \= -> =, \: -> :, \! -> !
  sed -E -i -- 's/\\([\\`=:!])/\1/g' ${file}

  # make unicode escapes \u00fc uppercase \u00FC
  perl -i -pe 's/\\u([0-9a-f]{4})/\\u\U\1/g' ${file}

  # replace newlines in help_text
  sed -E -i -- '/^help_text/s/((\\n)+)/\1\\\'$'\n          /g' ${file}
  sed -E -i -- 's/^(          )([[:space:]]+)/\1\\\2/g' ${file}

  # remove temp file
  rm ${file}-- 2> /dev/null
}

function processFile() {
  file=$1

  echo ""
  echo "Processing file '${file}'"
  file ${file} | grep -e 'ISO-8859\|data' > /dev/null
  if [ $? -eq 0 ]; then
    escapeISO88591 ${file}
  fi

  removeJavaEscapes ${file}
}


# Check if this is running from repo root dir near the .tx/ folder
if [ ! -d .tx/ ] ; then
	echo "FATAL: You need to run this script from the repository's root directory!"
	echo "e.g. ./src/build/transifex-pull.sh --all"
	exit 1
fi


# Check for Transifex Commandline Client
if [ `which tx >/dev/null ; echo $?` -eq 1 ] ; then
	echo "FATAL: You need to install the Transifex Commandline Client first in order to run this script!"
	echo "Instructions: http://docs.transifex.com/client/setup/"
	exit 1
fi


# Check for Java 8 to export $JAVA_HOME for native2ascii binary
if /usr/libexec/java_home -v1.8 > /dev/null; then
	export JAVA_HOME=$(/usr/libexec/java_home -v1.8)
else
	echo "FATAL: You need to have JDK 8 installed in order to run this script!"
	exit 1
fi


# Show help if no language parameter is passed to the script or --help
if [[ -z ${param1} || ${param1} == "--help" ]] ; then
	echo "usage: transifex-pull.sh [--all | <locale>]"
	echo "examples:"
	echo "  transifex-pull.sh --all"
	echo "  transifex-pull.sh de"
	echo "  transifex-pull.sh ko_KR"

# Pull ALL translations
elif [ ${param1} == "--all" ] ; then
	minimum_percent_translated=$(awk -F "=" '/minimum_perc/ {print $2}' .tx/config)
	echo "Pulling *ALL* EPUBCheck translations (>${minimum_percent_translated}% done) from Transifex..."
	echo ""
	tx pull -f | tee /dev/stderr | egrep "> [a-z][a-z](_[A-Z][A-Z])?:" | awk '{print $NF}' | while read f; do processFile ${f}; done

# Pull translations for a specific locale
elif [[ ${param1} =~ ^[a-z][a-z](_[A-Z][A-Z])?$ ]] ; then
	echo "Pulling EPUBCheck translation '${param1}' from Transifex..."
	echo ""
	tx pull -f -l ${param1} | tee /dev/stderr | grep "${param1}: " | awk '{print $NF}' | while read f; do processFile ${f}; done

else
	echo "FATAL: Couldn't recognize language code '${param1}'. Exit."
	exit 1
fi
