package com.victor.ark

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.DisplayMetrics
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class VoiceControlAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "ArkAccessibility"
        var instance: VoiceControlAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.i(TAG, "Ark accessibility service connected")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
        Log.w(TAG, "Ark accessibility service interrupted")
    }

    fun execute(command: ArkCommand, appResolver: AppResolver) {
        when (command) {
            ArkCommand.GoBack -> performGlobalAction(GLOBAL_ACTION_BACK)
            ArkCommand.GoHome -> performGlobalAction(GLOBAL_ACTION_HOME)
            ArkCommand.Recents -> performGlobalAction(GLOBAL_ACTION_RECENTS)
            ArkCommand.Screenshot -> performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
            ArkCommand.ScrollUp -> dispatchSwipe(down = false)
            ArkCommand.ScrollDown -> dispatchSwipe(down = true)
            is ArkCommand.OpenApp -> {
                val packageName = appResolver.resolve(command.spokenName)
                if (packageName != null) {
                    appResolver.launch(packageName)
                } else {
                    Log.w(TAG, "No app found matching '${command.spokenName}'")
                }
            }
            ArkCommand.Unknown -> {
                Log.d(TAG, "Unrecognized command, ignoring")
            }
        }
    }

    private fun dispatchSwipe(down: Boolean) {
        val metrics: DisplayMetrics = resources.displayMetrics
        val centerX = metrics.widthPixels / 2f
        val startY = if (down) metrics.heightPixels * 0.7f else metrics.heightPixels * 0.3f
        val endY = if (down) metrics.heightPixels * 0.3f else metrics.heightPixels * 0.7f

        val path = Path().apply {
            moveTo(centerX, startY)
            lineTo(centerX, endY)
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 250))
            .build()

        dispatchGesture(gesture, null, null)
    }

    fun dispatchTap(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()
        dispatchGesture(gesture, null, null)
    }
}
