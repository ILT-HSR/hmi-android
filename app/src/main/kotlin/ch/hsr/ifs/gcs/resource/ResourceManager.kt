package ch.hsr.ifs.gcs.resource

import androidx.lifecycle.Observer
import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import ch.hsr.ifs.gcs.driver.Platform
import ch.hsr.ifs.gcs.driver.PlatformModel
import ch.hsr.ifs.gcs.driver.access.PayloadProvider
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import java.io.InputStream
import java.io.InputStreamReader

class ResourceManager(private val fListener: Listener) {

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

    private var fKnownResources = emptyList<ResourceDescriptor>()
    private var fLocalResources = emptyList<Resource>()

    private val fPlatformObserver = Observer<List<Platform>> { lp ->
        lp?.apply {
            val platformCandidates = filter { fLocalResources.none { r -> r.plaform == it } }
            platformCandidates.forEach { p ->
                fKnownResources.find { it.driver == p.driverId }?.let { d ->
                    if(d.payload != null){
                        val payload = PayloadProvider.instantiate(d.payload) ?: return@forEach
                        p.payload = payload
                    }
                    val resource = LocalResource(d.id, d.driver, d.payload, d.capabilities).apply {
                        plaform = p
                    }
                    fListener.onNewResourceAvailable(resource)
                    return@forEach
                }
            }
        }
    }

    fun onCreate(context: Context, platformModel: PlatformModel) {
        fKnownResources = context.assets.list(RESOURCES_DIRECTORY)!!.mapNotNull {
            try {
                ResourceDescriptor.load(context.assets.open("$RESOURCES_DIRECTORY/$it", AssetManager.ACCESS_STREAMING))
            } catch (e: IllegalStateException) {
                Log.e(LOG_TAG, "Failed to load '$it'", e)
                null
            }
        }

        platformModel.availablePlatforms.observeForever(fPlatformObserver)
    }

    fun onDestroy(platformModel: PlatformModel) {
        platformModel.availablePlatforms.removeObserver(fPlatformObserver)
    }

}
