package ch.hsr.ifs.gcs.support

import ch.hsr.ifs.gcs.mission.Mission
import ch.hsr.ifs.gcs.mission.Need
import ch.hsr.ifs.gcs.mission.Result

sealed class Action

data class MissionAvailable(val mission: Mission) : Action()

data class NeedAvailable(val need: Need) : Action()

data class NeedUnavailable(val need: Need) : Action()

data class ResultAvailable(val result: Result) : Action()