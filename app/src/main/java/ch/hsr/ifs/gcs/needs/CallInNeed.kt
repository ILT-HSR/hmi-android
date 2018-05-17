package ch.hsr.ifs.gcs.needs

import ch.hsr.ifs.gcs.needs.parameters.CargoNeedParameter
import ch.hsr.ifs.gcs.needs.parameters.TargetNeedParameter
import ch.hsr.ifs.gcs.needs.parameters.NeedParameter
import ch.hsr.ifs.gcs.resources.Resource
import ch.hsr.ifs.gcs.tasks.Task

/**
 * This [Need] implementation represents the need to drop a cargo at a chosen location.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class CallInNeed(override val resource: Resource?) : Need {

    private val targetParameter = TargetNeedParameter()
    private val cargoParameter = CargoNeedParameter()

    override val name = "Call-in"

    override val needParameterList: List<NeedParameter<*>> = arrayListOf(
            targetParameter,
            cargoParameter)

    override var isActive = false

    override fun getTasks(): List<Task>? {
        if(targetParameter.isCompleted && cargoParameter.isCompleted) {
            val targetLocation = targetParameter.result
            val cargo = cargoParameter.result
            TODO("task implementations not ready") //Fill task list
            return ArrayList()
        } else {
            return null
        }
    }

}