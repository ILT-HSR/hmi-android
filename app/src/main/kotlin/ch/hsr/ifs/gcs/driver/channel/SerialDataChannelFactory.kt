package ch.hsr.ifs.gcs.driver.channel

import android.content.Context
import com.hoho.android.usbserial.driver.UsbSerialPort
import java.nio.channels.ByteChannel

object SerialDataChannelFactory : ChannelFactory {

    data class Parameters(
            val context: Context,
            val port: UsbSerialPort,
            val configuration: SerialDataChannel.Configuration = SerialDataChannel.Configuration()
    ) : ChannelFactory.Parameters

    override fun createChannel(parameters: ChannelFactory.Parameters): ByteChannel? = if (parameters !is Parameters) {
        throw IllegalArgumentException("${this::class.simpleName} can only be used with ${Parameters::class.simpleName}")
    } else {
        with(parameters) {
            SerialDataChannel.create(context, port, configuration)
        }
    }

}
