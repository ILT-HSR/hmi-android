package ch.hsr.ifs.gcs.resources

import android.content.Context
import android.hardware.usb.UsbManager
import android.util.Log
import ch.hsr.ifs.gcs.driver.DRIVER_MAVLINK_PIXHAWK_PX4
import ch.hsr.ifs.gcs.driver.MAVLinkCommonPlatform
import ch.hsr.ifs.gcs.driver.internal.MAVLinkPlatformPixhawkPX4
import ch.hsr.ifs.gcs.resources.Resource.Status
import com.hoho.android.usbserial.driver.UsbSerialProber
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * The resource manager provides an abstract interface to the distributed resource management system
 *
 * It is also a [node][ResourceNode] in the system itself, contributing it local availableResources
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
object ResourceManager : ResourceNode {

    interface OnResourceAvailabilityChangedListener {

        fun onResourceAvailabilityChanged()

    }

    private val fLocalResources = ArrayList<Resource>()

    var listener: OnResourceAvailabilityChangedListener? = null

    private val fScanExecutor = Executors.newSingleThreadScheduledExecutor()

    fun startScanning(context: Context) {
        fScanExecutor.scheduleAtFixedRate({ scan(context) }, 0, 100, TimeUnit.MILLISECONDS)
    }

    override val availableResources
        get() = synchronized(fLocalResources) {
            fLocalResources.filter {
                it.status == Resource.Status.AVAILABLE
            }
        }

    override val allResources get() = synchronized(fLocalResources) { fLocalResources }

    override fun add(resource: Resource) {
        synchronized(fLocalResources) {
            fLocalResources += resource
        }
    }

    operator fun plusAssign(resource: Resource) = synchronized(fLocalResources) {
        add(resource)
    }

    override operator fun get(vararg capabilities: Capability<*>) =
            synchronized(fLocalResources) {
                availableResources.asSequence()
                        .filter { it.status == Status.AVAILABLE }
                        .filter { capabilities.all(it::has) }
                        .firstOrNull();
            }

    override fun reset() {
        synchronized(fLocalResources) {
            assert(fLocalResources.none { it.status == Status.ACQUIRED || it.status == Status.BUSY }, {
                "Tried to reset ResourceManager with active resources"
            })
            fLocalResources.clear()
        }
    }

    override fun acquire(resource: Resource): Boolean {
        TODO("Implement")
    }

    private fun scan(context: Context) {
        val mUsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager).filter {
            it.device.manufacturerName != "Arduino LLC"
        }.forEach { dev ->
            synchronized(fLocalResources) {
                fLocalResources.filter { it.status == Status.UNAVAILABLE }.forEach {
                    when(it.driverId) {
                        DRIVER_MAVLINK_PIXHAWK_PX4 -> {
                            val platform = MAVLinkCommonPlatform.create(::MAVLinkPlatformPixhawkPX4, context, dev.ports[0])
                            if(platform != null) {
                                it.markAs(Status.AVAILABLE)
                                it.plaform = platform
                            }
                        }
                    }
                }
            }
        }
    }
}
