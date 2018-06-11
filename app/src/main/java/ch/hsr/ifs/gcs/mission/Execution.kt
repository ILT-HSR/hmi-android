package ch.hsr.ifs.gcs.mission

import ch.hsr.ifs.gcs.need.task.Task

abstract class Execution(val tasks: List<Task>) {

    abstract fun tick()

}