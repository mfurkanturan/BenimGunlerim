package com.benimgunlerim.notifications.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.benimgunlerim.MainActivity
import com.benimgunlerim.R
import com.benimgunlerim.notifications.NotificationConstants
import com.benimgunlerim.notifications.ReminderPolicy
import com.benimgunlerim.notifications.safeNotify
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Firebase Console › Messaging'den gönderilen duyurular.
 *
 * Uygulama arka plandayken "notification" yükü olan mesajları sistem kendisi gösterir (kanal ve ikon
 * manifest'teki varsayılanlardan gelir). Buraya düşenler: uygulama açıkken gelen her mesaj ve
 * yalnızca "data" yükü olan mesajlar. Veri anahtarları: title, body, route (today / plan / routines).
 */
@AndroidEntryPoint
class PushMessagingService : FirebaseMessagingService() {

    @Inject lateinit var reminderPolicy: ReminderPolicy

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data[KEY_TITLE] ?: return
        val body = message.notification?.body ?: message.data[KEY_BODY].orEmpty()
        // Kullanıcı bildirimleri kapattıysa ya da sessiz saatlerdeyse duyuru da beklemez, düşer.
        if (!reminderPolicy.canShowNotification()) return
        showAnnouncement(title, body, message.data[KEY_ROUTE])
    }

    // Konu (topic) aboneliği kullanıldığı için cihaz jetonunun sunucuya gönderilmesi gerekmez.
    override fun onNewToken(token: String) = PushTopics.subscribe(this)

    private fun showAnnouncement(title: String, body: String, route: String?) {
        ensureAnnouncementChannel()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            route?.let { putExtra(NotificationConstants.EXTRA_START_ROUTE, it) }
        }
        val pending = PendingIntent.getActivity(
            this,
            ANNOUNCEMENT_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(this, NotificationConstants.ANNOUNCEMENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        safeNotify(ANNOUNCEMENT_ID, notification)
    }

    private companion object {
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
        const val KEY_ROUTE = "route"

        /** Duyurular tek kimlik kullanır: yenisi eskisinin yerine geçer, bildirim çekmecesi dolmaz. */
        const val ANNOUNCEMENT_ID = 22_000
    }
}

/** Uygulama açılışında oluşturulur ki arka planda sistemin göstereceği duyurular da doğru kanala düşsün. */
fun Context.ensureAnnouncementChannel() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channel = NotificationChannel(
        NotificationConstants.ANNOUNCEMENT_CHANNEL_ID,
        getString(R.string.notif_channel_announcement_name),
        NotificationManager.IMPORTANCE_DEFAULT,
    ).apply {
        description = getString(R.string.notif_channel_announcement_desc)
    }
    manager.createNotificationChannel(channel)
}
