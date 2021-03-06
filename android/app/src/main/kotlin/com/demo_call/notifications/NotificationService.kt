package com.demo_call.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.demo_call.CALL_STATE_ACCEPT
import com.demo_call.CALL_STATE_REJECT
import com.demo_call.R
import com.demo_call.activities.*
import com.demo_call.utils.IntentUtils.putInfoExtra
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.demo_call.models.StringeePayload
import com.demo_call.utils.Common
import com.demo_call.utils.IntentUtils.retrieveDataFromFcm
import com.stringee.StringeeClient

class NotificationService : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("NotificationService", "onMessageReceived: ${message.data}")
        if (message.data.isNotEmpty()) {
            val client = StringeeClient(this)
            if (!client.isConnected)
                if (Common.token.value == null){
                    val dataSave = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                    Handler(mainLooper).post{
                        Common.token.value = dataSave.getString("token","")
                        MainActivity().initAndConnectStringee(Common.token.value!!,client)
                    }
                }
            val stringeePayload = retrieveDataFromFcm<StringeePayload>(message)
            Log.d("TAG", "onMessageReceived: payload converted: $stringeePayload")
            when (stringeePayload.data.callStatus) {
                "started" -> showCallNotification(
                    callId = stringeePayload.data.callID,
                    callerName = stringeePayload.data.from.alias,
                    photoUrl = null,
                    payload = stringeePayload
                )
                "ended" -> cancelNotification(stringeePayload.data.callID.hashCode())
            }

        }
        super.onMessageReceived(message)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showCallNotification(
        callId: String,
        callerName: String,
        photoUrl: String?,
        payload: StringeePayload
    ) {
        val notificationManager = NotificationManagerCompat.from(this)
        val soundUri =
            Uri.parse("android.resource://" + applicationContext.packageName + "/" + R.raw.incoming_call_ringtone)

        val lockedScreenAction = Intent(this, IncomingInvitationActivity::class.java)
        lockedScreenAction.apply {
            putExtra("call_id", callId)
            putExtra("caller_name", callerName)
            putExtra("photo_url", photoUrl)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val lockScreenIntent = PendingIntent.getActivity(
            applicationContext,
            callId.hashCode(),
            lockedScreenAction,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val contentText = "Incoming call"
        val notificationBuilder = NotificationCompat.Builder(this, CALL_CHANNEL_ID)
        notificationBuilder
            .setSmallIcon(R.drawable.notification_icon)
            .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
            .setContentTitle(callerName)
            .setContentText(contentText)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setTimeoutAfter(60000)
            .setFullScreenIntent(lockScreenIntent, true)

        addRejectCallAction(notificationBuilder, callId, payload)

        addAcceptCallAction(notificationBuilder, callId, payload)

        setNotificationColor(this, notificationBuilder)

        createCallNotificationChannel(notificationManager, soundUri)
        notificationManager.notify(callId.hashCode(), notificationBuilder.build())
    }

    private fun addAcceptCallAction(
        notificationBuilder: NotificationCompat.Builder,
        callId: String,
        payload: StringeePayload,
    ) {
        //Handle click on notification - Accept
        val receiveCallAction = Intent(this, NotificationReceiver::class.java)
        receiveCallAction.apply {
            putExtra("call_id", callId)
            putInfoExtra("payload", payload)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        receiveCallAction.action = CALL_STATE_ACCEPT

        val receiveCallPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            callId.hashCode(),
            receiveCallAction, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val acceptAction: NotificationCompat.Action = NotificationCompat.Action.Builder(
            this.resources.getIdentifier("ic_menu_call", "drawable", this.packageName),
            getColorizedText("Accept", "#4CB050"),
            receiveCallPendingIntent
        )
            .build()
        notificationBuilder.addAction(acceptAction)
    }

    private fun addRejectCallAction(
        notificationBuilder: NotificationCompat.Builder,
        callId: String,
        payload: StringeePayload,
    ) {
        //Handle click on notification - Decline
        val cancelCallAction = Intent(this, NotificationReceiver::class.java)
        cancelCallAction.apply {
            putExtra("call_id", callId)
            putInfoExtra("payload", payload)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        cancelCallAction.action = CALL_STATE_REJECT

        val cancelCallPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            callId.hashCode(),
            cancelCallAction,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val declineAction: NotificationCompat.Action = NotificationCompat.Action.Builder(
            this.resources.getIdentifier(
                "ic_menu_close_clear_cancel",
                "drawable",
                this.packageName
            ),
            getColorizedText("Reject", "#E02B00"),
            cancelCallPendingIntent
        )
            .build()
        notificationBuilder.addAction(declineAction)
    }

    private fun setNotificationColor(
        context: Context,
        notificationBuilder: NotificationCompat.Builder
    ) {
        val accentID = context.resources.getIdentifier(
            "call_notification_color_accent",
            "color",
            context.packageName
        )
        if (accentID != 0) {
            notificationBuilder.color = context.resources.getColor(accentID, null)
        } else {
            notificationBuilder.color = Color.parseColor("#4CAF50")
        }
    }

    private fun getColorizedText(string: String, colorHex: String): Spannable {
        val spannable: Spannable = SpannableString(string)
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor(colorHex)),
            0,
            spannable.length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        return spannable
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createCallNotificationChannel(
        notificationManager: NotificationManagerCompat,
        sound: Uri
    ) {
        val channel = NotificationChannel(
            CALL_CHANNEL_ID,
            CALL_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.setSound(
            sound, AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build()
        )
        notificationManager.createNotificationChannel(channel)
    }
}