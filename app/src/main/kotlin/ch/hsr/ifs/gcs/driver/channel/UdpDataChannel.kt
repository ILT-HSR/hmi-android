package ch.hsr.ifs.gcs.driver.channel

import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.StandardProtocolFamily.INET
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

class UdpDataChannel(port: Int) : Channel {

    private val fUdpChannel = DatagramChannel.open(INET).apply {
        bind(InetSocketAddress(Inet4Address.getByName("0.0.0.0"), port))
        configureBlocking(true)
        val remote = receive(ByteBuffer.wrap(ByteArray(256)))
        connect(remote)
    }

    override val isOpen get() = fUdpChannel.isOpen

    override fun read(incoming: ByteArray) =
            fUdpChannel.read(ByteBuffer.wrap(incoming))

    override fun write(outgoing: ByteArray) =
            fUdpChannel.write(ByteBuffer.wrap(outgoing))

    override fun close() =
            fUdpChannel.close()

}
