package ch.hsr.ifs.gcs.mission

import ch.hsr.ifs.gcs.driver.Command

abstract class Execution {

    protected val fCommands = mutableListOf<Command<*>>()

    enum class Status {
        FAILURE,
        PREPARING,
        RUNNING,
        FINISHED
    }

    abstract fun tick() : Status

    open operator fun plusAssign(command: Command<*>) {
        fCommands += command
    }

    fun add(command: Command<*>) {
        this += command
    }
}