package ch.hsr.ifs.gcs.ui.mission.need.parameter.configurator

import ch.hsr.ifs.gcs.GCS
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.support.geo.GPSPosition
import ch.hsr.ifs.gcs.ui.mission.need.parameter.ParameterConfigurator

class PersonConfigurator : ParameterConfigurator<GPSPosition>() {

    override fun present() {
        super.present()
        showInstructionText(GCS.context.getString(R.string.person_instruction))

        //TODO: get position of selected person marker as result
    }

    override fun destroy() {
        hideInstructionText()
        super.destroy()
    }
}
