package net.moonbaseone.vera.shared.pocket

/**
 * Local appointment consultation recording (not symptom STT).
 * Platform actuals live in androidApp / iosApp for MediaRecorder.
 */
interface AppointmentAudioRecorder {
    fun isAvailable(): Boolean
    /** Starts recording to a new local file; returns path on success. */
    suspend fun start(appointmentId: String): Result<String>
    /** Stops recording and keeps the file; returns final path. */
    suspend fun stop(): Result<String>
    fun isRecording(): Boolean
    /** Deletes a previously saved recording file from disk. */
    fun deleteFile(path: String): Boolean
}

interface AppointmentAudioPlayer {
    fun isAvailable(): Boolean
    /** Loads [path] without starting playback; returns duration in ms. */
    suspend fun prepare(path: String): Result<Long>
    fun play()
    fun pause()
    fun stop()
    fun seekTo(positionMs: Long)
    fun seekBy(deltaMs: Long)
    fun getPositionMs(): Long
    fun getDurationMs(): Long
    fun isPlaying(): Boolean

    /**
     * Convenience for list screens: prepare, play to completion, then stop.
     */
    suspend fun play(path: String): Result<Unit>
}

class FakeAppointmentAudioRecorder : AppointmentAudioRecorder {
    private var recording = false
    private var path: String? = null

    override fun isAvailable(): Boolean = true

    override suspend fun start(appointmentId: String): Result<String> {
        recording = true
        path = "fake://recording/$appointmentId.m4a"
        return Result.success(path!!)
    }

    override suspend fun stop(): Result<String> {
        recording = false
        return Result.success(path ?: "fake://recording/unknown.m4a")
    }

    override fun isRecording(): Boolean = recording

    override fun deleteFile(path: String): Boolean = true
}

class FakeAppointmentAudioPlayer : AppointmentAudioPlayer {
    private var playing = false
    private var positionMs = 0L
    private var durationMs = 60_000L

    override fun isAvailable(): Boolean = true

    override suspend fun prepare(path: String): Result<Long> {
        playing = false
        positionMs = 0L
        durationMs = 60_000L
        return Result.success(durationMs)
    }

    override fun play() {
        playing = true
    }

    override fun pause() {
        playing = false
    }

    override fun stop() {
        playing = false
        positionMs = 0L
    }

    override fun seekTo(positionMs: Long) {
        this.positionMs = positionMs.coerceIn(0L, durationMs)
    }

    override fun seekBy(deltaMs: Long) {
        seekTo(positionMs + deltaMs)
    }

    override fun getPositionMs(): Long = positionMs

    override fun getDurationMs(): Long = durationMs

    override fun isPlaying(): Boolean = playing

    override suspend fun play(path: String): Result<Unit> {
        prepare(path)
        play()
        playing = false
        positionMs = durationMs
        return Result.success(Unit)
    }
}
