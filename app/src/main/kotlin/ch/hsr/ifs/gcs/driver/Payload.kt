package ch.hsr.ifs.gcs.driver

interface Payload {

    fun trigger(): List<Command<*>>

}