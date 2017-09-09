#!/bin/bash
PROJDIR="/AppifyCart/code/appifycart.appifycart-mobile/iOSApp"
PROJECT_NAME="Mofluid"
TARGET_SDK="iphoneos"
PROJECT_BUILDDIR="${PROJDIR}"
BUILD_HISTORY_DIR=$2
PROVISONNING_PROFILE=$3
APPLICATION_NAME=$1
CERTIFICATE=$4
PASSPHRASE=$5
BUNDLE_ID=$6
ICON_FILE=$7
SPLASH_FILE=$8
BUILD_ID=${9}
BASE_URL=${10}
AUTH_KEY=${11}
AUTH_SECRET=${12}
BUILD_VERSION=${13}
BUILD_TYPE=${14}
DEFAULT_LANGUAGE=${15}
KEYCHAIN_NAME="${PROJECT_NAME}.keychain"
KEYCHAIN_TIME=7200
developerName=""
UUIDStr=""


renameProvisioningProfile(){
    UUIDKeyLineNo="$(awk '/UUID/{ print NR; exit }' ${PROVISONNING_PROFILE})"
    UUIDValueLineNo="$((UUIDKeyLineNo + 1))"

    UUIDContaingStr="$(awk NR==${UUIDValueLineNo} ${PROVISONNING_PROFILE})"
    UUIDStr="$(sed -ne '/string/{s/.*<string>\(.*\)<\/string>.*/\1/p;q;}' <<< ${UUIDContaingStr})"

    cp -v "${PROVISONNING_PROFILE}" ~/Library/MobileDevice/Provisioning\ Profiles/${UUIDStr}.mobileprovision
}

findDeveloperName(){
    tempKeyChainName="${BUNDLE_ID}.keychain"
    security delete-keychain "${tempKeyChainName}"
    security create-keychain -p 123456 "${tempKeyChainName}"
    security -v unlock-keychain -p 123456 "${tempKeyChainName}"
    security -v list-keychains -d system -s "${tempKeyChainName}"
    security default-keychain -s "${tempKeyChainName}"
    security list-keychains -s "${tempKeyChainName}"
    security set-keychain-settings -t "${KEYCHAIN_TIME}" -u "${tempKeyChainName}"
    oldNames="$(certtool y k=${tempKeyChainName} | grep 'iPhone Distribution\|iPhone Developer'|awk -F "Common Name     : " '{print $2}' |sort)"
    security import "${CERTIFICATE}" -k "${tempKeyChainName}" -P "${PASSPHRASE}" -A
    newNames="$(certtool y k=${tempKeyChainName} | grep 'iPhone Distribution\|iPhone Developer'|awk -F "Common Name     : " '{print $2}' |sort)"

    developerName="$(comm -23 <( echo "${newNames}" ) <( echo "${oldNames}" ))"

    security delete-keychain "${tempKeyChainName}"
}

changeDir(){
    mkdir -p "${BUILD_HISTORY_DIR}"
    cd "${PROJDIR}"
}

cleanGITCode(){
    git stash
}

replaceDevelopmentTeam(){
    newTeam="$(echo ${developerName} | cut -d "(" -f2 | cut -d ")" -f1)"

    mofluidTeam="$(awk -F '=' '/DevelopmentTeam/ {print $2}' ./Mofluid.xcodeproj/project.pbxproj |head -n1)"
    sed -i '' "s/${mofluidTeam}/ \"${newTeam}\";/g" Mofluid.xcodeproj/project.pbxproj

    podTeam="$(awk -F '=' '/DevelopmentTeam/ {print $2}' ./Pods/Pods.xcodeproj/project.pbxproj|head -n1)"
    sed -i '' "s/${podTeam}/ \"${newTeam}\";/g" Pods/Pods.xcodeproj/project.pbxproj
}

replaceDevloperName(){
    mofuluidDevName="$(awk -F '=' '/CODE_SIGN_IDENTITY/ {print $2}' ./Mofluid.xcodeproj/project.pbxproj |head -n1)"
    sed -i '' "s/${mofuluidDevName}/ \"${developerName}\";/g" Mofluid.xcodeproj/project.pbxproj

    podDevName="$(awk -F '=' '/CODE_SIGN_IDENTITY/ {print $2}' ./Pods/Pods.xcodeproj/project.pbxproj|head -n1)"
    sed -i '' "s/${podDevName}/ \"${developerName}\";/g" Pods/Pods.xcodeproj/project.pbxproj
}

replaceBundleId(){
    oldBundleId="$(awk -F '=' '/PRODUCT_BUNDLE_IDENTIFIER/ {print $2}' ./Mofluid.xcodeproj/project.pbxproj |head -n1)"
    sed -i '' "s/${oldBundleId}/ \"${BUNDLE_ID}\";/g" Mofluid.xcodeproj/project.pbxproj
    /usr/libexec/PlistBuddy -c "Set :CFBundleIdentifier ${BUNDLE_ID}" "${PROJDIR}/Mofluid/Info.plist"
}

repleaceProv(){
    oldProv="$(awk -F '=' '/PROVISIONING_PROFILE/ {print $2}' ./Mofluid.xcodeproj/project.pbxproj |head -n1)"
    sed -i '' "s/${oldProv}/ ${UUIDStr};/g" Mofluid.xcodeproj/project.pbxproj

    oldProv="$(awk -F '=' '/PROVISIONING_PROFILE/ {print $2}' ./Pods/Pods.xcodeproj/project.pbxproj |head -n1)"
    sed -i '' "s/${oldProv}/ ${UUIDStr};/g" Pods/Pods.xcodeproj/project.pbxproj
}


replaceConfig(){
    /usr/libexec/PlistBuddy -c "Set :CFBundleShortVersionString ${BUILD_VERSION}" "${PROJDIR}/Mofluid/Info.plist"
    /usr/libexec/PlistBuddy -c "Set :CFBundleVersion ${BUILD_VERSION}" "${PROJDIR}/Mofluid/Info.plist"
    /usr/libexec/PlistBuddy -c "Set :CFBundleDisplayName ${APPLICATION_NAME}" "${PROJDIR}/Mofluid/Info.plist"
    /usr/libexec/PlistBuddy -c "Set :BaseURL ${BASE_URL}" "${PROJDIR}/Mofluid/Model/config.plist"
    /usr/libexec/PlistBuddy -c "Set :AuthId ${AUTH_KEY}" "${PROJDIR}/Mofluid/Model/config.plist"
    /usr/libexec/PlistBuddy -c "Set :AuthSecret ${AUTH_SECRET}" "${PROJDIR}/Mofluid/Model/config.plist"
    /usr/libexec/PlistBuddy -c "Set :DefaultLanguage ${DEFAULT_LANGUAGE}" "${PROJDIR}/Mofluid/Model/config.plist"
}

changeIcons(){
    convert "${ICON_FILE}" -resize 40x40 "${PROJDIR}/Mofluid/Assets.xcassets/AppIcon.appiconset/Icon-40.png"
    convert "${ICON_FILE}" -resize 80x80 "${PROJDIR}/Mofluid/Assets.xcassets/AppIcon.appiconset/Icon-40@2x.png"
    convert "${ICON_FILE}" -resize 120x120 "${PROJDIR}/Mofluid/Assets.xcassets/AppIcon.appiconset/Icon-40@3x.png"
    convert "${ICON_FILE}" -resize 120x120 "${PROJDIR}/Mofluid/Assets.xcassets/AppIcon.appiconset/Icon-60@2x.png"
    convert "${ICON_FILE}" -resize 180x180 "${PROJDIR}/Mofluid/Assets.xcassets/AppIcon.appiconset/Icon-60@3x.png"
    convert "${ICON_FILE}" -resize 76x76 "${PROJDIR}/Mofluid/Assets.xcassets/AppIcon.appiconset/Icon-76.png"
    convert "${ICON_FILE}" -resize 152x152 "${PROJDIR}/Mofluid/Assets.xcassets/AppIcon.appiconset/Icon-76@2x.png"
    convert "${ICON_FILE}" -resize 167x167 "${PROJDIR}/Mofluid/Assets.xcassets/AppIcon.appiconset/Icon-83.5@2x.png"
    convert "${ICON_FILE}" -resize 29x29 "${PROJDIR}/Mofluid/Assets.xcassets/AppIcon.appiconset/Icon-Small.png"
    convert "${ICON_FILE}" -resize 58x58 "${PROJDIR}/Mofluid/Assets.xcassets/AppIcon.appiconset/Icon-Small@2x.png"
    convert "${ICON_FILE}" -resize 87x87 "${PROJDIR}/Mofluid/Assets.xcassets/AppIcon.appiconset/Icon-Small@3x.png"
}

convertSplashScreen(){
    convert "${SPLASH_FILE}" -resize $1 $2
     mogrify -extent $1 -gravity Center -fill white $2
}

changeSplashScreen(){
    convertSplashScreen 1242x2208 "${PROJDIR}/Mofluid/Assets.xcassets/BrandAsset.launchimage/Default-1242@3x~iphone6s-portrait_1242x2208.png"
    convertSplashScreen 640x1136 "${PROJDIR}/Mofluid/Assets.xcassets/BrandAsset.launchimage/Default-568h@2x~iphone_640x1136-1.png"
    convertSplashScreen 640x1136 "${PROJDIR}/Mofluid/Assets.xcassets/BrandAsset.launchimage/Default-568h@2x~iphone_640x1136.png"
    convertSplashScreen 750x1334 "${PROJDIR}/Mofluid/Assets.xcassets/BrandAsset.launchimage/Default-750@2x~iphone6-portrait_750x1334.png"
    convertSplashScreen 2048x1536 "${PROJDIR}/Mofluid/Assets.xcassets/BrandAsset.launchimage/Default-Landscape@2x~ipad_2048x1536.png"
    convertSplashScreen 1024x768 "${PROJDIR}/Mofluid/Assets.xcassets/BrandAsset.launchimage/Default-Landscape~ipad_1024x768.png"
    convertSplashScreen 1536x2048 "${PROJDIR}/Mofluid/Assets.xcassets/BrandAsset.launchimage/Default-Portrait@2x~ipad_1536x2048.png"
    convertSplashScreen 768x1024 "${PROJDIR}/Mofluid/Assets.xcassets/BrandAsset.launchimage/Default-Portrait~ipad_768x1024-1.png"
    convertSplashScreen 768x1024 "${PROJDIR}/Mofluid/Assets.xcassets/BrandAsset.launchimage/Default-Portrait~ipad_768x1024.png"
    convertSplashScreen 640x960 "${PROJDIR}/Mofluid/Assets.xcassets/BrandAsset.launchimage/Default@2x~iphone_640x960-1.png"
    convertSplashScreen 640x960 "${PROJDIR}/Mofluid/Assets.xcassets/BrandAsset.launchimage/Default@2x~iphone_640x960.png"
    convertSplashScreen 2048x2732 "${PROJDIR}/Mofluid/Assets.xcassets/BrandAsset.launchimage/Default~ipad.png"
    convertSplashScreen 1080x1920 "${PROJDIR}/Mofluid/Assets.xcassets/BrandAsset.launchimage/Default~iphone.png"
}

createBuild(){
    security create-keychain -p 123456 "${KEYCHAIN_NAME}"
    security -v list-keychains -d system -s "${KEYCHAIN_NAME}"
    security show-keychain-info "${KEYCHAIN_NAME}"
    security -v unlock-keychain -p 123456 "${KEYCHAIN_NAME}"
    security set-keychain-settings -t "${KEYCHAIN_TIME}" -u "${KEYCHAIN_NAME}"
    security -v unlock-keychain -p 123456 "${KEYCHAIN_NAME}"
    security -v list-keychains -d system -s "${KEYCHAIN_NAME}"
    security show-keychain-info "${KEYCHAIN_NAME}"
    security set-keychain-settings -t "${KEYCHAIN_TIME}" -u "${KEYCHAIN_NAME}"
    security show-keychain-info "${KEYCHAIN_NAME}"
    security list-keychains -s "${KEYCHAIN_NAME}"
    security default-keychain -s "${KEYCHAIN_NAME}"

    security import "${CERTIFICATE}" -k "${KEYCHAIN_NAME}" -P "${PASSPHRASE}" -T /usr/bin/codesign
    security import "${CERTIFICATE}" -k "${KEYCHAIN_NAME}" -P "${PASSPHRASE}" -T /usr/bin/codesign
    security set-key-partition-list -S apple-tool:,apple: -s -k 123456 "${KEYCHAIN_NAME}"

    echo "${BUILD_TYPE}"
    if [ "${BUILD_TYPE}" = "appStore" ]; then
        echo "Creating App store Build"
        xcodebuild -workspace "Mofluid.xcworkspace" -scheme "Mofluid" -sdk iphoneos -configuration AppStoreDistribution  archive -archivePath "${PROJECT_BUILDDIR}/Build/Mofluid.xcarchive" clean build
        xcodebuild -exportArchive -archivePath "${PROJECT_BUILDDIR}/Build/Mofluid.xcarchive" -exportOptionsPlist "${PROJDIR}/exportOptions.plist" -exportPath "${PROJECT_BUILDDIR}/Build"
        cp "${PROJECT_BUILDDIR}/Build/Mofluid.ipa" "${BUILD_HISTORY_DIR}/${APPLICATION_NAME}_${BUILD_ID}.ipa"
    else
        echo "Creating Test Build"
        xcodebuild -workspace "Mofluid.xcworkspace" -scheme "Mofluid" -derivedDataPath "${PROJECT_BUILDDIR}" CODE_SIGN_IDENTITY="${developerName}" OTHER_CODE_SIGN_FLAGS="--keychain ${KEYCHAIN_NAME}" -configuration Release clean build
        /usr/bin/xcrun -sdk iphoneos PackageApplicationFixed -v "${PROJECT_BUILDDIR}/Build/Products/Release-iphoneos/${PROJECT_NAME}.app" -o "${BUILD_HISTORY_DIR}/${APPLICATION_NAME}_${BUILD_ID}.ipa" --sign "${developerName}" --embed "${PROVISONNING_PROFILE}"
    fi
}

deleteCertificate(){
    security delete-certificate -c "${developerName}" "${KEYCHAIN_NAME}"
    security delete-keychain "${KEYCHAIN_NAME}"
}

echo "Renaming Provisionaing Profile"
renameProvisioningProfile

echo "Finding Developmer Name"
findDeveloperName

echo "Found developer Name"
echo "${developerName}"

echo "Changning directory"
changeDir

echo "Cleaning Git Code"
cleanGITCode

echo "Replacing Development Team"
replaceDevelopmentTeam

echo "Replacing Devloper Name"
replaceDevloperName

echo "Replacing Bundle Id"
replaceBundleId

echo "Replacing Prov Profile"
repleaceProv

echo "Replacing Config"
replaceConfig

echo "Changing Icon"
changeIcons

echo "Changing Splash(Launch) screen"
changeSplashScreen

echo "Creating Build"
createBuild

echo "Deleting Certificate"
deleteCertificate