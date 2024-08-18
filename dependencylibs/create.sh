#!/bin/bash

#DECLARE VARIABLES
OPENCV_VERSION_DEFAULT="4.10.0"
OPENCV_VERSION="${OPENCV_VERSION_DEFAULT}"
DIR_NAME=$(dirname "$(realpath "$0")")
TEMP_NAME="temp"
OPENCV_NAME="opencv-${OPENCV_VERSION}"
OPENCV_CONTRIB_NAME="opencv_contrib-${OPENCV_VERSION}"
SDK_OUT="opencv-android-sdk"

# DOWNLOAD OPENCV AND OPENCV CONTRIB FROM GITHUB
download(){
  cd "${DIR_NAME}"
  wget "https://github.com/opencv/opencv/archive/refs/tags/${OPENCV_VERSION}.zip" -O "${OPENCV_NAME}.zip"
  wget "https://github.com/opencv/opencv_contrib/archive/refs/tags/${OPENCV_VERSION}.zip" -O "${OPENCV_CONTRIB_NAME}.zip"
}

compile(){
  cd "${DIR_NAME}"
  rm -rf ${TEMP_NAME}
  mkdir ${TEMP_NAME}

  unzip "${OPENCV_NAME}.zip" -d "${TEMP_NAME}/"
  unzip "${OPENCV_CONTRIB_NAME}.zip" -d "${TEMP_NAME}/"
  mkdir "${TEMP_NAME}/${OPENCV_NAME}/build"
  cd "${TEMP_NAME}/${OPENCV_NAME}/build"
  cmake -GNinja -DCMAKE_INSTALL_PREFIX="${DIR_NAME}/${SDK_OUT}" \
  -DANDROID_PROJECTS_BUILD_TYPE="ANT" -DBUILD_ANDROID_PROJECTS=ON -DBUILD_EXAMPLES=OFF \
  -DBUILD_TESTS=OFF -DBUILD_PERF_TESTS=OFF  -DBUILD_JAVA=ON -DBUILD_opencv_java=ON \
  -DBUILD_SHARED_LIBS=OFF -DBUILD_FAT_JAVA_LIB=ON -DBUILD_PYTHON=OFF -DINSTALL_ANDROID_EXAMPLES=OFF \
  -DANDROID_EXAMPLES_WITH_LIBS=OFF -DBUILD_DOCS=OFF -DWITH_OPENCL=ON -DANDROID_NDK_HOST_X64=ON \
  -DANDROID_TOOLCHAIN=clang -DANDROID_STL=c++_static -DANDROID_ARM_NEON=ON -DANDROID_ABI=arm64-v8a \
  -DANDROID_ABI=x86_64 -DANDROID_ABI=x86 -DANDROID_ABI=arm64-v8a -DANDROID_NDK_HOST_X64=ON \
  -D BUILD_opencv_python3=OFF -D BUILD_opencv_python2=OFF \
  -D OPENCV_EXTRA_MODULES_PATH="${DIR_NAME}/${TEMP_NAME}/${OPENCV_CONTRIB_NAME}/modules" \
  -DOPENCV_ENABLE_NONFREE=ON -DANDROID_NATIVE_API_LEVEL=21 -DANDROID_SDK_TARGET=35  ../
}

build(){
  cd "${TEMP_NAME}/${OPENCV_NAME}/build"
  ninja -j8
  ninja install
  cd "${DIR_NAME}"
  cp "${SDK_OUT}/share/java/opencv4/opencv-${OPENCV_VERSION//.}.jar" "../app/libs/" # //. removes "." from string
}

cleanup(){
  cd "${DIR_NAME}"
  rm -rf "${TEMP_NAME}"
  rm -rf "$SDK_OUT"
  rm "${OPENCV_CONTRIB_NAME}.zip"
  rm "${OPENCV_NAME}.zip"
}

help(){
  echo "USAGE:"
  printf "\t --download  Download opencv sources from git \n\t --compile  Compile downloaded sources with cmake \n\t --build  Build java wrapper from compiled sources \n\t --cleanup remove build files after generation \n\t --cv-version the version of openCV. If unset it defaults to %s " "${OPENCV_VERSION_DEFAULT}"
}

do_download=false
do_compile=false
do_build=false
do_cleanup=false
while [[ $# -gt 0 ]]; do
  case "$1" in
    --download)
      do_download=true
      ;;
    --compile)
      do_compile=true
      ;;
    --build)
      do_build=true
      ;;
    --cleanup)
      do_cleanup=true
      ;;
    --cv-version)
      if [[ -n "$2" ]]; then
              OPENCV_VERSION="$2"
              shift
            else
              echo "Error: --cv-version requires a value."
              exit 1
            fi
      ;;
    *)
      echo "Unknown option: $1"
      help
      exit 1
      ;;
  esac
  shift
done

if $do_download; then
    download
fi

if $do_compile; then
    compile
fi

if $do_build; then
    build
fi

if $do_cleanup; then
    cleanup
fi
