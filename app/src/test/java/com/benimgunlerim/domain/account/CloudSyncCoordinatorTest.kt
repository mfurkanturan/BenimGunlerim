package com.benimgunlerim.domain.account

import com.benimgunlerim.data.DataExportService
import com.benimgunlerim.data.DataImportService
import com.benimgunlerim.data.LocalDataClearer
import com.benimgunlerim.data.UserPreferencesSource
import com.benimgunlerim.data.account.AccountError
import com.benimgunlerim.data.account.AccountPrefs
import com.benimgunlerim.data.account.AccountRepository
import com.benimgunlerim.data.account.AccountResult
import com.benimgunlerim.data.account.AccountState
import com.benimgunlerim.data.account.CloudBackupMeta
import com.benimgunlerim.data.account.CloudBackupStore
import com.benimgunlerim.data.account.EntryChoice
import com.benimgunlerim.data.local.RoutineDao
import com.benimgunlerim.data.local.TaskDao
import com.benimgunlerim.domain.DateTimeProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CloudSyncCoordinatorTest {
    private val account: AccountRepository = mockk(relaxed = true)
    private val store: CloudBackupStore = mockk(relaxed = true)
    private val prefs: AccountPrefs = mockk(relaxed = true)
    private val exporter: DataExportService = mockk(relaxed = true)
    private val importer: DataImportService = mockk(relaxed = true)
    private val clearer: LocalDataClearer = mockk(relaxed = true)
    private val taskDao: TaskDao = mockk(relaxed = true)
    private val routineDao: RoutineDao = mockk(relaxed = true)
    private val userPrefs: UserPreferencesSource = mockk(relaxed = true)
    private val clock: DateTimeProvider = mockk(relaxed = true)
    private lateinit var sut: CloudSyncCoordinator

    @Before fun setUp() {
        every { account.currentState() } returns AccountState.SignedIn("u1", "a@b.co", true)
        every { prefs.lastSyncAt } returns kotlinx.coroutines.flow.MutableStateFlow(0L)
        every { clock.currentTimeMillis() } returns 5_000L
        coEvery { exporter.exportToJson() } returns "{}"
        sut = CloudSyncCoordinator(account, store, prefs, exporter, importer, clearer, taskDao, routineDao, userPrefs, {}, { _, _, _, _ -> }, clock)
    }

    @Test fun guestUpgrade_bothHaveData_asksAndDoesNotOverwrite() = runTest {
        every { prefs.hasSyncedAccount("u1") } returns false
        coEvery { taskDao.count() } returns 4
        coEvery { store.meta("u1") } returns CloudBackupMeta(1_000L, 9, 2)

        val outcome = sut.sync(SyncTrigger.Start)

        assertTrue(outcome is SyncOutcome.NeedsChoice)
        coVerify(exactly = 0) { store.upload(any(), any(), any()) }
        coVerify(exactly = 0) { importer.importFromJson(any()) }
    }

    @Test fun guestUpgrade_emptyCloud_uploadsAndMarksAccount() = runTest {
        every { prefs.hasSyncedAccount("u1") } returns false
        coEvery { taskDao.count() } returns 4
        coEvery { store.meta("u1") } returns null

        val outcome = sut.sync(SyncTrigger.Start)

        assertEquals(SyncOutcome.Done(SyncAction.Upload), outcome)
        coVerify { store.upload("u1", "{}", any()) }
        verify { prefs.markSyncedAccount("u1") }
        verify { prefs.setEntryChoice(EntryChoice.Account) }
    }

    @Test fun notSignedIn_doesNothing() = runTest {
        every { account.currentState() } returns AccountState.Guest
        assertEquals(SyncOutcome.NotSignedIn, sut.sync(SyncTrigger.Background))
        coVerify(exactly = 0) { store.meta(any()) }
    }

    @Test fun networkError_reportsFailedWithoutCrashing() = runTest {
        coEvery { store.meta("u1") } throws RuntimeException("offline")
        assertEquals(SyncOutcome.Failed(AccountError.Network), sut.sync(SyncTrigger.Manual))
    }

    @Test fun deleteAccount_wrongPassword_deletesNothing() = runTest {
        coEvery { account.reauthenticate("yanlis") } returns AccountResult.Failure(AccountError.WrongCredentials)

        val r = sut.deleteAccount("yanlis")

        assertEquals(AccountResult.Failure(AccountError.WrongCredentials), r)
        coVerify(exactly = 0) { store.delete(any()) }
        coVerify(exactly = 0) { account.deleteUser() }
    }

    @Test fun deleteAccount_ok_removesCloudThenUserAndBecomesGuest() = runTest {
        coEvery { account.reauthenticate("dogru") } returns AccountResult.Success(Unit)
        coEvery { account.deleteUser() } returns AccountResult.Success(Unit)

        sut.deleteAccount("dogru")

        coVerify { store.delete("u1") }
        coVerify { account.deleteUser() }
        verify { prefs.setEntryChoice(EntryChoice.Guest) }
    }
}
