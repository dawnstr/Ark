package com.victor.ark

sealed class ArkCommand {
    object GoBack : ArkCommand()
    object GoHome : ArkCommand()
    object Recents : ArkCommand()
    object ScrollUp : ArkCommand()
    object ScrollDown : ArkCommand()
    object Screenshot : ArkCommand()
    data class OpenApp(val spokenName: String) : ArkCommand()
    object Unknown : ArkCommand()
}

object CommandParser {

    private val exactMatches: List<Pair<List<String>, ArkCommand>> = listOf(
        listOf("go back", "back") to ArkCommand.GoBack,
        listOf("go home", "home screen", "home") to ArkCommand.GoHome,
        listOf("recents", "recent apps", "show recents") to ArkCommand.Recents,
        listOf("scroll up") to ArkCommand.ScrollUp,
        listOf("scroll down") to ArkCommand.ScrollDown,
        listOf("take a screenshot", "screenshot", "take screenshot") to ArkCommand.Screenshot
    )

    private val openAppPrefixes = listOf("open ", "launch ", "start ")

    fun parse(rawText: String): ArkCommand {
        val text = rawText.trim().lowercase()

        for ((phrases, command) in exactMatches) {
            if (phrases.any { text == it || text.contains(it) }) {
                return command
            }
        }

        for (prefix in openAppPrefixes) {
            if (text.startsWith(prefix)) {
                val appName = text.removePrefix(prefix).trim()
                if (appName.isNotEmpty()) {
                    return ArkCommand.OpenApp(appName)
                }
            }
        }

        return ArkCommand.Unknown
    }
}
