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

disable_animations() {
    echo "Disabling animations"

    window_animation_scale=$(${ADB} shell settings get global window_animation_scale)
    transition_animation_scale=$(${ADB} shell settings get global transition_animation_scale)
    animator_duration_scale=$(${ADB} shell settings get global animator_duration_scale)
    if [[ "$window_animation_scale" = "null" ]]; then
        window_animation_scale=1.0
    fi
    if [[ "$transition_animation_scale" = "null" ]]; then
        transition_animation_scale=1.0
    fi
    if [[ "$animator_duration_scale" = "null" ]]; then
        animator_duration_scale=1.0
    fi

    ${ADB} shell settings put global window_animation_scale 0.0
    ${ADB} shell settings put global transition_animation_scale 0.0
    ${ADB} shell settings put global animator_duration_scale 0.0
}

restore_animations() {
    echo "Restoring animations"

    if [[ ! -z ${window_animation_scale+x} ]]; then
        ${ADB} shell settings put global window_animation_scale ${window_animation_scale}
    fi

    if [[ ! -z ${transition_animation_scale+x} ]]; then
        ${ADB} shell settings put global transition_animation_scale ${transition_animation_scale}
    fi

    if [[ ! -z ${animator_duration_scale+x} ]]; then
        ${ADB} shell settings put global animator_duration_scale ${animator_duration_scale}
    fi
}

run_tests() {
    ./gradlew -q --console=plain :app:installDevDebug :app:installDevDebugAndroidTest
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

cleanup() {
    exit_demo_mode
    restore_animations
}

ensure_device
trap cleanup EXIT

enter_demo_mode
disable_animations
run_tests
uninstall
download_screenshots
