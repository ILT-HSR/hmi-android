package ch.hsr.ifs.gcs.driver

import ch.hsr.ilt.uxv.hmi.core.driver.Command

class NullCommand: Command<Unit> {
    override val nativeCommand: Unit
        get() = Unit
}