package com.testnotify.app

import android.app.Service
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.testnotify.app.R
import com.testnotify.app.ScrollingActivity
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL


class FirebaseMessaging : FirebaseMessagingService() {

    val TAG = "FirebaseMessaging"
    var NOTIFICATION_ID = 0;
    lateinit var mBuilder : NotificationCompat.Builder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel();
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        Log.d(TAG, "Creating notification channel")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "TESTNOTIFY"
            val descriptionText = "Testing Notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("TESTNOTIFY", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

     override fun onMessageReceived(remoteMessage: RemoteMessage) {
         super.onMessageReceived(remoteMessage)

         val messageData = remoteMessage?.notification
         var image = Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888);
         val myIntent = Intent(this, ScrollingActivity::class.java)
         val pendingIntent = PendingIntent.getActivity(
             this,
             0,
             myIntent,
             PendingIntent.FLAG_ONE_SHOT
         )

         try {
             image =
                 BitmapFactory.decodeStream(URL(messageData?.imageUrl.toString()).openConnection().getInputStream());
         } catch (e: MalformedURLException){
             Log.d(TAG,"No URL Provided or malformed url")
         } catch (e: OutOfMemoryError){
             Log.d(TAG,"Stream too big")
         } catch (e: IOException){
             Log.d(TAG,"Connection unable to be established")
         }

         mBuilder = NotificationCompat.Builder(this, "TESTNOTIFY")
             .setContentTitle(messageData?.title)
             .setContentText(messageData?.body)
             .setPriority(NotificationCompat.PRIORITY_HIGH)
             .setSmallIcon(R.mipmap.ic_launcher)
             .setLargeIcon(image)
             .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
             .setContentIntent(pendingIntent)
             //TODO:Dynamic Links  ^ https://firebase.google.com/docs/dynamic-links/android/receive instead of
             // launching activity directly
             .setAutoCancel(true)

         with(NotificationManagerCompat.from(this)) {
             // notificationId is a unique int for each notification that you must define
             notify(NOTIFICATION_ID, mBuilder.build())
             NOTIFICATION_ID++
         }
    }
}

