package net.moonbaseone.vera.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.moonbaseone.vera.shared.pocket.AppointmentAudioPlayer
import net.moonbaseone.vera.shared.pocket.AppointmentAudioRecorder
import java.io.File

class AndroidAppointmentAudioRecorder(
    private val context: Context,
) : AppointmentAudioRecorder {
    private var recorder: MediaRecorder? = null
    private var currentPath: String? = null

    override fun isAvailable(): Boolean = true

    override fun isRecording(): Boolean = recorder != null

    override suspend fun start(appointmentId: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            stopInternal()
            val dir = File(context.filesDir, "appointment_recordings").apply { mkdirs() }
            val file = File(dir, "$appointmentId-${System.currentTimeMillis()}.m4a")
            val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            mr.setAudioSource(MediaRecorder.AudioSource.MIC)
            mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mr.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mr.setOutputFile(file.absolutePath)
            mr.prepare()
            mr.start()
            recorder = mr
            currentPath = file.absolutePath
            file.absolutePath
        }
    }

    override suspend fun stop(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val path = currentPath ?: error("Not recording")
            stopInternal()
            path
        }
    }

    private fun stopInternal() {
        try {
            recorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (_: Exception) {
        } finally {
            recorder = null
        }
    }
}

class AndroidAppointmentAudioPlayer : AppointmentAudioPlayer {
    private var player: MediaPlayer? = null

    override fun isAvailable(): Boolean = true

    override suspend fun play(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            stop()
            val mp = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                start()
            }
            player = mp
            while (mp.isPlaying) {
                Thread.sleep(100)
            }
            stop()
        }
    }

    override fun stop() {
        try {
            player?.stop()
            player?.release()
        } catch (_: Exception) {
        } finally {
            player = null
        }
    }
}
