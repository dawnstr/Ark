package com.victor.ark

/**
 * Parses raw speech text into an ArkCommand.
 * Kept as simple pattern matching for v0.1 — swap in something smarter later
 * without touching the services that consume ArkCommand.
 */

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

    // Order matters: more specific phrases first.
    private val exactMatche
