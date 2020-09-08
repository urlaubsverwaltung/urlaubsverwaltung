#!/usr/bin/env bash

BASEDIR=$(dirname "$0")
echo "$BASEDIR"

function copy() {
  FILENAME=$1
  mkdir -p "$BASEDIR/../.git/hooks"
  cp "$BASEDIR/githooks/$FILENAME" "$BASEDIR/../.git/hooks/"
}

copy "post-merge"

echo "sucessfully installed git hooks for you 😽"
