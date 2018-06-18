package ch.hsr.ifs.gcs.mission

import ch.hsr.ifs.gcs.mission.need.task.Task

abstract class Execution(val tasks: List<Task>) {

    enum class Status {
        FAILURE,
        PREPARING,
        RUNNING,
        FINISHED
    }

    abstract fun tick() : Status

}