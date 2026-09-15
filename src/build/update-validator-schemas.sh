#!/usr/bin/env bash

set -euo pipefail

# Config
#-----------------------------------------------------
SCHEMA_DIR="src/main/resources/com/adobe/epubcheck/schema/30/mod"
SCHEMA_MODULES="html5 svg11 its2"



# Base directories
#-----------------------------------------------------

# Command name, used for usage help
CMD=$0
# Base directory of this script
BASEDIR=$(readlink -f "$(dirname "$0")")
# Working directory where the command was invoked
OWD=$PWD



# Output utils
#-----------------------------------------------------

# colors
color_pfx="\e[1;35m"
color_hl="\e[1;36m"
color_ok="\e[1;32m"
color_warn="\e[2;33m"
color_error="\e[1;31m"
color_end="\e[0m"
checkmark="${color_ok}\xE2\x9C\x94${color_end}"
# Pretty-print step
function step {
  printf "${color_pfx}==>${color_end} $*\n"
}
# Pretty-print warning
function info {
  local IFS=
  while read -r line; do
    printf "$line\n" >>/dev/stderr
  done <<< "$*"
}
function warn {
  local IFS=
  while read -r line; do
    printf "${color_warn}!!!${color_end}  $line\n" >>/dev/stderr
  done <<< "$*"
}
# Print error and exit
function fatal {
  cd "$OWD"
  local IFS=
  printf "${color_error}Error: ${color_end}"
  while read -r line; do
    printf "$line\n" >>/dev/stderr
  done <<< "$*"
  exit 1
}
# Print debug message and optionally exit if first argument is true
function debug {
  cd "$OWD"
  local abort
  if [ "$1" = true ]; then
    abort=true
    shift
  fi
  echo "DEBUG: $*"
  [ "$abort" = true ] && exit
}
# Print message and exit
function debugx {
  debug true $*
}


# Set EPUBCheck directories
#-----------------------------------------------------

## Check we're in a git repo, and move to the repo’s root
cd "$BASEDIR"
if ! git -C . rev-parse 2>/dev/null; then
  fatal "not a git repository"
fi

EPUBCHECK_DIR=$(git rev-parse --show-toplevel)
SCHEMA_DIR="$EPUBCHECK_DIR/$SCHEMA_DIR"
VALIDATOR_DIR="$EPUBCHECK_DIR/target/validator.nu"

cd "$EPUBCHECK_DIR"



# Clone validator.nu repo (if needed)
#-----------------------------------------------------

if [ ! -d "$VALIDATOR_DIR" ]; then
  step "Cloning the validator.nu repository"
  mkdir -p "target"
  git clone https://github.com/validator/validator.git $VALIDATOR_DIR
fi

cd "$VALIDATOR_DIR"



# Fetch latest commits
#-----------------------------------------------------

step "Fetching new commits from validator.nu"

if ! git fetch; then
  fatal "failed to fetch latest commits."
fi



# Get the current and target update revisions
#-----------------------------------------------------

step "Getting update revisions"

## - get the stored last update tag or SHA
LAST_UPDATE_FILE="$SCHEMA_DIR/html5/LAST_UPDATE"
if [ ! -f "$LAST_UPDATE_FILE" ]; then
  fatal "could not find the file storing the last update revision"
fi
OLD_REV=$(cat "$LAST_UPDATE_FILE")

## - get the newest tag or SHA
NEW_REV=$(git describe --tags --abbrev=0 main)
HEAD=$(git rev-list -n 1 main)
if [ ! $(git rev-list -n 1 $NEW_REV) = $HEAD ]; then
  NEW_REV=$HEAD
fi
if [ "$NEW_REV" == "$OLD_REV" ] ; then
  step "Already up to date ${checkmark}"
  exit 0
else
  info "Update from ${color_hl}$OLD_REV${color_end} to ${color_hl}$NEW_REV${color_end}"
fi




# Create patch files
#-----------------------------------------------------

step "Creating pach files for changes since the last update"

## create the patches
if ! git format-patch $OLD_REV..$NEW_REV $(echo "$SCHEMA_MODULES" | sed 's/[^ ]* */schema\/&/g'); then
  fatal "could not create the patch files"
fi
if ! ls *.patch 2> /dev/null; then
  step "No changes since last update ${checkmark}"
  exit 0
fi
## move the patches to the schema directory
mv *.patch "$SCHEMA_DIR"
## move to the schema directory
cd "$SCHEMA_DIR"



# Adapt the patch files for EPUBCheck
#-----------------------------------------------------

step "Adapting the patch files location and data types"
if ! sed -i "" -E \
  -e "/^(diff|---|\+\+\+) / s:/schema/:/src/main/resources/com/adobe/epubcheck/schema/30/mod/:g"\
  -e "s|^datatypes w = \"http://whattf.org/datatype-draft\"||"\
  -e "s/w:a-rel/datatype.html5.space.separated.tokens/g"\
  -e "s/w:autocomplete-any/datatype.string/g"\
  -e "s/w:browsing-context-or-keyword/datatype.html5.browsing.context.name.or.keyword/g"\
  -e "s/w:browsing-context/datatype.html5.browsing.context.name/g"\
  -e "s/w:charset/datatype.character.encoding/g"\
  -e "s/w:circle/datatype.html5.coords.circle/g"\
  -e "s/w:color/datatype.html5.color/g"\
  -e "s/w:content-security-policy/datatype.string/g"\
  -e "s/w:custom-element-name/datatype.string/g"\
  -e "s/w:datetime-local/datatype.html5.datetime.local/g"\
  -e "s/w:datetime-tz/datatype.html5.datetime/g"\
  -e "s/w:date/datatype.html5.date/g"\
  -e "s/w:email-address-list/datatype.html5.emailaddresslist/g"\
  -e "s/w:email-address/datatype.html5.emailaddress/g"\
  -e "s/w:float-positive/datatype.float.positive/g"\
  -e "s/w:float-non-negative/datatype.float.non-negative/g"\
  -e "s/w:float/datatype.float/g"\
  -e "s/w:hash-name/datatype.html5.name.reference/g"\
  -e "s/w:IDREFS/datatype.html5.space.separated.tokens/g"\
  -e "s/w:IDREF/datatype.html5.token/g"\
  -e "s/w:ID/datatype.html5.token/g"\
  -e "s/w:integer-positive/datatype.positive.integer/g"\
  -e "s/w:integer-non-negative/datatype.non-negative.integer/g"\
  -e "s/w:integer/datatype.integer/g"\
  -e "s/w:integrity-metadata/datatype.string/g"\
  -e "s/w:iri-ref-http-or-https/datatype.URI.absolute/g"\
  -e "s/w:iri-ref/datatype.IRI/g"\
  -e "s/w:iri/datatype.URI.absolute/g"\
  -e "s/w:keylabellist/datatype.html5.accesskey/g"\
  -e "s/w:language/datatype.languagecode/g"\
  -e "s/w:link-rel/datatype.html5.space.separated.tokens/g"\
  -e "s/w:media-query/datatype.html5.media.query/g"\
  -e "s/w:meta-charset/datatype.string/g"\
  -e "s/w:microdata-property/datatype.string/g"\
  -e "s/w:mime-type-list/datatype.html5.forms.mimetypelist/g"\
  -e "s/w:mime-type/datatype.mimetype/g"\
  -e "s/w:month/datatype.html5.month/g"\
  -e "s/w:non-empty-string/datatype.string.nonempty/g"\
  -e "s/w:polyline/datatype.html5.coords.poly/g"\
  -e "s/w:rectangle/datatype.html5.coords.rectangle/g"\
  -e "s/w:refresh/datatype.string/g"\
  -e "s/w:rel-value/datatype.string/g"\
  -e "s/w:sandbox-allow-list/datatype.html5.sandboxallowlist/g"\
  -e "s/w:script-type/datatype.string/g"\
  -e "s/w:simple-color/datatype.html5.color/g"\
  -e "s/w:source-size-list/datatype.string/g"\
  -e "s/w:string-without-line-breaks/datatype.html5.forms.string.nolinebreaks/g"\
  -e "s/w:string //g"\
  -e "s/w:svg-pathdata/datatype.string/g"\
  -e "s/w:time-datetime/datatype.html5.date.or.time/g"\
  -e "s/w:time/datatype.html5.time/g"\
  -e "s/w:week/datatype.html5.week/g"\
  -e "s/w:xml-name/datatype.html5.token/g"\
  -e "s/w:zero/\"0\"/g"\
  *.patch; then
  fatal "could not edit the patch files"
fi



# Apply the patch files
#-----------------------------------------------------

step "Applying the patches"

SHA_BEFORE_PATCH=$(git rev-list -n 1 HEAD)
if ! git am --whitespace nowarn *.patch; then
  warn "
WARNING
  Make sure to apply the patches carefully, and finalize manually.
"
  exit
fi
SHA_AFTER_PATCH=$(git rev-list -n 1 HEAD)



# Update and commit the LAST_UPDATE file
#-----------------------------------------------------

step "Updating the LAST_UPDATE file"
echo $NEW_REV > $LAST_UPDATE_FILE
git add $LAST_UPDATE_FILE
git commit -m "tmp: update LAST_UPDATE file"



# Build the commit message
#-----------------------------------------------------

step "Creating the consolidated commit message"

## Build a commit message
## - in chronological order (reverse log order)
## - with proper credits to original committers
## - replacing issue references to the validator repo
printf "feat: update schemas from validator.nu ${NEW_REV:0:8}\n\n" > commit-message.txt
git log --reverse \
 --pretty=format:"---------------------%n\
 %n\
 %s%n\
 %n\
 https://github.com/validator/validator/commit/%h%n\
 %n\
 Co-authored-by: %aN <%aE>%n" \
 $SHA_BEFORE_PATCH..$SHA_AFTER_PATCH \
 | sed -E "s/#([0-9]+)/validator\/validator\/#\1/" >> commit-message.txt


# Squash and commit
#-----------------------------------------------------

step "Consolidating the commits"
git reset --soft $SHA_BEFORE_PATCH
git commit -F commit-message.txt


# Clean up
#-----------------------------------------------------
step "Cleaning the patch files"
rm *.patch
rm commit-message.txt


step "Done! ${checkmark}"
