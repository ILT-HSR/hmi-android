package ch.hsr.ifs.gcs.driver.channel

import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.StandardProtocolFamily.INET
import java.nio.channels.ByteChannel
import java.nio.channels.DatagramChannel

private fun makeChannel(port: Int) = DatagramChannel.open(INET).apply {
    bind(InetSocketAddress(Inet4Address.getByName("0.0.0.0"), port))
    connect(InetSocketAddress(Inet4Address.getByName("10.0.2.2"), 14570))
    configureBlocking(false)
}

class UdpDataChannel(port: Int) : ByteChannel by makeChannel(port)