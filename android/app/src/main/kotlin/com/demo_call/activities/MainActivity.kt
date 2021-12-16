package com.demo_call.activities

import android.content.Context
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.annotation.NonNull
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.gson.Gson
import com.demo_call.CALL_CHANNEL_METHOD
import com.demo_call.CHANNEL_NAME
import com.demo_call.utils.Common
import com.studyguide.mightyid.models.LocalRequestCall
import com.studyguide.mightyid.models.RequestCall
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.StringCodec

const val CALL_CHANNEL_ID = "calls_channel_id"
const val CALL_CHANNEL_NAME = "Calls"

class MainActivity : FlutterActivity(), LifecycleObserver {

    private lateinit var tokenChannel: MethodChannel

    companion object {
        var messageChannel: BasicMessageChannel<String>? = null
        var callActionResult: MethodChannel.Result? = null
        var isBackground = true
        var callMethodChannel: MethodChannel? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        isBackground = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        isBackground = false
    }

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        messageChannel = BasicMessageChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL_NAME,
            StringCodec.INSTANCE
        )
        messageChannel!!.setMessageHandler { message, _ ->
            Log.d("MainActivity", "configureFlutterEngine: $message")
        }

        setupCallMethodChannel(flutterEngine)

        setupTokenChannel(flutterEngine)

    }

    private fun setupCallMethodChannel(flutterEngine: FlutterEngine) {
        callMethodChannel = MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CALL_CHANNEL_METHOD
        )
        callMethodChannel!!.setMethodCallHandler { call, result ->
            when (call.method) {
                "start_call" -> {
                    val payload = Gson().fromJson(
                        call.argument("payload") as String?,
                        LocalRequestCall::class.java
                    )
                    Log.d("MainActivity", "setupCallMethodChannel: payload: $payload")
                    callActionResult = result
                }
            }
        }
    }

    private fun setupTokenChannel(flutterEngine: FlutterEngine) {
        tokenChannel = MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            "com.studyguide.mightyid/token"
        )
        tokenChannel.setMethodCallHandler { call, result ->
            when (call.method) {
                "sendToken" -> {
                    Common.customerId = call.argument("customer_id")
                    Common.token = call.argument("token")
                    val dataSave = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                    val editor: SharedPreferences.Editor = dataSave.edit()
                    editor.putInt("customer_id", Common.customerId!!)
                    editor.putString("token", Common.token)
                    editor.apply()
                    result.success(null)
                }
            }
        }
    }
}


