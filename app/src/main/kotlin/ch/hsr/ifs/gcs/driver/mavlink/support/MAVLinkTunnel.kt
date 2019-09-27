package ch.hsr.ifs.gcs.driver.mavlink.support

import ch.hsr.ifs.gcs.driver.mavlink.MAVLinkPlatform
import me.drton.jmavlib.mavlink.MAVLinkMessage
import me.drton.jmavlib.mavlink.MAVLinkSchema
import java.nio.ByteBuffer

class MAVLinkTunnel(private val fPlatform: MAVLinkPlatform,
                    private val fRemote: MAVLinkSystem,
                    private val fLocal: MAVLinkSystem,
                    private val fSchema: MAVLinkSchema) {

    private var sequenceNumber: Byte = 0

    fun encode(message: MAVLinkMessage): MAVLinkMessage {
        val payload = message.encode(sequenceNumber++).array()
        return createTargetedMAVLinkMessage(MessageID.TUNNEL, fLocal, fRemote, fPlatform.schema).apply {
            set("payload_type", 0)
            set("payload_length", payload.size)
            set("payload", payload)
        }
    }

    fun decode(message: MAVLinkMessage) : MAVLinkMessage {
        assert(message.msgName == MessageID.TUNNEL.name)

        val payload = message.get("payload") as ByteArray
        return MAVLinkMessage(fSchema, ByteBuffer.wrap(payload))
    }

}
