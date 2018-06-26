package ch.hsr.ifs.gcs.ui.mission.need

import android.content.Context
import android.util.Log
import ch.hsr.ifs.gcs.mission.need.Need
import ch.hsr.ifs.gcs.ui.MainActivity
import com.google.gson.JsonParser
import java.io.InputStream
import java.io.InputStreamReader

class NeedItemFactory(context: MainActivity) {

    companion object {

        private const val NEED_ITEM_DESCRIPTOR_DIRECTORY = "needs"

        private val LOG_TAG = this::class.simpleName

    }

    data class NeedItemDescriptor(val id: String, val name: String) {
        companion object {
            fun load(stream: InputStream) = with(JsonParser().parse(InputStreamReader(stream, Charsets.UTF_8)).asJsonObject) {
                NeedItemDescriptor(
                        id = this["id"].asString,
                        name = this["name"].asString
                )
            }
        }
    }

    private val fConstructors = mutableMapOf<String, (Need) -> NeedItem>()

    init {
        context.assets.list(NEED_ITEM_DESCRIPTOR_DIRECTORY).forEach {
            try {
                context.assets.open("$NEED_ITEM_DESCRIPTOR_DIRECTORY/$it")?.let {
                    NeedItemDescriptor.load(it)
                }?.apply {
                    fConstructors[id] = { need -> NeedItem(need, name, context) }
                }
            } catch (e: IllegalStateException) {
                Log.e(LOG_TAG, "Failed to read need item descriptor configuration '$NEED_ITEM_DESCRIPTOR_DIRECTORY/$it'")
            }
        }
    }

    fun instantiate(need: Need) = fConstructors[need.id]?.let {
        it.invoke(need)
    } ?: throw IllegalArgumentException("No constructor for ${need.id}")

}