package ch.hsr.ifs.gcs.driver

interface ToggleablePayload : Payload {

    fun turnOn(): List<Command<*>>

    fun turnOff(): List<Command<*>>

}