#!/bin/bash
FILE_DIR=$1
FILE_NAME=$2

changeDir(){
 cd ${FILE_DIR}
}

deleteFile(){
  rm ${FILE_NAME}
}

echo "Changing Directory to save Delete file in path : "${FILE_DIR}
changeDir

echo "Deleting File : "${FILE_NAME}
deleteFile
