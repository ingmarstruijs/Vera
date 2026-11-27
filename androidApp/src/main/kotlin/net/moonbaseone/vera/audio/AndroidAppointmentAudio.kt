package net.moonbaseone.vera.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    override fun deleteFile(path: String): Boolean {
        return try {
            if (path.isBlank() || path.startsWith("fake://")) return true
            File(path).delete()
        } catch (_: Exception) {
            false
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

    override suspend fun prepare(path: String): Result<Long> = withContext(Dispatchers.IO) {
        runCatching {
            releasePlayer()
            val mp = MediaPlayer().apply {
                setDataSource(path)
                prepare()
            }
            player = mp
            mp.duration.toLong().coerceAtLeast(0L)
        }
    }

    override fun play() {
        val mp = player ?: return
        if (!mp.isPlaying) {
            try {
                mp.start()
            } catch (_: Exception) {
            }
        }
    }

    override fun pause() {
        try {
            if (player?.isPlaying == true) player?.pause()
        } catch (_: Exception) {
        }
    }

    override fun stop() {
        try {
            player?.let { mp ->
                if (mp.isPlaying) mp.pause()
                mp.seekTo(0)
            }
        } catch (_: Exception) {
        }
    }

    override fun seekTo(positionMs: Long) {
        try {
            val mp = player ?: return
            val clamped = positionMs.toInt().coerceIn(0, mp.duration.coerceAtLeast(0))
            mp.seekTo(clamped)
        } catch (_: Exception) {
        }
    }

    override fun seekBy(deltaMs: Long) {
        seekTo(getPositionMs() + deltaMs)
    }

    override fun getPositionMs(): Long =
        try {
            player?.currentPosition?.toLong() ?: 0L
        } catch (_: Exception) {
            0L
        }

    override fun getDurationMs(): Long =
        try {
            player?.duration?.toLong()?.coerceAtLeast(0L) ?: 0L
        } catch (_: Exception) {
            0L
        }

    override fun isPlaying(): Boolean =
        try {
            player?.isPlaying == true
        } catch (_: Exception) {
            false
        }

    override suspend fun play(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            prepare(path).getOrThrow()
            play()
            while (isPlaying()) {
                delay(100)
            }
            stop()
            releasePlayer()
        }
    }

    private fun releasePlayer() {
        try {
            player?.stop()
        } catch (_: Exception) {
        }
        try {
            player?.release()
        } catch (_: Exception) {
        } finally {
            player = null
        }
    }
}
