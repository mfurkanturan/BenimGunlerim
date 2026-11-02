package com.benimgunlerim.data.account

import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Buluttaki yedek: `users/{uid}` belgesinde, uygulamanın dışa aktarma JSON'u gzip'li Blob olarak.
 * Tek belge = atomik yazma; Firestore 1 MB sınırının çok altında (sıkıştırılmış).
 */
interface CloudBackupStore {
    suspend fun meta(uid: String): CloudBackupMeta?
    suspend fun download(uid: String): String?
    suspend fun upload(uid: String, json: String, meta: CloudBackupMeta)
    suspend fun delete(uid: String)
}

@Singleton
class FirestoreCloudBackupStore @Inject constructor(
    private val accountRepository: AccountRepository,
) : CloudBackupStore {

    private val db: FirebaseFirestore? by lazy {
        if (accountRepository.isAvailable) FirebaseFirestore.getInstance() else null
    }

    private fun doc(uid: String) = requireNotNull(db) { "Firebase yapılandırılmamış" }.collection(USERS).document(uid)

    override suspend fun meta(uid: String): CloudBackupMeta? {
        val snap = doc(uid).get().await()
        if (!snap.exists()) return null
        return CloudBackupMeta(
            updatedAtMillis = snap.getLong(F_UPDATED) ?: 0L,
            taskCount = (snap.getLong(F_TASKS) ?: 0L).toInt(),
            routineCount = (snap.getLong(F_ROUTINES) ?: 0L).toInt(),
        )
    }

    override suspend fun download(uid: String): String? {
        val snap = doc(uid).get().await()
        val blob = snap.getBlob(F_SNAPSHOT) ?: return null
        return gunzip(blob.toBytes())
    }

    override suspend fun upload(uid: String, json: String, meta: CloudBackupMeta) {
        doc(uid).set(
            mapOf(
                F_SNAPSHOT to Blob.fromBytes(gzip(json)),
                F_UPDATED to meta.updatedAtMillis,
                F_TASKS to meta.taskCount,
                F_ROUTINES to meta.routineCount,
                F_SCHEMA to SCHEMA_VERSION,
            ),
        ).await()
    }

    override suspend fun delete(uid: String) {
        doc(uid).delete().await()
    }

    private companion object {
        const val USERS = "users"
        const val F_SNAPSHOT = "snapshot"
        const val F_UPDATED = "updatedAt"
        const val F_TASKS = "taskCount"
        const val F_ROUTINES = "routineCount"
        const val F_SCHEMA = "schemaVersion"
        const val SCHEMA_VERSION = 1L
    }
}

internal fun gzip(text: String): ByteArray =
    ByteArrayOutputStream().also { out -> GZIPOutputStream(out).use { it.write(text.toByteArray(Charsets.UTF_8)) } }.toByteArray()

internal fun gunzip(bytes: ByteArray): String =
    GZIPInputStream(ByteArrayInputStream(bytes)).use { it.readBytes().toString(Charsets.UTF_8) }
