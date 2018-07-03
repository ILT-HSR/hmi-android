package ch.hsr.ifs.gcs.support.file

import android.util.Log
import ch.hsr.ifs.gcs.driver.Command
import ch.hsr.ifs.gcs.driver.mavlink.MAVLinkCommand
import ch.hsr.ifs.gcs.driver.mavlink.support.LongCommand
import ch.hsr.ifs.gcs.driver.mavlink.support.MAVLinkMissionCommand
import ch.hsr.ifs.gcs.driver.mavlink.support.NavigationFrame
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import java.io.InputStream
import java.io.InputStreamReader

fun readQGCPlan(stream: InputStream): List<Command<*>> = try {
    val parse = JsonParser().parse(InputStreamReader(stream))
    val mission = parse.asJsonObject["mission"]
    processMission(mission.asJsonObject).map(::MAVLinkCommand)
} catch (e: JsonParseException) {
    Log.i("Plan::readQGCPlan", "Failed to read plan", e)
    emptyList()
} catch (e: IllegalArgumentException) {
    Log.i("Plan::readQGCPlan", "Failed to read plan", e)
    emptyList()
}

private fun processMission(mission: JsonObject) =
        mission["items"].asJsonArray.map { processItem(it.asJsonObject) }

private fun processItem(item: JsonObject) = item["params"].asJsonArray.let{ params ->
    MAVLinkMissionCommand(
            LongCommand.values().find { it.value == item["command"].asInt }
                    ?: throw IllegalArgumentException("Unknown command ${item["command"].asInt}"),
            NavigationFrame.values().find { it.ordinal == item["frame"].asInt }
                    ?: throw IllegalArgumentException("Unknown fram ${item["frame"].asInt}"),
            processFloat(params[0]),
            processFloat(params[1]),
            processFloat(params[2]),
            processFloat(params[3]),
            processFloat(params[4]),
            processFloat(params[5]),
            processFloat(params[6])
            )
}

private fun processFloat(candidate: JsonElement) =
        if(candidate.isJsonNull) Float.NaN else candidate.asFloat