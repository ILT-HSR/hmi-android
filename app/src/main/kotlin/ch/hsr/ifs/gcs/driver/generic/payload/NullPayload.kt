package ch.hsr.ifs.gcs.driver.generic.payload

import ch.hsr.ifs.gcs.driver.Command
import ch.hsr.ifs.gcs.driver.Payload
import ch.hsr.ifs.gcs.driver.generic.NullCommand

class NullPayload : Payload {

    companion object {
        const val DRIVER_ID = "ch.hsr.ifs.gcs.driver.generic.payload.null"
    }

    override fun trigger(): List<Command<*>> = listOf(NullCommand())

}