package ch.hsr.ifs.gcs.driver.mavlink.payload

import android.util.Log
import ch.hsr.ifs.gcs.driver.PlatformContext
import ch.hsr.ifs.gcs.driver.Recording
import ch.hsr.ifs.gcs.driver.RecordingPayload
import ch.hsr.ifs.gcs.driver.mavlink.*
import ch.hsr.ifs.gcs.driver.mavlink.support.MAVLinkSystem
import ch.hsr.ifs.gcs.driver.mavlink.support.MessageID
import ch.hsr.ifs.gcs.support.geo.GPSPosition
import kotlinx.coroutines.runBlocking
import me.drton.jmavlib.mavlink.MAVLinkMessage
import me.drton.jmavlib.mavlink.MAVLinkSchemaRegistry


class RadiationSensor : MAVLinkPayload, RecordingPayload {

    data class Measurement(val timestamp: Float, val value: Int)

    class RadiationMap : Recording<List<Pair<GPSPosition, Measurement>>> {

        private val fData = mutableListOf<Pair<GPSPosition, Measurement>>()

        override val nativeRecording: List<Pair<GPSPosition, Measurement>>
            get() = fData

        internal fun put(position: GPSPosition, measurement: Measurement) = put(Pair(position, measurement))

        internal fun put(dataPoint: Pair<GPSPosition, Measurement>) = fData.add(dataPoint)
    }

    private val fMap = RadiationMap()
    private var fIsOn = false

    override val recording: Recording<*>
        get() = runBlocking(PlatformContext) { fMap }

    companion object {
        const val DRIVER_ID = "ch.hsr.ifs.gcs.driver.mavlink.payload.radiationSensor"
    }

    override val schema = MAVLinkSchemaRegistry["arktis_radiation_sensor_bridge"]!!

    override fun handle(message: MAVLinkMessage, platform: MAVLinkPlatform) {
        if (message.msgName != "RADIATION_DATA") {
            return
        }
        val dataPoint = platform.currentPosition?.run {
            val timestamp = message.getFloat("timestamp")
            val counts = message.getInt("sensor_value")
            Pair(this, Measurement(timestamp, counts))
        } ?: return
        Log.i("RadiationSensor", "New radiation data: $dataPoint")
        fMap.put(dataPoint)
    }

    override val commandDescriptor: NativeCommand
        get() = PayloadCommand(MessageID.COMMAND_LONG.name,
                mapOf(
                        "command" to 40001,
                        "param1" to if (fIsOn) 1.0f else 0.0f
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

    override fun trigger(): List<MAVLinkCommand> = if (fIsOn) turnOff() else turnOn()

}