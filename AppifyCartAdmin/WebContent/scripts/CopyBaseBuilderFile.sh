#!/usr/bin/env bash
FILE_SRC="/AppifyCart/code/appifycart.appifycart-mobile/AndroidApp/moFluid/src/main/res/values/"
COPY_DEST="/AppifyCart/configFiles/androidConfigFiles/"
CONFIG_FILE="basicdata.xml"
FILENAME=$1
mkdir -p ${COPY_DEST}
cp ${FILE_SRC}${CONFIG_FILE} ${COPY_DEST}${FILENAME}.xml