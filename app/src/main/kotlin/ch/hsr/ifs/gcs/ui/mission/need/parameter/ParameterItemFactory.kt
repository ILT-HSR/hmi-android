package ch.hsr.ifs.gcs.ui.mission.need.parameter

import android.util.Log
import ch.hsr.ifs.gcs.mission.need.parameter.Parameter
import ch.hsr.ifs.gcs.ui.MainActivity
import com.google.gson.JsonParser
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.reflect.full.createInstance

class ParameterItemFactory(context: MainActivity) {

    companion object {

        private const val PARAMETER_ITEM_DESCRIPTOR_DIRECTORY = "parameters"

        private val LOG_TAG = this::class.simpleName

    }

    data class ParameterItemDescriptor(val id: String, val name: String, val configurator: ParameterConfigurator<*>) {
        companion object {
            fun load(stream: InputStream) = with(JsonParser().parse(InputStreamReader(stream, Charsets.UTF_8)).asJsonObject) {
                ParameterItemDescriptor(
                        get("id").asString,
                        get("name").asString,
                        Class.forName(get("configurator").asString).kotlin.createInstance() as ParameterConfigurator<*>
                )
            }
        }
    }

    private val fDescriptors = mutableMapOf<String, ParameterItemDescriptor>()

    init {
        context.assets.list(PARAMETER_ITEM_DESCRIPTOR_DIRECTORY)?.forEach { d ->
            try {
                context.assets.open("$PARAMETER_ITEM_DESCRIPTOR_DIRECTORY/$d").let {
                    ParameterItemDescriptor.load(it)
                }.apply {
                    configurator.context = context
                    fDescriptors[id] = this
                }
            } catch (e: IllegalStateException) {
                Log.e(LOG_TAG, "Failed to read parameter item descriptor configuration '$PARAMETER_ITEM_DESCRIPTOR_DIRECTORY/$d'")
            }
        }

    }

    /**
     * Instantiate an item for the [Parameter] with the given id
     */
    fun instantiate(parameter: Parameter<*>) = fDescriptors[parameter.id]?.let {
        ParameterItem(parameter, it.name, it.configurator)
    } ?: throw IllegalArgumentException("Unknown parameter type ${parameter.id}")

}