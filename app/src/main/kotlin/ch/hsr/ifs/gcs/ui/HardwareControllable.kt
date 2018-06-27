package ch.hsr.ifs.gcs.ui

import ch.hsr.ifs.gcs.driver.Input

interface HardwareControllable<Controllable : Input.Listener> {

    fun enableHardwareControls(controllable: HardwareControllable<Controllable>)

    fun disableHardwareControls(controllable: HardwareControllable<Controllable>)

}