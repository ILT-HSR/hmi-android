package ch.hsr.ifs.gcs.driver.generic

import ch.hsr.ifs.gcs.driver.Command

class NullCommand: Command<Unit> {
    override val nativeCommand: Unit
        get() = Unit
}