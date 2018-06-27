package ch.hsr.ifs.gcs.ui

import ch.hsr.ifs.gcs.driver.Input
import ch.hsr.ifs.gcs.driver.access.InputProvider

class BasicHardwareControllable<Controllable : Input.Listener>(private val fProvider: InputProvider) : HardwareControllable<Controllable> {

    private var fControls: Input? = null

    private val fProviderListener = object : InputProvider.Listener {
        lateinit var controllable: HardwareControllable<Controllable>

        override fun onInputDeviceAvailable(device: Input) {
            fProvider.removeListener(this)
            fControls = device
            enableHardwareControls(controllable)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun disableHardwareControls(controllable: HardwareControllable<Controllable>) {
        fControls?.removeListener(controllable as Controllable)
        fProvider.removeListener(fProviderListener)
    }

    @Suppress("UNCHECKED_CAST")
    override fun enableHardwareControls(controllable: HardwareControllable<Controllable>) {
        if(fControls != null) {
            fControls!!.addListener(controllable as Controllable)
        } else {
            fProviderListener.controllable = controllable
            fProvider.addListener(fProviderListener)
        }
    }

}