package com.victor.ark

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.DisplayMetrics
import android.util.Log
import android.view.accessibility.AccessibilityEvent

/**
 * Does the actual "control the phone" work. MainActivity/FloatingMicService
 * send it an ArkCommand via the companion instance; this class translates
 * that into global actions or dispatched gestures.
 */
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
        // Not currently reacting to screen events, but required override.
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

    /**
     * Dispatches a vertical swipe gesture to scroll the current screen.
     * down=true swipes content upward (scrolls down the page).
     */
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

    /**
     * Dispatches a single tap at the given screen coordinates.
     * Useful later for "tap top left" style commands.
     */
    fun dispatchTap(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()
        dispatchGesture(gesture, null, null)
    }
}
