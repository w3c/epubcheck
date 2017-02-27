#!/bin/bash

# bash script to update and normalize
# pulled transifex properties files
#
# Author:  Tobias Fischer (https://github.com/tofi86)
# Project: IDPF/EpubCheck (https://github.com/IDPF/epubcheck)
#
# Date: 2017-02-27
# License: MIT License
#

param1=$1


function escapeISO88591() {
  file=$1

  echo "- Escaping ISO-8859-1 encodings with Unicode escapes"
  native2ascii -encoding ISO-8859-1 ${file} ${file}

  # replace ' -> '' (#748)
  sed -E -i -- "s/'/''/g" ${file}
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
  file ${file} | grep 'ISO-8859' > /dev/null
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


# Show help if no language parameter is passed to the script or --help
if [[ -z ${param1} || ${param1} == "--help" ]] ; then
	echo "usage: transifex-pull.sh [--all | <2-digit-country-code>]"
	echo "examples:"
	echo "  transifex-pull.sh --all"
	echo "  transifex-pull.sh de"

# Pull ALL translations
elif [ ${param1} == "--all" ] ; then
	minimum_percent_translated=$(awk -F "=" '/minimum_perc/ {print $2}' .tx/config)
	echo "Pulling ALL epubcheck translations (>${minimum_percent_translated}% done) from Transifex..."
	echo ""
	tx pull -f | tee /dev/stderr | grep "> [a-z][a-z]: " | awk '{print $3}' | while read f; do processFile ${f}; done

# Pull translations for a 2-digit-language-code
elif [ ${#param1} -eq 2 ] ; then
	echo "Pulling epubcheck translation '${param1}' from Transifex..."
	echo ""
	tx pull -f -l ${param1} | tee /dev/stderr | grep "${param1}: " | awk '{print $3}' | while read f; do processFile ${f}; done

else
	echo "FATAL: Couldn't recognize language code '${param1}'. Exit."
	exit 1
fi
