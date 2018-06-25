package ch.hsr.ifs.gcs.ui.mission.need

import ch.hsr.ifs.gcs.mission.need.CallIn
import ch.hsr.ifs.gcs.ui.mission.need.parameter.ParameterItemFactory

class CallInItem(need: CallIn) : BasicNeedItem(need) {

    override val name = "Call-in"

    override val parameters =
            need.parameterList.map {
                ParameterItemFactory.instantiate(it.id, it)
            }

}