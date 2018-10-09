package ch.hsr.ifs.gcs.driver.channel

import java.nio.ByteBuffer
import java.nio.channels.ByteChannel

class TestChannel : ByteChannel {

    override fun isOpen() = true

    override fun write(src: ByteBuffer?) = src?.remaining() ?: 0

    override fun close() = Unit

    override fun read(dst: ByteBuffer?) = dst?.let {
        it.put(kotlin.byteArrayOf(
                0xc,
                0xa,
                0xf,
                0xe,
                0xb,
                0xa,
                0xb,
                0xe
        ))
        8
    } ?: 0
}
