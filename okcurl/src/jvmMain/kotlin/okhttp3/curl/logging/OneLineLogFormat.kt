
package okhttp3.curl

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import java.util.concurrent.TimeUnit.SECONDS
import okhttp3.Call
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.curl.internal.commonCreateRequest
import okhttp3.curl.internal.commonRun
import okhttp3.curl.logging.LoggingUtil
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.LoggingEventListener

actual class Main : CliktCommand(name = NAME, help = "A curl for the next-generation web.") {
  actual val method: String? by option("-X", "--request", help="Specify request command to use")

  actual val data: String? by option("-d", "--data", help="HTTP POST data")

  actual val headers: List<String>? by option("-H", "--header", help="Custom header to pass to server").multiple()

  actual val userAgent: String by option("-A", "--user-agent", help="User-Agent to send to server").default(NAME + "/" + versionString())

  val connectTimeout: Int by option("--connect-timeout", help="Maximum time allowed for connection (seconds)").int().default(DEFAULT_TIMEOUT)

  val readTimeout: Int by option("--read-timeout", help="Maximum time allowed for reading data (seconds)").int().default(DEFAULT_TIMEOUT)

  val callTimeout: Int by option("--call-timeout", help="Maximum time allowed for the entire call (seconds)").int().default(DEFAULT_TIMEOUT)

  val followRedirects: Boolean by option("-L", "--location", help="Follow redirects").flag()

  actual val showHeaders: Boolean by option("-i", "--include", help="Include protocol headers in the output").flag()

  val showHttp2Frames: Boolean by option("--frames", help="Log HTTP/2 frames to STDERR").flag()

  actual val referer: String? by option("-e", "--referer", help="Referer URL")

  val verbose: Boolean by option("-v", "--verbose", help="Makes $NAME verbose during the operation").flag()

  val sslDebug: Boolean by option(help="Output SSL Debug").flag()

  actual val url: String? by argument(name = "url", help="Remote resource URL")

  actual var client: Call.Factory? = null

  actual override fun run() {
    LoggingUtil.configureLogging(debug = verbose, showHttp2Frames = showHttp2Frames, sslDebug = sslDebug)

    commonRun()
  }

  actual fun createRequest(): Request = commonCreateRequest()

  actual fun createClient(): Call.Factory {
    val builder = OkHttpClient.Builder()
    builder.followSslRedirects(followRedirects)
    if (connectTimeout != DEFAULT_TIMEOUT) {
      builder.connectTimeout(connectTimeout.toLong(), SECONDS)
    }
    if (readTimeout != DEFAULT_TIMEOUT) {
      builder.readTimeout(readTimeout.toLong(), SECONDS)
    }
    if (callTimeout != DEFAULT_TIMEOUT) {
      builder.callTimeout(callTimeout.toLong(), SECONDS)
    }
    configureCertificatePinning(builder) // <-- Add this method to configure certificate pinning
    if (verbose) {
      val logger = HttpLoggingInterceptor.Logger(::println)
      builder.eventListenerFactory(LoggingEventListener.Factory(logger))
    }
    return builder.build()
  }

  actual fun close() {
    val okHttpClient = client as OkHttpClient
    okHttpClient.connectionPool.evictAll() // Close any persistent connections.
    okHttpClient.dispatcher.executorService.shutdownNow()
  }

  companion object {
    internal const val NAME = "okcurl"
    internal const val DEFAULT_TIMEOUT = -1

    private fun versionString(): String? {
      val prop = Properties()
      Main::class.java.getResourceAsStream("/okcurl-version.properties")?.use {
        prop.load(it)
      }
      return prop.getProperty("version", "dev")
    }

    private fun configureCertificatePinning(builder: OkHttpClient.Builder) {
      // Add the certificate pins for your target hosts
      val pins = listOf(
          // Example pin for example.com: "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
      )

      val certificatePinner = CertificatePinner.Builder()
      for (pin in pins) {
        certificatePinner.add("example.com", pin)
      }

      builder.certificatePinner(certificatePinner.build())
    }
  }
}
