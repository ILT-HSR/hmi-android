package ch.hsr.ifs.gcs.resource

import android.content.Context
import android.content.res.AssetManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import ch.hsr.ifs.gcs.driver.PlatformDrivers
import ch.hsr.ifs.gcs.driver.PayloadDrivers
import ch.hsr.ifs.gcs.driver.channel.SerialDataChannelFactory
import ch.hsr.ifs.gcs.driver.channel.UdpDataChannelFactory
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialProber
import java.nio.channels.ByteChannel
import java.util.concurrent.Executors

class ResourceManager(private val fListener: Listener, private val fChannelType: String) {

    interface Listener {
        fun onNewResourceAvailable(resource: Resource)
        fun onResourceAcquired(resource: Resource)
        fun onResourceUnavailable(resource: Resource)
    }

    companion object {
        private const val RESOURCES_DIRECTORY = "resources"
        private const val LOG_TAG = "ResourceManager"

        private val CHANNEL_FACTORIES: Map<String, (Context, ResourceManager) -> ByteChannel?> = mapOf(
                "ch.hsr.ilt.driver.channel.UdpDataChannel" to {_: Context, rm -> rm.createUdpChannel()},
                "ch.hsr.ilt.driver.channel.SerialDataChannel" to {ctx: Context, rm -> rm.createSerialChannel(ctx)}
        )
    }

    private val fDeviceScanner = Executors.newSingleThreadExecutor()
    private val fUsbDeviceFilter: (UsbSerialDriver) -> Boolean = { it.device.manufacturerName != "Arduino LLC" }
    private var fKnownResources = emptyList<ResourceDescriptor>()
    private var fLocalResources = mutableListOf<Resource>()

    fun onCreate(context: Context) {
        fKnownResources = context.assets.list(RESOURCES_DIRECTORY)!!.mapNotNull {
            try {
                ResourceDescriptor.load(context.assets.open("$RESOURCES_DIRECTORY/$it", AssetManager.ACCESS_STREAMING))
            } catch (e: IllegalStateException) {
                Log.e(LOG_TAG, "Failed to load '$it'", e)
                null
            }
        }

        fDeviceScanner.submit { scan(context) }
    }

    fun onDestroy() {
        fDeviceScanner.shutdownNow()
    }

    private fun scan(context: Context) {
        for (res in fKnownResources) {
            val deviceFactory = PlatformDrivers.drivers[res.driver] ?: continue

            val payloads = res.payloadDrivers.map(PayloadDrivers::instantiate).filterNotNull()

            val channelFactory = CHANNEL_FACTORIES[fChannelType] ?: return

            val channel = channelFactory(context, this)

            val platform = channel?.run {
                deviceFactory(this, payloads)
            } ?: continue

            val resource = LocalResource(res.id, platform.driverId, res.payloadDrivers, res.capabilities).also {
                it.plaform = platform
            }

            fLocalResources.add(resource)
            fListener.onNewResourceAvailable(resource)

        }
    }

    private fun createUdpChannel(): ByteChannel? {
        val parameters = UdpDataChannelFactory.Parameters(14550)
        return UdpDataChannelFactory.createChannel(parameters)
    }

    private fun createSerialChannel(context: Context): ByteChannel? {
        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        return UsbSerialProber.getDefaultProber().findAllDrivers(manager).filter(fUsbDeviceFilter).map { usbDriver ->
            val parameters = SerialDataChannelFactory.Parameters(context, usbDriver.ports[0])
            SerialDataChannelFactory.createChannel(parameters)
        }.firstOrNull()
    }

    fun deviceAttached(context: Context, device: UsbDevice) {

    }

    fun deviceDetached(device: UsbDevice) {

    }

}
