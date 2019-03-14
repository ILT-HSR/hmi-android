package ch.hsr.ifs.gcs.driver.access

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import ch.hsr.ifs.gcs.driver.Platform
import ch.hsr.ifs.gcs.driver.SerialPlatform
import ch.hsr.ifs.gcs.driver.channel.SerialDataChannelFactory
import ch.hsr.ifs.gcs.driver.mavlink.platform.CommonPlatform
import ch.hsr.ifs.gcs.driver.mavlink.platform.CommonPlatform.Companion.DRIVER_MAVLINK_COMMON
import ch.hsr.ifs.gcs.driver.mavlink.platform.PixhawkPX4
import ch.hsr.ifs.gcs.driver.mavlink.platform.PixhawkPX4.Companion.DRIVER_MAVLINK_PIXHAWK_PX4
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialProber
import java.nio.channels.ByteChannel
import java.util.concurrent.Executors

class PlatformManager(private val fListener: Listener) {

    interface Listener {

        fun onNewPlatformAvailable(platform: Platform)

    }

    companion object {
        private const val LOG_TAG = "PlatformManager"
    }

    private val fDeviceScanner = Executors.newSingleThreadExecutor()
    private val fOpenDevices = mutableMapOf<UsbDevice, Platform>()
    private val fDeviceFilter: (UsbSerialDriver) -> Boolean = { it.device.manufacturerName != "Arduino LLC" && !fOpenDevices.contains(it.device) }
    private val fSerialDrivers = mutableMapOf<String, (ByteChannel) -> SerialPlatform?>(
            DRIVER_MAVLINK_PIXHAWK_PX4 to PixhawkPX4.Companion::instantiate,
            DRIVER_MAVLINK_COMMON to ::CommonPlatform
    )

    fun start(context: Context) {
        fDeviceScanner.submit { scan(context) }
    }

    fun stop() {
        fDeviceScanner.shutdownNow()
    }

    fun deviceAttached(context: Context, device: UsbDevice) {
        Log.i(LOG_TAG, "Device attached")
        UsbSerialProber.getDefaultProber().probeDevice(device)?.let{
            if(fDeviceFilter(it)) {
                fDeviceScanner.submit { probe(context, it) }
            }
        }
    }

    fun deviceDetached(device: UsbDevice) {
        Log.i(LOG_TAG, "Device detached")
    }

    private fun probe(context: Context, driver: UsbSerialDriver): Boolean {
        val parameters = SerialDataChannelFactory.Parameters(context, driver.ports[0])
        (SerialDataChannelFactory.createChannel(parameters))?.let { channel ->
            fSerialDrivers.map { e ->
                e.value(channel)?.let {
                    fOpenDevices[driver.device] = it
                    Log.i(LOG_TAG, "New platform created: $it")
                    fListener.onNewPlatformAvailable(it)
                    return true
                }
            }
        }
        return false
    }

    private fun scan(context: Context) {
        Log.i(LOG_TAG, "Scanning for devices")
        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        UsbSerialProber.getDefaultProber().findAllDrivers(manager).filter(fDeviceFilter).forEach {
            probe(context, it)
        }
    }
}