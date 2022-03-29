package de.lichessbyvoice.vosk

interface ErrorListener {
    /**
     * Called when an error occurs.
     */
    suspend fun onError(exception: Exception?)

    /**
     * Called after timeout expired
     */
    fun onTimeout()
}