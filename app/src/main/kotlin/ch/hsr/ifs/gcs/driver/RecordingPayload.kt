package ch.hsr.ifs.gcs.driver

import ch.hsr.ifs.gcs.support.geo.GPSPosition

interface Recording<out NativeType> {
    val nativeRecording: NativeType
}

interface MapRecording<out DataType> : Recording<List<Pair<GPSPosition, DataType>>>

interface RecordingPayload : ToggleablePayload {

    val recording: Recording<*>

}