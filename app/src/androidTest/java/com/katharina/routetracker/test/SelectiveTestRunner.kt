package com.katharina.routetracker.test

import android.os.Build
import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner

/**
 * A custom test runner that only executes tests on emulators to protect
 * user data on real devices.
 */
class SelectiveTestRunner : AndroidJUnitRunner() {
    override fun onCreate(arguments: Bundle?) {
        val isEmulator = (Build.FINGERPRINT.contains("generic") ||
                Build.FINGERPRINT.contains("unknown") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("sdk_gphone") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                "google_sdk" == Build.PRODUCT)

        if (!isEmulator) {
            // If on a real device, we stop early. 
            // Note: In a production CI environment, you might want to log this instead.
            android.util.Log.w("SelectiveTestRunner", "Skipping tests on real device: ${Build.MODEL}")
            // We use a dummy argument to filter out all tests
            val newArgs = arguments ?: Bundle()
            newArgs.putString("notPackage", "com.katharina.routetracker")
            super.onCreate(newArgs)
        } else {
            super.onCreate(arguments)
        }
    }
}
