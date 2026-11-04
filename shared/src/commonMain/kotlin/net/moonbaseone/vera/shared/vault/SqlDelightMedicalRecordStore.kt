package net.moonbaseone.vera.shared.vault

import net.moonbaseone.vera.shared.db.VeraDatabase

/** SQLDelight-backed [MedicalRecordStore] for Android/iOS drivers. */
class SqlDelightMedicalRecordStore(
    private val database: VeraDatabase,
) : MedicalRecordStore {
    private val queries get() = database.medicalRecordQueries

    override suspend fun upsert(row: EncryptedMedicalRecordRow) {
        queries.upsertMedicalRecord(
            uuid = row.uuid,
            encryptedPayload = row.encryptedPayload,
            encryptedTitle = row.encryptedTitle,
            resourceType = row.resourceType,
            dateRecorded = row.dateRecorded,
            iknlGuidelineRef = row.iknlGuidelineRef,
            fhirStatus = row.fhirStatus,
            tagsCsv = row.tagsCsv,
        )
    }

    override suspend fun getAllSortedByDate(): List<EncryptedMedicalRecordRow> =
        queries.selectAllMedicalRecordsSorted().executeAsList().map { entity ->
            EncryptedMedicalRecordRow(
                uuid = entity.uuid,
                encryptedPayload = entity.encryptedPayload,
                encryptedTitle = entity.encryptedTitle,
                resourceType = entity.resourceType,
                dateRecorded = entity.dateRecorded,
                iknlGuidelineRef = entity.iknlGuidelineRef,
                fhirStatus = entity.fhirStatus,
                tagsCsv = entity.tagsCsv,
            )
        }

    override suspend fun deleteByUuid(uuid: String) {
        queries.deleteMedicalRecordByUuid(uuid)
    }

    override suspend fun clear() {
        queries.clearMedicalRecords()
    }
}
