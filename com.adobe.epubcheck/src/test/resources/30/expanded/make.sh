#!/bin/sh

DIR="invalid/lorem-xhtml-rng-1"
OUTNAME="lorem-xhtml-rng-1"

cd $DIR

zip $OUTNAME.epub -X0D mimetype

zip $OUTNAME.epub -X9rD EPUB -x.svn

zip $OUTNAME.epub -X9rD META-INF -x.svn
