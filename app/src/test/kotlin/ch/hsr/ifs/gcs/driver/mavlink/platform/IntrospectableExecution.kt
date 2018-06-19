package ch.hsr.ifs.gcs.driver.mavlink.platform

import ch.hsr.ifs.gcs.driver.Command

interface IntrospectableExecution {

    val size: Int

    val commands: List<Command<*>>

}