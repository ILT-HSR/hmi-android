package ch.hsr.ifs.gcs.driver.channel

import java.nio.channels.ByteChannel

interface ChannelFactory {

    interface Parameters

    fun createChannel(parameters: Parameters): ByteChannel?

}