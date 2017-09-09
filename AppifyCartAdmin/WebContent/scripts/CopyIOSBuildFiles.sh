#!/usr/bin/env bash
#SERVER="admin@74.80.245.242"
SERVER="macoo8@125.63.92.226"
DESTINY=$1
SRC=$1
FILE=$2
echo $SERVER
echo $SRC
echo $FILE
ssh "$SERVER" "mkdir -p $DESTINY" && scp -r "$SRC$FILE" "$SERVER:$DESTINY"
