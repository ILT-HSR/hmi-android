package ch.hsr.ifs.gcs.resource.access

import android.content.Context
import android.content.res.AssetManager
import android.hardware.usb.UsbManager
import android.util.Log
import ch.hsr.ifs.gcs.driver.Platform
import ch.hsr.ifs.gcs.driver.access.PlatformProvider
import ch.hsr.ifs.gcs.driver.channel.SerialDataChannelFactory
import ch.hsr.ifs.gcs.resource.Capability
import ch.hsr.ifs.gcs.resource.Resource
import ch.hsr.ifs.gcs.resource.ResourceNode
import ch.hsr.ifs.gcs.resource.capability.BUILTIN_CAPABILITIES
import ch.hsr.ifs.gcs.resource.internal.SimpleResource
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.hoho.android.usbserial.driver.UsbSerialProber
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ResourceManager(val context: Context) : ResourceNode, Platform.Listener {

    interface Listener {

        fun onResourceAvailabilityChanged()

    }

    companion object {

        private const val RESOURCES_DIRECTORY = "resources"

        private val LOG_TAG = this::class.simpleName

    }

    private val fLocalResources = ArrayList<Resource>()
    private val fListeners = mutableListOf<Listener>()
    private val fScanExecutor = Executors.newSingleThreadScheduledExecutor()

    init {
        context.assets.list(RESOURCES_DIRECTORY).forEach {
            try {
                fLocalResources += load(context.assets.open("$RESOURCES_DIRECTORY/$it", AssetManager.ACCESS_STREAMING))
            } catch (e: IllegalStateException) {
                Log.e(LOG_TAG, "Failed to load '$it'", e)
            }
        }
        fScanExecutor.scheduleAtFixedRate(this::scan, 0, 100, TimeUnit.MILLISECONDS)
    }

    fun addListener(listener: Listener) {
        fListeners += listener
    }

    // ResourceNode implementation

    override val availableResources
        get() = synchronized(fLocalResources) {
            fLocalResources.filter {
                it.status == Resource.Status.AVAILABLE
                        && it.plaform.isAlive
            }
        }

    override val allResources
        get() = synchronized(fLocalResources) { fLocalResources }

    override fun add(resource: Resource) {
        synchronized(fLocalResources) {
            fLocalResources += resource
        }
    }

    override fun get(vararg capabilities: Capability<*>) =
            synchronized(fLocalResources) {
                availableResources.asSequence()
                        .filter(Resource::isAvailable)
                        .filter { capabilities.all(it::has) }
                        .firstOrNull()
            }

    override fun acquire(resource: Resource): Boolean =
            synchronized(fLocalResources) {
                if (resource.status != Resource.Status.AVAILABLE) {
                    false
                } else {
                    resource.markAs(Resource.Status.ACQUIRED)
                    true
                }
            }

    // Platform.Listener implementation

    override fun onLivelinessChanged(platform: Platform) {
        fListeners.forEach(Listener::onResourceAvailabilityChanged)
    }

    // Private implementation

    private fun scan() {
        Log.i(LOG_TAG, "Scanning")
        val mUsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager).filter {
            it.device.manufacturerName != "Arduino LLC"
        }.forEach { dev ->
            synchronized(fLocalResources) {
                fLocalResources.filter { it.status == Resource.Status.UNAVAILABLE }.forEach {
                    val parameters = SerialDataChannelFactory.Parameters(context, dev.ports[0])
                    PlatformProvider.instantiate(it.driverId, SerialDataChannelFactory, parameters, it.payloadDriverId)?.apply {
                        it.markAs(Resource.Status.AVAILABLE)
                        it.plaform = this
                        addListener(this@ResourceManager)
                        fScanExecutor.shutdownNow()
                    }
                }
            }
        }
    }

    private fun load(stream: InputStream) =
            JsonParser().parse(InputStreamReader(stream, Charsets.UTF_8)).asJsonObject.let { res ->
                val id = res["id"].asString
                val capabilities = res["capabilities"].asJsonArray
                        .asSequence()
                        .map(JsonElement::getAsJsonObject)
                        .mapNotNull { obj ->
                            BUILTIN_CAPABILITIES[obj["id"].asString]?.let { cap ->
                                when (cap.type) {
                                    "boolean" -> Capability(cap, obj["value"].asBoolean)
                                    "number" -> Capability(cap, obj["value"].asNumber)
                                    else -> null
                                }
                            }
                        }
                        .toList()
                val platformDescriptor = res["platform"].asJsonObject
                val payloadDriver = if (platformDescriptor.has("payload")) {
                    platformDescriptor["payload"].asJsonObject["driver"].asString
                } else {
                    null
                }

                SimpleResource(
                        id,
                        platformDescriptor["driver"].asString,
                        payloadDriver,
                        capabilities
                ) as Resource
            }

}