package ch.hsr.ifs.gcs.resource

import android.content.Context
import android.hardware.usb.UsbManager
import ch.hsr.ifs.gcs.driver.PayloadDrivers
import ch.hsr.ifs.gcs.driver.PlatformDrivers
import ch.hsr.ifs.gcs.driver.channel.Channel
import ch.hsr.ifs.gcs.driver.channel.SerialDataChannelFactory
import ch.hsr.ifs.gcs.driver.channel.UdpDataChannelFactory
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialProber
import java.util.concurrent.Executors
import kotlin.time.ExperimentalTime

class ResourceManager @ExperimentalTime constructor(private val fParameters: Parameters) {

    interface Listener {
        fun onNewResourceAvailable(resource: Resource)
        fun onResourceAcquired(resource: Resource)
        fun onResourceUnavailable(resource: Resource)
    }

    data class Parameters @ExperimentalTime constructor(
            val resourceFiles: List<String>,
            val channelType: String,
            val listener: Listener,
            val resourceNodeParameters: ResourceNode.Parameters,
            val context: Context
    )

    companion object {
        @ExperimentalTime
        private val CHANNEL_FACTORIES: Map<String, (ResourceManager) -> Channel?> = mapOf(
                "ch.hsr.ilt.driver.channel.UdpDataChannel" to ResourceManager::createUdpChannel,
                "ch.hsr.ilt.driver.channel.SerialDataChannel" to ResourceManager::createSerialChannel
        )
    }

    private val fDeviceScanner = Executors.newSingleThreadExecutor()
    private val fUsbDeviceFilter: (UsbSerialDriver) -> Boolean = { it.device.manufacturerName != "Arduino LLC" }
    private var fKnownResources = fParameters.resourceFiles.map(ResourceDescriptor.Companion::load)
    private var fLocalResources = mutableListOf<Resource>()

    @ExperimentalTime
    private val fResourceNode = ResourceNode(fParameters.resourceNodeParameters)

    @ExperimentalTime
    fun start() {
        fResourceNode.send("RegisterNode")
        fDeviceScanner.submit { scan() }
    }

    fun stop() {
        fDeviceScanner.shutdownNow()
    }

    @ExperimentalTime
    private fun scan() {
        for (res in fKnownResources) {
            val deviceFactory = PlatformDrivers.drivers[res.driver] ?: continue

            val payloads = res.payloadDrivers.map(PayloadDrivers::instantiate).filterNotNull()

            val channelFactory = CHANNEL_FACTORIES[fParameters.channelType] ?: return

            val channel = channelFactory(this)

            val platform = channel?.run {
                deviceFactory(this, payloads)
            } ?: continue

            val resource = LocalResource(res.id, platform.driverId, res.payloadDrivers, res.capabilities).also {
                it.plaform = platform
            }

            fLocalResources.add(resource)
            fParameters.listener.onNewResourceAvailable(resource)
        }
    }

    private fun createUdpChannel(): Channel? {
        val parameters = UdpDataChannelFactory.Parameters(14550)
        return UdpDataChannelFactory.createChannel(parameters)
    }

    @ExperimentalTime
    private fun createSerialChannel(): Channel? {
        val context = fParameters.context
        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        return UsbSerialProber.getDefaultProber().findAllDrivers(manager).filter(fUsbDeviceFilter).map { usbDriver ->
            val parameters = SerialDataChannelFactory.Parameters(context, usbDriver.ports[0])
            SerialDataChannelFactory.createChannel(parameters)
        }.firstOrNull()
    }

}
