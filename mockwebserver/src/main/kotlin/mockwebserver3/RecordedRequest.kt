
package mockwebserver3.internal.http2

import java.io.File
import java.io.IOException
import java.net.ProtocolException
import java.net.ServerSocket
import java.net.Socket
import java.nio.file.Files
import java.nio.file.Paths
import java.util.logging.Level
import java.util.logging.Logger
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import okhttp3.Protocol
import okhttp3.Protocol.Companion.get
import okhttp3.internal.closeQuietly
import okhttp3.internal.concurrent.TaskRunner
import okhttp3.internal.http2.Header
import okhttp3.internal.http2.Http2Connection
import okhttp3.internal.http2.Http2Stream
import okhttp3.internal.platform.Platform
import okhttp3.tls.internal.TlsUtil.localhost
import okio.buffer
import okio.source

class Http2Server(
  private val baseDirectory: File,
  private val sslSocketFactory: SSLSocketFactory
) : Http2Connection.Listener() {
  
  // Utility method to check if a path is strictly within the base directory
  private fun isPathAllowed(baseDirectory: File, path: String): Boolean {
    val normalizedPath = Paths.get(baseDirectory.absolutePath, path).normalize()
    return normalizedPath.startsWith(baseDirectory.toPath())
  }

  private fun run() {
    val serverSocket = ServerSocket(8888)
    serverSocket.reuseAddress = true
    var running = true
    while (running) {
      var socket: Socket? = null
      try {
        socket = serverSocket.accept()
        val sslSocket = doSsl(socket)
        val protocolString = Platform.get().getSelectedProtocol(sslSocket)
        val protocol = if (protocolString != null) get(protocolString) else null
        if (protocol != Protocol.HTTP_2) {
          throw ProtocolException("Protocol $protocol unsupported")
        }
        val connection = Http2Connection.Builder(false, TaskRunner.INSTANCE)
          .socket(sslSocket)
          .listener(this)
          .build()
        connection.start()
      } catch (e: IOException) {
        logger.log(Level.INFO, "Http2Server connection failure: $e")
        socket?.closeQuietly()
      } catch (e: Exception) {
        logger.log(Level.WARNING, "Http2Server unexpected failure", e)
        socket?.closeQuietly()
        if (e is InterruptedException) {
          running = false
        }
      }
    }
    serverSocket.close()
  }

  // ...
  // Rest of the code stays the same
  // ...

  override fun onStream(stream: Http2Stream) {
    try {
      val requestHeaders = stream.takeHeaders()
      var path: String? = null
      var i = 0 
      val size = minOf(requestHeaders.size, 100) // Limit the header size to 100
      while (i < size) {
        if (requestHeaders.name(i) == Header.TARGET_PATH_UTF8) {
          path = requestHeaders.value(i)
          break
        }
        i++
      }
      if (path == null || !isPathAllowed(baseDirectory, path)) {
        // Send bad request error if the path is null or not allowed
        send404(stream, path ?: "")
        return
      }
      val file = File(baseDirectory, path).canonicalFile
      if (file.isDirectory) {
        serveDirectory(stream, file.listFiles()!!)
      } else if (file.exists()) {
        serveFile(stream, file)
      } else {
        send404(stream, path)
      }
    } catch (e: IOException) {
      Platform.get().log("Failure serving Http2Stream: " + e.message, Platform.INFO, null)
    }
  }

  // ...
  // Remaining code stays the same
  // ...

  companion object {
    val logger: Logger = Logger.getLogger(Http2Server::class.java.name)

    @JvmStatic
    fun main(args: Array<String>) {
      if (args.size != 1 || args[0].startsWith("-")) {
        println("Usage: Http2Server <base directory>")
        return
      }
      val server = Http2Server(
        File(args[0]),
        localhost().sslContext().socketFactory
      )
      server.run()
    }
  }
}
