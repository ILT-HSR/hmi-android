package ch.hsr.ifs.gcs.driver.mavlink.payload

import ch.hsr.ifs.gcs.driver.ToggleablePayload
import ch.hsr.ifs.gcs.driver.mavlink.MAVLinkCommand
import ch.hsr.ifs.gcs.driver.mavlink.MAVLinkPayload
import ch.hsr.ifs.gcs.driver.mavlink.PayloadCommand
import ch.hsr.ifs.gcs.driver.mavlink.NativeCommand
import ch.hsr.ifs.gcs.driver.mavlink.support.MAVLinkSystem
import ch.hsr.ifs.gcs.driver.mavlink.support.MessageID
import me.drton.jmavlib.mavlink.MAVLinkMessage
import me.drton.jmavlib.mavlink.MAVLinkSchemaRegistry

class RadiationSensor : MAVLinkPayload, ToggleablePayload {
    private var fIsOn = false

    companion object {
        const val DRIVER_ID = "ch.hsr.ifs.gcs.driver.mavlink.payload.radiationSensor"
    }

    override val schema = MAVLinkSchemaRegistry["arktis_radiation_sensor_bridge"]!!

    override fun handle(message: MAVLinkMessage) {
    }


    override val commandDescriptor: NativeCommand
        get() = PayloadCommand(MessageID.COMMAND_LONG.name,
                mapOf(
                        "command" to 40001,
                        "param1" to if(fIsOn) 1.0f else 0.0f
                )
        )

    override val system = MAVLinkSystem(id = 3, component = 1)

    override fun turnOn(): List<MAVLinkCommand> {
        fIsOn = true
        return listOf(MAVLinkCommand(commandDescriptor))
    }

    override fun turnOff(): List<MAVLinkCommand> {
        fIsOn = false
        return listOf(MAVLinkCommand(commandDescriptor))
    }

    override fun trigger(): List<MAVLinkCommand> = if(fIsOn) turnOff() else turnOn()

}