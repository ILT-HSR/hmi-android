package ch.hsr.ifs.gcs.resource

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Serializable

data class ResourceDescriptor(val id: String, val driver: String, val payloadDrivers: List<String>, val capabilities: List<Capability<*>>) {

    companion object {
        fun load(stream: InputStream): ResourceDescriptor =
                JsonParser().parse(InputStreamReader(stream, Charsets.UTF_8)).asJsonObject.let { res ->
                    val id = res["id"].asString
                    val capabilities = loadCapabilities(res)
                    val platformDescriptor = res["platform"].asJsonObject
                    val payloadDrivers = loadPayloadDrivers(res)
                    ResourceDescriptor(
                            id,
                            platformDescriptor["driver"].asString,
                            payloadDrivers,
                            capabilities
                    )
                }

        private fun loadPayloadDrivers(res: JsonObject) =
                if (res.has("payloads")) {
                    res["payloads"].asJsonArray
                            .asSequence()
                            .map(JsonElement::getAsJsonObject)
                            .mapNotNull { it["driver"].asString }
                            .toList()
                } else {
                    emptyList()
                }

        private fun loadCapabilities(res: JsonObject): List<Capability<out Serializable>> {
            return res["capabilities"].asJsonArray
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
        }
    }

}