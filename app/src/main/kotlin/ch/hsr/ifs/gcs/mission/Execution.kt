package ch.hsr.ifs.gcs.mission

import ch.hsr.ifs.gcs.driver.Command

abstract class Execution {

    enum class Status {
        FAILURE,
        PREPARING,
        RUNNING,
        FINISHED
    }

    protected val fCommands = mutableListOf<Command<*>>()

    abstract suspend fun tick() : Status

    open operator fun plusAssign(command: Command<*>) {
        fCommands += command
    }

    fun add(command: Command<*>) {
        this += command
    }
}