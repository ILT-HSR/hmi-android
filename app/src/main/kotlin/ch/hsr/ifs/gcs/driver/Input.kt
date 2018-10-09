package ch.hsr.ifs.gcs.driver

interface Input {

    interface Listener {

        fun onButton(control: Control)

        fun onJoystick(control: Control, value: Byte) {}

    }

    enum class Control(val value: Byte) {
        DPAD_LEFT(0x1),
        DPAD_RIGHT(0x2),
        DPAD_UP(0x3),
        DPAD_DOWN(0x4),
        NEED_START(0xA),
        UPDATE_ABORT(0xB),
        SHOW_ALL(0xD),
        SHOW_MENU(0xE),
        ZOOM_IN(0x10),
        ZOOM_OUT(0x11),
        JOYSTICK_X_AXIS(0x81.toByte()),
        JOYSTICK_Y_AXIS(0x82.toByte())
    }

    fun addListener(listener: Listener)

    fun removeListener(listener: Listener)

}