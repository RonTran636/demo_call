package com.demo_call.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.annotation.NonNull
import androidx.lifecycle.*
import com.demo_call.CALL_CHANNEL_METHOD
import com.demo_call.CHANNEL_NAME
import com.demo_call.utils.Common
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.stringee.StringeeClient
import com.stringee.call.StringeeCall
import com.stringee.call.StringeeCall2
import com.stringee.exception.StringeeError
import com.stringee.listener.StatusListener
import com.stringee.listener.StringeeConnectionListener
import com.studyguide.mightyid.models.LocalRequestCall
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.StringCodec
import org.json.JSONObject


const val CALL_CHANNEL_ID = "calls_channel_id"
const val CALL_CHANNEL_NAME = "Calls"

class MainActivity : FlutterActivity(), LifecycleObserver {

    companion object {
        var isBackground = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        initAndConnectStringee()
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
    }

    fun initAndConnectStringee(){
        val client = StringeeClient(this)
        client.setConnectionListener(object : StringeeConnectionListener{
            override fun onConnectionConnected(p0: StringeeClient?, p1: Boolean) {
            }

            override fun onConnectionDisconnected(p0: StringeeClient?, p1: Boolean) {
            }

            override fun onIncomingCall(p0: StringeeCall?) {
                Log.d("TAG", "onIncomingCall: stringee data: $p0")
            }

            override fun onIncomingCall2(p0: StringeeCall2?) {
            }

            override fun onConnectionError(p0: StringeeClient?, p1: StringeeError?) {
            }

            override fun onRequestNewToken(p0: StringeeClient?) {
            }

            override fun onCustomMessage(p0: String?, p1: JSONObject?) {
            }

            override fun onTopicMessage(p0: String?, p1: JSONObject?) {
            }
        })
    }
}


