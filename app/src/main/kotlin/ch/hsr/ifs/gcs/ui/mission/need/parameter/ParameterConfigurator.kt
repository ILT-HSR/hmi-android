package ch.hsr.ifs.gcs.ui.mission.need.parameter

import android.support.v4.app.Fragment
import ch.hsr.ifs.gcs.ui.MainActivity

abstract class ParameterConfigurator<ResultType> : Fragment() {

    lateinit var context: MainActivity

    lateinit var parameter: ParameterItem<ResultType>

    open fun present() = Unit

    open fun destroy() = Unit

}