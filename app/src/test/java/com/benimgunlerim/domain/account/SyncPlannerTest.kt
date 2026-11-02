package com.benimgunlerim.domain.account

import com.benimgunlerim.data.account.CloudBackupMeta
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncPlannerTest {
    private val cloud = CloudBackupMeta(updatedAtMillis = 2_000L, taskCount = 3, routineCount = 2)

    private fun plan(trigger: SyncTrigger, first: Boolean, tasks: Int, routines: Int, cloud: CloudBackupMeta?, last: Long = 0L) =
        SyncPlanner.plan(trigger, first, tasks, routines, cloud, last)

    @Test fun firstSync_emptyCloud_uploadsGuestData() =
        assertEquals(SyncAction.Upload, plan(SyncTrigger.Start, true, 5, 1, null))

    @Test fun firstSync_emptyPhone_downloadsCloud() =
        assertEquals(SyncAction.Download, plan(SyncTrigger.Start, true, 0, 0, cloud))

    @Test fun firstSync_bothHaveData_asksUser() =
        assertEquals(SyncAction.AskUser, plan(SyncTrigger.Start, true, 5, 1, cloud))

    @Test fun later_cloudNewer_downloadsOnStartAndManual() {
        assertEquals(SyncAction.Download, plan(SyncTrigger.Start, false, 5, 1, cloud, last = 1_000L))
        assertEquals(SyncAction.Download, plan(SyncTrigger.Manual, false, 5, 1, cloud, last = 1_000L))
    }

    @Test fun later_cloudNewer_backgroundNeverOverwritesIt() =
        assertEquals(SyncAction.None, plan(SyncTrigger.Background, false, 5, 1, cloud, last = 1_000L))

    @Test fun later_upToDate_backgroundAndManualUpload_startDoesNothing() {
        assertEquals(SyncAction.Upload, plan(SyncTrigger.Background, false, 5, 1, cloud, last = 2_000L))
        assertEquals(SyncAction.Upload, plan(SyncTrigger.Manual, false, 5, 1, cloud, last = 2_000L))
        assertEquals(SyncAction.None, plan(SyncTrigger.Start, false, 5, 1, cloud, last = 2_000L))
    }

    @Test fun later_cloudDeletedElsewhere_reuploads() =
        assertEquals(SyncAction.Upload, plan(SyncTrigger.Background, false, 5, 1, null, last = 2_000L))
}
