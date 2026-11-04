package net.moonbaseone.vera.shared.vault

/** Persisted ciphertext row (SQLDelight / in-memory). */
data class EncryptedMedicalRecordRow(
    val uuid: String,
    val encryptedPayload: String,
    val encryptedTitle: String,
    val resourceType: String,
    val dateRecorded: String,
    val iknlGuidelineRef: String? = null,
    val fhirStatus: String? = null,
    val tagsCsv: String = "",
)

interface MedicalRecordStore {
    suspend fun upsert(row: EncryptedMedicalRecordRow)
    suspend fun getAllSortedByDate(): List<EncryptedMedicalRecordRow>
    suspend fun deleteByUuid(uuid: String)
    suspend fun clear()
}

class InMemoryMedicalRecordStore : MedicalRecordStore {
    private val rows = LinkedHashMap<String, EncryptedMedicalRecordRow>()

    override suspend fun upsert(row: EncryptedMedicalRecordRow) {
        rows[row.uuid] = row
    }

    override suspend fun getAllSortedByDate(): List<EncryptedMedicalRecordRow> =
        rows.values.sortedBy { it.dateRecorded }

    override suspend fun deleteByUuid(uuid: String) {
        rows.remove(uuid)
    }

    override suspend fun clear() {
        rows.clear()
    }
}
