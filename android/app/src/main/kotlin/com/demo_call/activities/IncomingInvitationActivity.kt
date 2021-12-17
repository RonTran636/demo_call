package com.demo_call.activities

import android.app.Activity
import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.demo_call.*
import com.demo_call.databinding.ActivityIncomingInvitationBinding
import com.studyguide.mightyid.*
import com.demo_call.notifications.NotificationReceiver
import com.demo_call.utils.IntentUtils.getInfoExtra
import com.demo_call.utils.IntentUtils.putInfoExtra
import com.demo_call.models.RequestCall
import com.demo_call.models.StringeePayload

class IncomingInvitationActivity : Activity() {
    private lateinit var localBroadcastManager: LocalBroadcastManager
    private lateinit var binding: ActivityIncomingInvitationBinding
    private val broadcastReceiver = NotificationReceiver()
    private var payload: StringeePayload? = null
    private var callId: String? = null
    private var meetingType: String? = null
    private var callerName: String? = null
    private var callerPhotoUrl: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        turnScreenOnAndKeyguardOff()
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_incoming_invitation)
        processIncomingData(intent)
        initUI()
        registerCallStateReceiver()
    }

    override fun onStart() {
        super.onStart()
        Handler(Looper.myLooper()!!).postDelayed({
            if (payload == null) {
                finish()
            }
        }, 60000)
    }

    override fun onDestroy() {
        super.onDestroy()
        turnScreenOffAndKeyguardOn()
        localBroadcastManager.apply {
            unregisterReceiver(localBr)
            unregisterReceiver(broadcastReceiver)
        }
    }

    private fun registerCallStateReceiver() {
        IntentFilter().apply {
            addAction(ACTION_CALL_ACCEPT)
            addAction(ACTION_CALL_REJECT)
        }.also {
            localBroadcastManager.registerReceiver(broadcastReceiver, it)
        }
        IntentFilter().apply {
            addAction(CALL_STATE_MISSED)
            addAction(CLOSE_POP_UP_CALL)
        }.also {
            localBroadcastManager.registerReceiver(localBr, it)
        }
    }

    private fun processIncomingData(intent: Intent) {
        callId = intent.getStringExtra(CALL_ID)
        meetingType = intent.getStringExtra(CALL_MEETING_TYPE)
        callerName = intent.getStringExtra(CALLER_NAME)
        callerPhotoUrl = intent.getStringExtra(CALLER_PHOTO_URL)
        payload = intent.getInfoExtra(CALL_PAYLOAD)
    }

    private fun initUI() {
        binding.callerName = callerName
        binding.incomingMessage.text = getString(R.string.incoming_message)
        binding.incomingCallerAvatar.loadImage(callerPhotoUrl)
        binding.incomingCallerAccept.setOnClickListener { onStartCall() }
        binding.incomingCallerReject.setOnClickListener { onEndCall() }
    }

    // calls from layout file
    private fun onEndCall() {
        Log.d("IncomingInvitationActivity", "onEndCall: Called")
        val intent = Intent(ACTION_CALL_REJECT)
        intent.putInfoExtra(CALL_PAYLOAD, payload)
        localBroadcastManager.sendBroadcast(intent)
        this.moveTaskToBack(true)
        finish()
    }

    // calls from layout file
    private fun onStartCall() {
        Log.d("IncomingInvitationActivity", "onStartCall: Called")
        val intent = Intent(ACTION_CALL_ACCEPT)
        intent.putInfoExtra(CALL_PAYLOAD, payload)
        localBroadcastManager.sendBroadcast(intent)
        finish()
    }

    // Internal broadcast receiver
    private val localBr = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent == null || intent.action?.length == 0) return
            if (intent.action == CALL_STATE_MISSED || intent.action == CLOSE_POP_UP_CALL) {
                Log.d("TAG", "onReceive: FCM MISSED_CALL")
                finishAndRemoveTask()
                moveTaskToBack(true)
            }
        }
    }
}

fun ImageView.loadImage(url: String?) {
    val option = RequestOptions()
        .error(R.drawable.ic_avatar_default)
    Glide.with(this.context)
        .setDefaultRequestOptions(option)
        .load(url)
        .into(this)
}

fun Context.cancelNotification(id: Int) {
    val notificationManager =
        this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(id)
}

@RequiresApi(Build.VERSION_CODES.O)
fun Activity.turnScreenOnAndKeyguardOff() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        setShowWhenLocked(true)
        setTurnScreenOn(true)
    } else {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        )
    }

    with(getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager) {
        requestDismissKeyguard(this@turnScreenOnAndKeyguardOff, null)
    }
}

fun Activity.turnScreenOffAndKeyguardOn() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        setShowWhenLocked(false)
        setTurnScreenOn(false)
    } else {
        window.clearFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )
    }
}