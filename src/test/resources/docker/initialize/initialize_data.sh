#!/bin/sh
BASEDIR=$(dirname $0)

####################### UCD server data

curl -u admin:admin \
    -H "Accept: application/json" \
    -H "Content-type: application/json" \
    -X POST \
    -d @$BASEDIR/data/server-configs.json \
    http://localhost:5516/repository/cis