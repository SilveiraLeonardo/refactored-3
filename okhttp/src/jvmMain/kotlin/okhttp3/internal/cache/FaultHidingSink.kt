
init {
  cacheResponse?.let {
    sentRequestMillis = it.sentRequestAtMillis
    receivedResponseMillis = it.receivedResponseAtMillis
    val headers = it.headers
    for (i in 0 until headers.size) {
      val fieldName = headers.name(i)
      val value = headers.value(i)
      when {
        fieldName.equals("Date", ignoreCase = true) -> {
          servedDate = value.toHttpDateOrNull()
          servedDateString = value
        }
        fieldName.equals("Expires", ignoreCase = true) -> {
          expires = value.toHttpDateOrNull()
        }
        fieldName.equals("Last-Modified", ignoreCase = true) -> {
          lastModified = value.toHttpDateOrNull()
          lastModifiedString = value
        }
        fieldName.equals("ETag", ignoreCase = true) -> {
          etag = value
        }
        fieldName.equals("Age", ignoreCase = true) -> {
          ageSeconds = value.toNonNegativeInt(-1)
        }
      }
    }
  }
}
