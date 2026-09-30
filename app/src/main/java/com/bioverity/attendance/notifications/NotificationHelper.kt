package com.bioverity.attendance.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object NotificationHelper {

    private const val CHANNEL_ID =
        "bioverity_system_notifications"

    private const val CHANNEL_NAME =
        "BioVerity Notifications"

    private const val CHANNEL_DESCRIPTION =
        "BioVerity attendance notifications"


    // ----------------------------------------------------
    // CREATE NOTIFICATION CHANNEL
    // ----------------------------------------------------

    fun createNotificationChannel(
        context: Context
    ) {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {

                    description =
                        CHANNEL_DESCRIPTION

                    enableVibration(true)

                    setShowBadge(true)
                }


            val notificationManager =
                context.getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager


            notificationManager
                .createNotificationChannel(
                    channel
                )
        }
    }


    // ----------------------------------------------------
    // SHOW SYSTEM NOTIFICATION
    // ----------------------------------------------------

    fun showSystemNotification(
        context: Context,
        title: String,
        message: String
    ) {

        // Android 13+
        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {

            val permission =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                )


            if (
                permission !=
                PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }


        // Create notification channel
        createNotificationChannel(context)


        // Build notification
        val notification =
            NotificationCompat.Builder(
                context,
                CHANNEL_ID
            )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )
                .setContentTitle(
                    title
                )
                .setContentText(
                    message
                )
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(message)
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setAutoCancel(true)
                .setDefaults(
                    NotificationCompat.DEFAULT_ALL
                )
                .build()


        // Show notification
        NotificationManagerCompat
            .from(context)
            .notify(
                System.currentTimeMillis()
                    .toInt(),
                notification
            )
    }
}