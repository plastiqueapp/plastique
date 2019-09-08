#!/usr/bin/env bash
set -euo pipefail

app_id=io.plastique.android.dev
test_app_id=io.plastique.android.dev.test
device_screenshot_path="/mnt/sdcard/Pictures/screenshots/plastique/"
screenshot_path="app/src/prod/play/listings/en-US/graphics/phone-screenshots/"

cd "$(dirname "${BASH_SOURCE[0]}")"/..

: "${ADB:=adb}"

ensure_device() {
    ${ADB} wait-for-device
}

enter_demo_mode() {
    echo "Entering Demo Mode"
    # https://android.googlesource.com/platform/frameworks/base/+/master/packages/SystemUI/docs/demo_mode.md
    ${ADB} shell settings put global sysui_demo_allowed 1
    ${ADB} shell am broadcast -a com.android.systemui.demo -e command enter > /dev/null
    ${ADB} shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm 1230 > /dev/null
    ${ADB} shell am broadcast -a com.android.systemui.demo -e command network -e mobile show -e level 4 -e datatype false > /dev/null
    ${ADB} shell am broadcast -a com.android.systemui.demo -e command network -e wifi show -e level 4 > /dev/null
    ${ADB} shell am broadcast -a com.android.systemui.demo -e command notifications -e visible false > /dev/null
    ${ADB} shell am broadcast -a com.android.systemui.demo -e command battery -e plugged false -e level 100 > /dev/null
}

exit_demo_mode() {
    echo "Exiting Demo Mode"
    ${ADB} shell am broadcast -a com.android.systemui.demo -e command exit > /dev/null
}

run_tests() {
    ./gradlew -q :app:installDevDebug :app:installDevDebugAndroidTest
    ${ADB} shell am instrument -w -e package io.plastique.test ${test_app_id}/io.plastique.test.PlastiqueJUnitRunner
}

uninstall() {
    ${ADB} uninstall ${app_id} > /dev/null
    ${ADB} uninstall ${test_app_id} > /dev/null
}

download_screenshots() {
    echo "Downloading screenshots"
    rm -rf "${screenshot_path}"*
    mkdir -p "${screenshot_path}"
    ${ADB} shell "find $device_screenshot_path -type f -print0" | xargs -0 -n 1 -I "{}" ${ADB} pull "{}" "$screenshot_path" > /dev/null
    ${ADB} shell rm -r "$device_screenshot_path"
}

ensure_device
trap exit_demo_mode EXIT

enter_demo_mode
run_tests
uninstall
download_screenshots
