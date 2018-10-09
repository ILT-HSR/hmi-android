package ch.hsr.ifs.gcs.driver

interface Command<out NativeType> {
    val nativeCommand: NativeType
}