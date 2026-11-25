package net.moonbaseone.vera.shared.pocket

/**
 * Local appointment consultation recording (not symptom STT).
 * Platform actuals live in androidApp / iosApp for MediaRecorder.
 */
interface AppointmentAudioRecorder {
    fun isAvailable(): Boolean
    /** Starts recording to a new local file; returns path on success. */
    suspend fun start(appointmentId: String): Result<String>
    /** Stops recording; returns final path. */
    suspend fun stop(): Result<String>
    fun isRecording(): Boolean
}

interface AppointmentAudioPlayer {
    fun isAvailable(): Boolean
    suspend fun play(path: String): Result<Unit>
    fun stop()
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
}

class FakeAppointmentAudioPlayer : AppointmentAudioPlayer {
    override fun isAvailable(): Boolean = true
    override suspend fun play(path: String): Result<Unit> = Result.success(Unit)
    override fun stop() = Unit
}
