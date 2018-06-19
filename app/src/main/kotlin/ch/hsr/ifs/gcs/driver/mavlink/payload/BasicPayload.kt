package ch.hsr.ifs.gcs.driver.mavlink.payload

import ch.hsr.ifs.gcs.driver.Platform
import ch.hsr.ifs.gcs.driver.mavlink.MAVLinkPayload
import ch.hsr.ifs.gcs.driver.mavlink.support.MAVLinkExecution
import ch.hsr.ifs.gcs.mission.Execution

abstract class BasicPayload(protected val fPlatform: Platform) : MAVLinkPayload {

    override fun runDuring(execution: Execution) {
        if (execution !is MAVLinkExecution) {
            throw IllegalArgumentException("MAVLink payloads can only run on MAVLink platforms")
        }
        execution.add(commandDescriptor)
    }

}