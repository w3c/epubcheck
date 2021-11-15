#!/bin/bash

# Command name, used for usage help
CMD=$0
# Working directory where the command was invoked
OWD=$PWD

# Print error and exit
function error_exit {
  cd "$OWD"
  echo "Error: $*" >>/dev/stderr
  exit 1
}

## Check the argument is present, is a directory, and is a git repo
if [ -z $1 ]; then
  error_exit "the directory of validator.nu is required as argument"
elif [ ! -d "$1" ]; then
  error_exit "'$1' is not a directory"
elif ! git -C "$1" rev-parse 2>/dev/null; then
  error_exit "'$1' is not a git repository"
fi
VALIDATOR_DIR=`realpath "$1"`

## Check we're in a git repo, and move to the repo’s root
cd "$OWD"
if ! git -C . rev-parse 2>/dev/null; then
  error_exit "This command must be executed from the EPUBCheck git repository"
fi
EPUBCHECK_DIR=`git rev-parse --show-toplevel`
cd "$EPUBCHECK_DIR"

## Get the SHA-1 of the validator commit used for the last update
LAST_UPDATE_FILE=$EPUBCHECK_DIR/src/main/resources/com/adobe/epubcheck/schema/30/mod/html5/LAST_UPDATE
if [ ! -f "$LAST_UPDATE_FILE" ]; then
  error_exit "could not find the last commit sha-1; make sure you are in the EPUBCheck git repository"
fi
LAST_SHA1=`cat $LAST_UPDATE_FILE`

## Move to validator.nu repo
echo "Moving to the validator.nu repository"
cd "$VALIDATOR_DIR"

## Check we're in the validator repo and get the remote for validator.nu 
for remote in `git remote`; do
  if test `git remote get-url $remote` = "https://github.com/validator/validator.git"; then
    UPSTREAM=$remote
  fi
done
if [ -z $UPSTREAM ]; then
  error_exit "could not find the validator.nu URL in the repository remotes"
fi

## Fetch remote new commits
echo "Fetching new commits from remote '$UPSTREAM'"
git fetch $UPSTREAM

## Create patches since last update (to the `latest` tag)
echo "Creating paches of changes since the last update"
git format-patch -o $EPUBCHECK_DIR $LAST_SHA1..latest schema

## Get the SHA-1 of the `latest` tag
LATEST=`git rev-list -n 1 latest`
echo $LATEST

## Move to EPUBCheck repo
echo "Moving back to the EPUBCheck repository"
cd "$EPUBCHECK_DIR" # let's move to the validator repo

## Edit the LAST_UPDATE file
echo $LATEST > $LAST_UPDATE_FILE
git add $LAST_UPDATE_FILE
git ci -m "feat: update to latest schemas from the Nu HTML Checker"

## Update the paths in patches to the EPUBCheck location
echo "Updating paths in patch files"
sed -i "" -E  "/^(diff|---|\+\+\+) / s:/schema/:/src/main/resources/com/adobe/epubcheck/schema/30/mod/:g" *.patch

## Apply the patches, interatively
echo "Applying the patches"
git am *.patch

## FROM HERE ON, THE INTERACTIVE APPLICATION OF PATCHES MAY EXIT THE SCRIPT

# Remaing tasks:
# - commit new last commit ID
# - squash commits (add "Co-Authored by" credits)
# - remove patch files