package ch.hsr.ifs.gcs.resource

import android.content.Context
import android.content.res.AssetManager
import android.hardware.usb.UsbManager
import android.util.Log
import ch.hsr.ifs.gcs.driver.access.PlatformProvider
import ch.hsr.ifs.gcs.driver.channel.SerialDataChannelFactory
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.hoho.android.usbserial.driver.UsbSerialProber
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ResourceManager(val context: Context, val listener: Listener) {

    interface Listener {
        fun onNewResourceAvailable(resource: Resource)
        fun onResourceAcquired(resource: Resource)
        fun onResourceUnavailable(resource: Resource)
    }

    data class ResourceDescriptor(val id: String, val driver: String, val payload: String?, val capabilities: List<Capability<*>>) {
        companion object {
            fun load(stream: InputStream): ResourceDescriptor =
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

                        ResourceDescriptor(
                                id,
                                platformDescriptor["driver"].asString,
                                payloadDriver,
                                capabilities
                        )
                    }
        }
    }

    companion object {
        private const val RESOURCES_DIRECTORY = "resources"
        private const val LOG_TAG = "ResourceManager"
    }

    private val fKnownResources: List<ResourceDescriptor>
    private var fLocalResources = emptyList<Resource>()
    private val fScanExecutor = Executors.newSingleThreadScheduledExecutor()

    init {
        fKnownResources = context.assets.list(RESOURCES_DIRECTORY).mapNotNull {
            try {
                ResourceDescriptor.load(context.assets.open("$RESOURCES_DIRECTORY/$it", AssetManager.ACCESS_STREAMING))
            } catch (e: IllegalStateException) {
                Log.e(LOG_TAG, "Failed to load '$it'", e)
                null
            }
        }
        fScanExecutor.scheduleAtFixedRate(this::scan, 0, 100, TimeUnit.MILLISECONDS)
    }

    // Private implementation

    private fun scan() {
        val mUsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager).filter { it.device.manufacturerName != "Arduino LLC" }
                .forEach { d ->
                    fKnownResources.filter { k -> fLocalResources.none { l -> l.id == k.id } }.forEach {
                        val parameters = SerialDataChannelFactory.Parameters(context, d.ports[0])
                        PlatformProvider.instantiate(it.driver, SerialDataChannelFactory, parameters, it.payload)?.let { p ->
                            val resource = LocalResource(it.id, it.driver, it.payload, it.capabilities).apply {
                                markAs(Resource.Status.AVAILABLE)
                                plaform = p
                            }
                            fLocalResources += resource
                            listener.onNewResourceAvailable(resource)
                        }
                    }
                }
    }
}
