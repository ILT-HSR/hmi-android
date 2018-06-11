package ch.hsr.ifs.gcs.driver

interface Input {

    interface Listener {

        fun onButton(button: Button)

    }

    enum class Button(val value: Byte) {
        DPAD_LEFT(0x1),
        DPAD_RIGHT(0x2),
        DPAD_UP(0x3),
        DPAD_DOWN(0x4),
        NEED_START(0xA),
        UPDATE_ABORT(0xB),
        SHOW_ALL(0xD),
        SHOW_MENU(0xE),
        ZOOM_IN(0x10),
        ZOOM_OUT(0x11)
    }

    fun addListener(listener: Listener)

    fun removeListener(listener: Listener)

}