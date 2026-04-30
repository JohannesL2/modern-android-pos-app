#!/bin/bash

API=34
ABI=x86_64
DEVICE=pixel_6
AVD=Pixel6_API34

sdkmanager "platform-tools"
sdkmanager "platforms;android-$API"
sdkmanager "system-images;android-$API;google_apis;$ABI"

echo "no" | avdmanager create avd \
-n $AVD \
-k "system-images;android-$API;google_apis;$ABI" \
-d $DEVICE

./gradlew build
