package ch.hsr.ifs.gcs.resource

import ch.hsr.ifs.gcs.support.okhttp.WebsocketCloseCode
import ch.hsr.ifs.gcs.support.okhttp.WebsocketCloseCode.GoingAway
import ch.hsr.ifs.gcs.support.okhttp.WebsocketCloseCode.UnsupportedData
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

fun WebSocket.close(code: WebsocketCloseCode, message: String) =
        close(code.rawCode, message)

@ExperimentalTime
fun OkHttpClient.Builder.readTimeout(timeout: Duration) =
        readTimeout(timeout.toLongMilliseconds(), TimeUnit.MILLISECONDS)


@ExperimentalTime
class ResourceNode @ExperimentalTime constructor(parameters: Parameters) : WebSocketListener() {

    private var fWebsocket: WebSocket = connect(parameters)

    data class Parameters(
            val address: String,
            val port: Int = 80,
            val ioTimeout: Duration = 100.milliseconds
    ) {
        val url get() = "ws://$address:$port"
    }

    fun send(message: String) {
        fWebsocket.send(message)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        webSocket.close(UnsupportedData, "Byte transfer is not supported")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        webSocket.close(GoingAway, "Bye")
    }

    private fun connect(parameters: Parameters): WebSocket {
        val client = OkHttpClient.Builder()
                .readTimeout(parameters.ioTimeout)
                .build()

        val request = Request.Builder()
                .url(parameters.url)
                .build()

        return client.newWebSocket(request, this)
    }
}