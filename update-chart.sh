#!/bin/bash

set -veu
set -o pipefail

temp_repo=$(mktemp -d -t urlaubsverwaltung-chart.XXX)
function cleanup {
    rm -rf "$temp_repo"
}
trap cleanup EXIT

REV="$1"
if [ -z "$REV" ]; then
    echo "Expected revision first argument" >&2
    exit 1
fi

git clone -b "$REV" https://github.com/synyx/urlaubsverwaltung "$temp_repo"
helm package -d chart_repo -u "$temp_repo/.examples/kubernetes/chart/urlaubsverwaltung/"
helm repo index chart_repo --url https://synyx.github.io/urlaubsverwaltung/chart_repo/ --merge chart_repo/index.yaml

