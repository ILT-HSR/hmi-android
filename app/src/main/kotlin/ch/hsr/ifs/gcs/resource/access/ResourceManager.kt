package ch.hsr.ifs.gcs.resource.access

import android.content.Context
import android.hardware.usb.UsbManager
import ch.hsr.ifs.gcs.driver.access.PayloadProvider
import ch.hsr.ifs.gcs.driver.access.PlatformProvider
import ch.hsr.ifs.gcs.resource.Capability
import ch.hsr.ifs.gcs.resource.Resource
import ch.hsr.ifs.gcs.resource.Resource.Status
import ch.hsr.ifs.gcs.resource.ResourceNode
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
                        .firstOrNull()
            }

    override fun reset() {
        synchronized(fLocalResources) {
            assert(fLocalResources.none { it.status == Status.ACQUIRED || it.status == Status.BUSY }) {
                "Tried to reset ResourceManager with active resources"
            }
            fLocalResources.clear()
        }
    }

    /**
     * Acquire the given resource, making it unavailable for other missions
     *
     * @return `true` iff. the resource was available, `false` otherwise
     * @since 1.0.0
     */
    override fun acquire(resource: Resource): Boolean =
            synchronized(fLocalResources) {
                if (resource.status != Status.AVAILABLE) {
                    false
                } else {
                    resource.markAs(Status.ACQUIRED)
                    true
                }
            }

    /**
     * Scan the connected telemetry interfaces for available vehicles
     *
     * @since 1.0.0
     */
    private fun scan(context: Context) {
        val mUsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager).filter {
            it.device.manufacturerName != "Arduino LLC"
        }.forEach { dev ->
            synchronized(fLocalResources) {
                fLocalResources.filter { it.status == Status.UNAVAILABLE }.forEach {
                    PlatformProvider.instantiate(it.driverId, context, dev.ports[0], it.driverId)?.apply {
                        it.markAs(Status.AVAILABLE)
                        it.plaform = this
                    }
                }
            }
        }
    }
}
