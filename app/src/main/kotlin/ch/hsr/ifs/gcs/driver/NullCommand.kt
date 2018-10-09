package ch.hsr.ifs.gcs.driver

class NullCommand: Command<Unit> {
    override val nativeCommand: Unit
        get() = Unit
}