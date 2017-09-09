#!/usr/bin/env bash
GRADLEHOME="/gradle/gradle-2.10/bin/gradle"
PROJDIR="/AppifyCart/code/appifycart.appifycart-mobile/AndroidApp"
PROJECT_NAME="Mofluid"
PROJECT_BUILDDIR="${PROJDIR}/moFluid/build/outputs/apk"
APPLICATION_NAME=$1
BUILD_HISTORY_DIR=$2
KEY_STORE=$3
STORE_PASS=$4
ALIAS_NAME=$5
BUNDLE_ID=$6
ICON_FILE=$7
SPLASH_FILE=$8
BUILD_ID=${9}
BASE_URL=${10}
AUTH_KEY=${11}
AUTH_SECRET=${12}
BUILD_VERSION=${13}
APP_LANGUAGE=${14}
changeDir(){
	mkdir -p "${BUILD_HISTORY_DIR}"
	cd "${PROJDIR}"
}

gitClean(){
	git stash
}

configureApp(){
	sed -i  "s/app_name_value/${APPLICATION_NAME}/" ./moFluid/src/main/res/values/basicdata.xml
	sed -i  "s,base_url_value,${BASE_URL}," ./moFluid/src/main/res/values/basicdata.xml
	sed -i  "s/auth_id_value/${AUTH_KEY}/" ./moFluid/src/main/res/values/basicdata.xml
	sed -i  "s/auth_secret_value/${AUTH_SECRET}/" ./moFluid/src/main/res/values/basicdata.xml
	sed -i  "s/app_language_value/${APP_LANGUAGE}/" ./moFluid/src/main/res/values/basicdata.xml
	sed -i  "s/applicationId_value/${BUNDLE_ID}/" ./moFluid/build.gradle
	sed -i  "s/version_code_value/${BUILD_VERSION}/" ./moFluid/src/main/AndroidManifest.xml
	sed -i  "s/applicationId_value/${BUNDLE_ID}/" ./moFluid/src/main/AndroidManifest.xml
	sed -i  "s/com.appify.cart/${BUNDLE_ID}/" ./moFluid/google-services.json
}

changeIcons(){
	convert "${ICON_FILE}" -resize 48x48 "${PROJDIR}/moFluid/src/main/res/drawable/appicon.png"
	convert "${ICON_FILE}" -resize 48x48 "${PROJDIR}/moFluid/src/main/res/drawable-mdpi/appicon.png"
	convert "${ICON_FILE}" -resize 72x72 "${PROJDIR}/moFluid/src/main/res/drawable-hdpi/appicon.png"
	convert "${ICON_FILE}" -resize 96x96 "${PROJDIR}/moFluid/src/main/res/drawable-xhdpi/appicon.png"
	convert "${ICON_FILE}" -resize 144x144 "${PROJDIR}/moFluid/src/main/res/drawable-xxhdpi/appicon.png"
}

convertSplashScreen(){
	convert "${SPLASH_FILE}" -resize $1 $2
 	mogrify -extent $1 -gravity Center -fill white $2
}

changeSplashScreen(){
	convertSplashScreen 480x800 "${PROJDIR}/moFluid/src/main/res/drawable/screen.png"
	convertSplashScreen 320x480 "${PROJDIR}/moFluid/src/main/res/drawable-mdpi/screen.png"
	convertSplashScreen 480x800 "${PROJDIR}/moFluid/src/main/res/drawable-hdpi/screen.png"
	convertSplashScreen 720x1280 "${PROJDIR}/moFluid/src/main/res/drawable-xhdpi/screen.png"
	convertSplashScreen 960x1600 "${PROJDIR}/moFluid/src/main/res/drawable-xxhdpi/screen.png"
	convertSplashScreen 800x480 "${PROJDIR}/moFluid/src/main/res/drawable-land/screen.png"
	convertSplashScreen 320x200 "${PROJDIR}/moFluid/src/main/res/drawable-land-ldpi/screen.png"
	convertSplashScreen 480x320 "${PROJDIR}/moFluid/src/main/res/drawable-land-mdpi/screen.png"
	convertSplashScreen 800x480 "${PROJDIR}/moFluid/src/main/res/drawable-land-hdpi/screen.png"
	convertSplashScreen 1280x720 "${PROJDIR}/moFluid/src/main/res/drawable-land-xhdpi/screen.png"
	convertSplashScreen 1600x960 "${PROJDIR}/moFluid/src/main/res/drawable-land-xxhdpi/screen.png"
	convertSplashScreen 1920x1280 "${PROJDIR}/moFluid/src/main/res/drawable-land-xxxhdpi/screen.png"
}

createBuild(){
	${GRADLEHOME}  clean assembleRelease --info
}

signApp(){
	jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore "${KEY_STORE}" -storepass "${STORE_PASS}" "${PROJECT_BUILDDIR}/moFluid-release-unsigned.apk" "${ALIAS_NAME}"

	APP_NAME="${BUILD_HISTORY_DIR}/${APPLICATION_NAME}_${BUILD_ID}.apk"
	$ANDROID_HOME/build-tools/23.0.2/zipalign -v 4 "${PROJECT_BUILDDIR}/moFluid-release-unsigned.apk" "${APP_NAME}"
}

echo "Changing Directory"
changeDir

echo "Clean git"
gitClean

echo "Configuring App"
configureApp

echo "Changing Icon"
changeIcons

echo "Changing Splash(Lanuch) screen"
changeSplashScreen

echo "Creating Build"
createBuild

echo "Signing App"
signApp
