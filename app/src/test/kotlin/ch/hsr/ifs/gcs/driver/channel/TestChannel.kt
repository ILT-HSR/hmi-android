package ch.hsr.ifs.gcs.driver.channel

import java.nio.ByteBuffer
import java.nio.channels.ByteChannel

class TestChannel : Channel {

    override val isOpen = true

    override fun write(outgoing: ByteArray) = outgoing.size

    override fun close() = Unit

    override fun read(incoming: ByteArray) = with(incoming) {
        fill(0xc)
        incoming.size
    }
}
