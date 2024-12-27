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
SHA_LAST_UPDATE=`cat $LAST_UPDATE_FILE`

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

## Get the SHA-1 of the SHA_VALIDATOR_LATEST revision on upstream
SHA_VALIDATOR_LATEST=`git rev-list -n 1 $UPSTREAM/main`
echo $SHA_VALIDATOR_LATEST

## Create patches since last update (to the `SHA_VALIDATOR_LATEST` tag)
echo "Creating paches of changes since the last update"
git format-patch -o $EPUBCHECK_DIR/ $SHA_LAST_UPDATE..$SHA_VALIDATOR_LATEST schema

## Move to EPUBCheck repo
echo "Moving back to the EPUBCheck repository"
cd "$EPUBCHECK_DIR" # let's move to the validator repo

## Edit the LAST_UPDATE file
echo $SHA_VALIDATOR_LATEST > $LAST_UPDATE_FILE
git add $LAST_UPDATE_FILE
git ci -m "feat: update to latest schemas from the Nu HTML Checker"
SHA_BEFORE_PATCH=`git rev-list -n 1 HEAD`

## Update the paths in patches to the EPUBCheck location
echo "Updating paths in patch files"
sed -i "" -E  "/^(diff|---|\+\+\+) / s:/schema/:/src/main/resources/com/adobe/epubcheck/schema/30/mod/:g" *.patch

## Apply the patches, interatively
echo "Applying the patches"
git am *.patch

###
echo ""
echo "WARNING! From here on, the interactive application of patches may exit the script."
echo ""
###

SHA_AFTER_PATCH=`git rev-list -n 1 HEAD`

## Build a commit message
## - with proper credits to original committers
## - replacing issue references to the validator repo
echo "Creating the consolidated commit message"
printf "feat: update to latest schemas from the Nu HTML Checker\n\n" > commit-message.txt
git log \
 --pretty=format:"---------------------%n%n%s%n%nhttps://github.com/validator/validator/commit/%h%n%nCo-authored-by: %aN <%aE>%n" \
 $SHA_BEFORE_PATCH..$SHA_AFTER_PATCH \
 | sed -E "s/#([0-9]+)/validator\/validator\/#\1/" >> commit-message.txt

## Squash the patch commits
echo "Consolidating the commits"
git reset --soft $SHA_BEFORE_PATCH~1
git ci -F commit-message.txt

## Clean up the files
echo "Cleaning the patch files"
rm *.patch
rm commit-message.txt
