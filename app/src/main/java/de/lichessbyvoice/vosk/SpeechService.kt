package de.lichessbyvoice.vosk

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder.AudioSource
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.Channel
import org.vosk.Recognizer
import java.io.IOException
import kotlin.math.roundToInt

// The original code from org.vosk (see https://github.com/alphacep/vosk-api/blob/master/android/)
// was transcribed to Kotlin and modified for coroutine channels (c) 2022 by Ralf Stephan.
// Copyright 2019 Alpha Cephei Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


/**
 * Service that records audio in a thread, passes it to a recognizer and emits
 * recognition results. Recognition events are passed to a client using
 * [Channel]
 */
class SpeechService(context: Context, private val recognizer: Recognizer, sampleRate: Float) {
    private val sampleRate: Int = sampleRate.toInt()
    private val bufferSize: Int = (this.sampleRate * BUFFER_SIZE_SECONDS).roundToInt()
    private lateinit var recorder : AudioRecord
    private var recognizerThread: RecognizerThread? = null

    init {
        if (ContextCompat.checkSelfPermission( context, android.Manifest.permission.RECORD_AUDIO ) == PackageManager.PERMISSION_GRANTED ) {
            recorder = AudioRecord(
                AudioSource.VOICE_RECOGNITION, this.sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize * 2)
        }
        // else: we already took care of permissions in SpeechRecognitionService.start()
    }

    /**
     * Starts recognition. Does nothing if recognition is active.
     *
     * @return Channel if recognition was actually started
     */
    suspend fun startListening(listener: ErrorListener): Channel<String?>? {
        if (null != recognizerThread) return null
        val textChannel = Channel<String?>()
        recognizerThread = RecognizerThread(textChannel, listener)
        recognizerThread!!.run()
        return textChannel
    }

    /**
     * Starts recognition. After specified timeout listening stops and the
     * endOfSpeech signals about that. Does nothing if recognition is active.
     *
     *
     * timeout - timeout in milliseconds to listen.
     *
     * @return true if recognition was actually started
     */
/*
    fun startListening(listener: ErrorListener, timeout: Int): Boolean {
        if (null != recognizerThread) return false
        recognizerThread = RecognizerThread(listener, timeout)
        recognizerThread!!.start()
        return true
    }
*/

/*
    private fun stopRecognizerThread(): Boolean {
        if (null == recognizerThread) return false
        try {
            recognizerThread!!.interrupt()
            recognizerThread!!.join()
        } catch (e: InterruptedException) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt()
        }
        recognizerThread = null
        return true
    }
*/

    /**
     * Stops recognition. Listener should receive final result if there is
     * any. Does nothing if recognition is not active.
     *
     * @return true if recognition was actually stopped
     */
/*
    fun stop(): Boolean {
        return stopRecognizerThread()
    }
*/

    /**
     * Cancel recognition. Do not post any new events, simply cancel processing.
     * Does nothing if recognition is not active.
     *
     * @return true if recognition was actually stopped
     */
/*
    fun cancel(): Boolean {
        if (recognizerThread != null) {
            recognizerThread!!.setPause(true)
        }
        return stopRecognizerThread()
    }
*/

    /**
     * Shutdown the recognizer and release the recorder
     */
    fun shutdown() {
        recorder.release()
    }

    fun setPause(paused: Boolean) {
        if (recognizerThread != null) {
            recognizerThread!!.setPause(paused)
        }
    }

    /**
     * Resets recognizer in a thread, starts recognition over again
     */
    fun reset() {
        if (recognizerThread != null) {
            recognizerThread!!.reset()
        }
    }

    private inner class RecognizerThread(
        channel: Channel<String?>,
        listener: ErrorListener,
        timeout: Int = NO_TIMEOUT) {
        private val textChannel = channel
        private var remainingSamples: Int
        private val timeoutSamples: Int = if (timeout != Companion.NO_TIMEOUT)
            timeout * sampleRate / 1000 else Companion.NO_TIMEOUT
        private val listener : ErrorListener = listener

        @Volatile
        private var paused = false

        @Volatile
        private var reset = false

        /**
         * When we are paused, don't process audio by the recognizer and don't emit
         * any listener results
         *
         * @param paused the status of pause
         */
        fun setPause(paused: Boolean) {
            this.paused = paused
        }

        /**
         * Set reset state to signal reset of the recognizer and start over
         */
        fun reset() {
            reset = true
        }

        suspend fun run() {
            recorder.startRecording()
            if (recorder.recordingState == AudioRecord.RECORDSTATE_STOPPED) {
                recorder.stop()
                val ioe = IOException(
                    "Failed to start recording. Microphone might be already in use."
                )
                listener.onError(ioe)
                textChannel.close()
            }
            val buffer = ShortArray(bufferSize)
            while (timeoutSamples == Companion.NO_TIMEOUT || remainingSamples > 0)
            {
                val nread = recorder.read(buffer, 0, buffer.size)
                if (paused) {
                    continue
                }
                if (reset) {
                    recognizer.reset()
                    reset = false
                }
                if (nread < 0) throw RuntimeException("error reading audio buffer")
                if (recognizer.acceptWaveForm(buffer, nread)) {
                    val result = recognizer.result
                    textChannel.send(result)
                } else {
                    val partialResult = recognizer.partialResult
                    textChannel.send(partialResult)
                }
                if (timeoutSamples != Companion.NO_TIMEOUT) {
                    remainingSamples -= nread
                }
            }
            recorder.stop()
            if (!paused) {
                // If we met timeout signal that speech ended
                if (timeoutSamples != Companion.NO_TIMEOUT && remainingSamples <= 0) {
                    listener.onTimeout()
                } else {
                    val finalResult = recognizer.finalResult
                    textChannel.send(finalResult)
                }
            }
        }

        init {
            remainingSamples = timeoutSamples
        }
    }

    companion object {
        private const val BUFFER_SIZE_SECONDS = 0.2f
        private const val NO_TIMEOUT = -1
    }

    /**
     * Creates speech service. Service holds the AudioRecord object, so you
     * need to call [.shutdown] in order to properly finalize it.
     *
     * @throws IOException thrown if audio recorder can not be created for some reason.
     */
    init {
        if (recorder.state == AudioRecord.STATE_UNINITIALIZED) {
            recorder.release()
            throw IOException(
                "Failed to initialize recorder. Microphone might be already in use."
            )
        }
    }
}