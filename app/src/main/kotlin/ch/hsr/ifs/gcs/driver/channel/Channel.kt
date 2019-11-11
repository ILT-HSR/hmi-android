package ch.hsr.ifs.gcs.driver.channel

interface Channel {

    val isOpen: Boolean

    fun read(incoming: ByteArray): Int

    fun write(outgoing: ByteArray): Int

    fun close()

}