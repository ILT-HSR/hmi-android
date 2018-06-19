package ch.hsr.ifs.gcs.driver

import ch.hsr.ifs.gcs.mission.Execution

interface Payload {

    fun runDuring(execution: Execution)

}