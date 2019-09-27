package ch.hsr.ifs.gcs.driver.mavlink.support

interface MAVLinkExecution {

    fun handleCurrentMissionItem(itemNumber: Int)

    fun handleMissionItemReached(itemNumber: Int)

    fun handleLandedState(state: Int)

}