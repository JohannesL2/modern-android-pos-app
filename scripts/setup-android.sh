#!/usr/bin/env bash

set -e

ANDROID_API=36
BUILD_TOOLS=36.1.0
NDK_VERSION=27.0.12077973

echo "Installing Android SDK components..."

sdkmanager \
"platform-tools" \
"platforms;android-$ANDROID_API" \
"build-tools;$BUILD_TOOLS" \
"ndk;$NDK_VERSION"

echo "Accepting licenses..."
yes | sdkmanager --licenses

echo "Running first Gradle build..."

./gradlew build

echo "Setup complete."
