package ch.hsr.ifs.gcs.driver.access

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import ch.hsr.ifs.gcs.driver.Platform
import ch.hsr.ifs.gcs.driver.SerialPlatform
import ch.hsr.ifs.gcs.driver.channel.ChannelFactory
import ch.hsr.ifs.gcs.driver.channel.SerialDataChannelFactory
import ch.hsr.ifs.gcs.driver.mavlink.platform.CommonPlatform
import ch.hsr.ifs.gcs.driver.mavlink.platform.CommonPlatform.Companion.DRIVER_MAVLINK_COMMON
import ch.hsr.ifs.gcs.driver.mavlink.platform.PixhawkPX4
import ch.hsr.ifs.gcs.driver.mavlink.platform.PixhawkPX4.Companion.DRIVER_MAVLINK_PIXHAWK_PX4
import com.hoho.android.usbserial.driver.UsbSerialProber
import java.nio.channels.ByteChannel
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class PlatformManager(private val fListener: Listener) {

    interface Listener {

        fun onNewPlatformAvailable(platform: Platform)

    }

    companion object {
        private const val LOG_TAG = "PlatformManager"
    }

    private val fDeviceScanner = Executors.newSingleThreadScheduledExecutor()
    private val fOpenDevices = mutableListOf<UsbDevice>()
    private val fSerialDrivers = mutableMapOf<String, (ByteChannel) -> SerialPlatform?>(
            DRIVER_MAVLINK_PIXHAWK_PX4 to PixhawkPX4.Companion::instantiate,
            DRIVER_MAVLINK_COMMON to ::CommonPlatform
    )

    fun start(context: Context) {
        fDeviceScanner.scheduleAtFixedRate({
            scan(context)
        }, 0, 100, TimeUnit.MILLISECONDS)
    }

    fun stop() {
        fDeviceScanner.shutdownNow()
    }

    /**
     * Create a new driver instance using a channel provided by [factory]
     *
     * This function ensures, that the communication port is initialized correctly, as required
     * by the driver implementation.
     *
     * @param driverId The ID associated with the desired platform driver
     * @param factory A factory to create new channels
     * @param parameters A parameter object used to request a new channel from the [factory]
     * @param payloadDriverId The ID of the driver of the attached [payload][ch.hsr.ifs.gcs.driver.Payload]
     *
     * @return A new instance of platform driver if a vehicle was detected on the
     * provided port, `null` otherwise.
     */
    fun instantiate(driverId: String, factory: ChannelFactory, parameters: ChannelFactory.Parameters) =
            fSerialDrivers[driverId]?.let { f ->
                factory.createChannel(parameters)?.let { c ->
                    f(c)
                }
            } as Platform?

    private fun scan(context: Context) {
        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        UsbSerialProber.getDefaultProber().findAllDrivers(manager).filter { it.device.manufacturerName != "Arduino LLC" && !fOpenDevices.contains(it.device) }.forEach { d ->
            val parameters = SerialDataChannelFactory.Parameters(context, d.ports[0])
            (SerialDataChannelFactory.createChannel(parameters))?.let { channel ->
                fSerialDrivers.map {
                    it.value(channel)?.let {
                        fOpenDevices += d.device
                        Log.i(LOG_TAG, "New platform created: $it")
                        fListener.onNewPlatformAvailable(it)
                        return@forEach
                    }
                }
            }
        }
    }
}