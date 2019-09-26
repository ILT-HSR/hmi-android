package ch.hsr.ifs.gcs.driver

import kotlinx.coroutines.newSingleThreadContext

val PayloadContext = newSingleThreadContext("PlatformContext")

interface Payload {

    fun trigger(): List<Command<*>>

}