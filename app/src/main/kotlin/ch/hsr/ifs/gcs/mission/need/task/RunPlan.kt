package ch.hsr.ifs.gcs.mission.need.task

import android.util.Log
import ch.hsr.ifs.gcs.GCS
import ch.hsr.ifs.gcs.resource.Resource
import ch.hsr.ifs.gcs.support.file.readQGCPlan
import ch.hsr.ilt.uxv.hmi.core.driver.Command
import java.io.IOException

class RunPlan(val name: String) : Task {

    override fun executeOn(resource: Resource): List<Command<*>> =
            resource.plaform.run {
                try {
                    val planStream = GCS.context.assets.open("plans/${this@RunPlan.name}.plan")
                    readQGCPlan(planStream)
                } catch (e: IOException) {
                    Log.e("RunPlan", "Failed to load plan file", e)
                    emptyList()
                }

            }

}