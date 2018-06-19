package ch.hsr.ifs.gcs.driver

interface Payload {

    fun trigger(): Command<*>

}