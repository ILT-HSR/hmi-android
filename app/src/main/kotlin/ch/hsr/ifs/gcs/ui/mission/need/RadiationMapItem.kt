package ch.hsr.ifs.gcs.ui.mission.need

import ch.hsr.ifs.gcs.mission.need.RadiationMap
import ch.hsr.ifs.gcs.ui.mission.need.parameter.ParameterItemFactory

class RadiationMapItem(need: RadiationMap) : BasicNeedItem(need) {

    override val name = "Radiation Map"

    override val parameters =
            need.parameterList.map(ParameterItemFactory::instantiate)

}