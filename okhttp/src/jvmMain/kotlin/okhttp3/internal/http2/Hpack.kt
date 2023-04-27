
import java.io.Closeable
import java.io.InterruptedIOException
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
//... (all other imports stay the same)

@Suppress("NAME_SHADOWING")
class Http2Connection internal constructor(builder: Builder) : Closeable {
  // ...

  /** A lock for managing synchronization issues instead of using 'this' */
  private val connectionLock = ReentrantLock()

  /** A semaphore to control connection rate limiting */
  private val connectionSemaphore = Semaphore(100) // Adjust the number of permits as needed

  // ... ( rest of the variables and methods stay the same )

  @Throws(IOException::class)
  private fun newStream(
    associatedStreamId: Int,
    requestHeaders: List<Header>,
    out: Boolean
  ): Http2Stream {
    // ... ( rest of the code inside this method stays the same )
    connectionSemaphore.acquire() // Acquire permit for connection rate limiting
    connectionLock.lock() // Lock with connectionLock instead of 'this'
    try {
      if (associatedStreamId == 0) {
        writer.headers(outFinished, streamId, requestHeaders)
      }
    // ...
  }
}
