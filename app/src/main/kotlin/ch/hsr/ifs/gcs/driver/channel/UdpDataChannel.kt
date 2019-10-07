package ch.hsr.ifs.gcs.driver.channel

import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.StandardProtocolFamily.INET
import java.nio.ByteBuffer
import java.nio.channels.ByteChannel
import java.nio.channels.DatagramChannel

private fun makeChannel(port: Int) = DatagramChannel.open(INET).apply {
    bind(InetSocketAddress(Inet4Address.getByName("0.0.0.0"), port))
    configureBlocking(true)
    val remote = receive(ByteBuffer.wrap(ByteArray(256)))
    connect(remote)
}

class UdpDataChannel(port: Int) : ByteChannel by makeChannel(port)