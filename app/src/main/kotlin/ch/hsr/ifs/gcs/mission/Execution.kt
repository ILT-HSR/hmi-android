package ch.hsr.ifs.gcs.mission

abstract class Execution() {

    enum class Status {
        FAILURE,
        PREPARING,
        RUNNING,
        FINISHED
    }

    abstract fun tick() : Status

}