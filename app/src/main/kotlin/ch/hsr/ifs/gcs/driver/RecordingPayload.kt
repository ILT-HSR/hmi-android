package ch.hsr.ifs.gcs.driver

interface Recording<out NativeType> {
    val nativeRecording: NativeType
}

interface RecordingPayload : ToggleablePayload {

    val recording: Recording<*>

}