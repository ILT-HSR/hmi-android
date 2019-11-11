package ch.hsr.ifs.gcs.driver.channel

import java.nio.ByteBuffer
import java.nio.channels.ByteChannel

class ByteChannelAdaptor private constructor(private val fChannel: Channel) : ByteChannel {

    companion object {
        fun adapt(channel: Channel): ByteChannel = ByteChannelAdaptor(channel)
    }

    override fun isOpen() = fChannel.isOpen

    override fun write(src: ByteBuffer): Int {
        val data = ByteArray(src.limit() - src.position())
        src.get(data)
        return fChannel.write(data)
    }

    override fun close() = fChannel.close()

    override fun read(dst: ByteBuffer): Int {
        val data = ByteArray(dst.limit() - dst.position())
        val read = fChannel.read(data)
        dst.put(data, 0, read)
        return read
    }

}