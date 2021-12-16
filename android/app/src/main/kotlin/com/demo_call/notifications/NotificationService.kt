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
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.demo_call.*
import com.demo_call.activities.IncomingInvitationActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.studyguide.mightyid.models.RequestCall
import com.demo_call.IntentUtils.putInfoExtra
import com.demo_call.activities.CALL_CHANNEL_ID
import com.demo_call.activities.CALL_CHANNEL_NAME

class NotificationService : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (message.data.isNotEmpty()) {
            Log.d("NotificationService", "onMessageReceived: message.data: ${message.data}")
//            when (message.data[MESSAGE_TYPE]) {
//                CALL_REQUEST -> {
//                    val data = retrieveDataFromFcm<RequestCall>(message)
//                    Log.d("NotificationService", "onMessageReceived: $data")
//                    Log.d("NotificationService", "onMessageReceived: is app in background: ${MainActivity.isBackground}")
//                    if (MainActivity.isBackground)
//                        showCallNotification(data.callId!!,
//                            data.meetingType!!,
//                            data.callerName!!,
//                            data.callerPhotoURL,
//                            data)
//                }
//                CALL_RESPONSE -> {
//                    if (message.data["response"] == CALL_STATE_MISSED){
//                        val callId = message.data["callId"]!!
//                        cancelNotification(callId.hashCode())
//                        val intent = Intent(CALL_STATE_MISSED)
//                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
//                    }
//                }
//
//                MEMBER_IN_TOPIC ->{
//                    if (Looper.myLooper() == null) Looper.prepare()
//                    Toast.makeText(this, "Call ended", Toast.LENGTH_LONG).show()
//                    val intent = BroadcastIntentHelper.buildHangUpIntent()
//                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
//                }
//
//                CLOSE_POP_UP_CALL ->{
//                    Log.d("TAG", "onMessageReceived: CLOSE_POP_UP_CALL, data :${message.data["call_id"]}")
//                    val callId = message.data["call_id"]
//                    cancelNotification(callId.hashCode())
//                    val intent = Intent(CLOSE_POP_UP_CALL)
//                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
//                }
//            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showCallNotification(
            callId: String,
            meetingType: String,
            callerName: String,
            photoUrl: String?,
            payload: RequestCall,
    ) {
        val notificationManager = NotificationManagerCompat.from(this)
        val soundUri =
            Uri.parse("android.resource://" + applicationContext.packageName + "/" + R.raw.incoming_call_ringtone)

        val lockedScreenAction = Intent(this, IncomingInvitationActivity::class.java)
        lockedScreenAction.apply {
            putExtra("call_id", callId)
            putExtra("meeting_type", meetingType)
            putExtra("caller_name", callerName)
            putExtra("photo_url", photoUrl)
            putInfoExtra("payload", payload)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val lockScreenIntent = PendingIntent.getActivity(
                applicationContext,
                callId.hashCode(),
                lockedScreenAction,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        val contentText = "Incoming MightyID " + if (meetingType == "video") "video call" else "Call"
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
            payload: RequestCall,
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
            payload: RequestCall,
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
    private fun createCallNotificationChannel(notificationManager: NotificationManagerCompat, sound: Uri) {
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