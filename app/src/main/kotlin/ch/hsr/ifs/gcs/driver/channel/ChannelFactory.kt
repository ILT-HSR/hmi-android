package ch.hsr.ifs.gcs.driver.channel

interface ChannelFactory {

    interface Parameters

    fun createChannel(parameters: Parameters): Channel?

}