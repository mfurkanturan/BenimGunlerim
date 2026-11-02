package com.benimgunlerim.notifications.push

import android.content.Context
import com.benimgunlerim.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Duyurular konu (topic) üzerinden gider; hesap gerekmez, misafirler de alır.
 *
 * Yayın sürümü [TOPIC_ALL]'a, test sürümü [TOPIC_DEBUG]'a abone olur: Console'dan önce "debug"
 * konusuna gönderip kendi telefonunda denersin, sonra "all" ile herkese yollarsın.
 */
object PushTopics {
    const val TOPIC_ALL = "all"
    const val TOPIC_DEBUG = "debug"

    fun subscribe(context: Context) {
        // google-services.json yokken (misafir derlemesi) Firebase başlatılmaz; sessizce atlanır.
        if (FirebaseApp.getApps(context).isEmpty()) return
        val topic = if (BuildConfig.DEBUG) TOPIC_DEBUG else TOPIC_ALL
        runCatching { FirebaseMessaging.getInstance().subscribeToTopic(topic) }
    }
}
