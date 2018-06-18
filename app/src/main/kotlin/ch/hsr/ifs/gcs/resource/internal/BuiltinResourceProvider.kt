package ch.hsr.ifs.gcs.resource.internal

import android.content.ContentProvider
import android.content.ContentValues
import android.content.res.AssetManager
import android.database.Cursor
import android.net.Uri
import android.util.Log
import ch.hsr.ifs.gcs.resource.Capability
import ch.hsr.ifs.gcs.resource.Resource
import ch.hsr.ifs.gcs.resource.access.ResourceManager
import ch.hsr.ifs.gcs.resource.capability.BUILTIN_CAPABILITIES
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import java.io.InputStream
import java.io.InputStreamReader

class BuiltinResourceProvider : ContentProvider() {

    companion object {
        private val LOG_TAG = BuiltinResourceProvider::class.simpleName
    }

    override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null

    override fun onCreate(): Boolean {

        context.assets.list("resources").forEach {
            try {
                val resource = load(context.assets.open("resources/$it", AssetManager.ACCESS_STREAMING))
                ResourceManager += resource
            } catch (e: IllegalStateException) {
                Log.e(LOG_TAG, "Failed to load '$it'", e)
            }
        }

        return true
    }

    override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun getType(uri: Uri?): String? = null

    override fun insert(uri: Uri?, values: ContentValues?): Uri? = null

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

                SimpleResource(id, res["platform"].asJsonObject["driver"].asString, capabilities) as Resource
            }

}