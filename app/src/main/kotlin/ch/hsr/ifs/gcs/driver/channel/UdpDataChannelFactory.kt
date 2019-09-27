package ch.hsr.ifs.gcs.driver.channel

import java.nio.channels.ByteChannel

object UdpDataChannelFactory : ChannelFactory {

    data class Parameters(val port: Int) : ChannelFactory.Parameters

    override fun createChannel(parameters: ChannelFactory.Parameters): ByteChannel? =
            when (parameters as? Parameters) {
                null -> throw IllegalArgumentException("Unknown parameter type '${parameters::class.java}'")
                else -> UdpDataChannel(parameters.port)
            }

}