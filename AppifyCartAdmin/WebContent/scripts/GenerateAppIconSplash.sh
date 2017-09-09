#!/bin/bash
IMAGE_DIR=$1
ICON_IMAGE_NAME=$2
ICON_FONT_SIZE=$3
SPLASH_IMAGE_NAME=$4
SPLASH_FONT_SIZE=$5
LABEL=$6
DEMO_SPLASH=/AppifyCart/uploadFiles/defaultBuildFiles/iosDemoSplash.png
DEMO_ICON=/AppifyCart/uploadFiles/defaultBuildFiles/iosDemoAppIcon.png
UBUNTU_FONT=/usr/share/fonts/truetype/ubuntu-font-family/Ubuntu-R.ttf
changeDir(){
 cd ${IMAGE_DIR}
}

generateAppIcon(){

    convert  ${DEMO_ICON} -gravity center -fill black -pointsize ${ICON_FONT_SIZE} -font ${UBUNTU_FONT} -draw "text 0,0 '  ${LABEL}  '" ${ICON_IMAGE_NAME}.png
}

generateAppSplash(){
	convert  ${DEMO_SPLASH} -gravity center -fill black -pointsize ${SPLASH_FONT_SIZE} -font ${UBUNTU_FONT} -draw "text 0,0 '  ${LABEL}  '" ${SPLASH_IMAGE_NAME}.png
}

echo "Changing Directory to save Images in path : "${IMAGE_DIR}
changeDir


echo "Genarting App Icon For Demo App : "${ICON_IMAGE_NAME}.png
generateAppIcon

echo "Genarting Splash Screen For Demo App : "${SPLASH_IMAGE_NAME}.png
generateAppSplash

#
# NOTE
# example running script
# ./generateImage.sh /Users/macoo8/scripts icon11 12 icon22 120 ABCDEFGHIJKL
#