package com.demo_call.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.NonNull
import androidx.lifecycle.*
import com.demo_call.utils.Common
import com.stringee.StringeeClient
import com.stringee.call.StringeeCall
import com.stringee.call.StringeeCall2
import com.stringee.exception.StringeeError
import com.stringee.listener.StringeeConnectionListener
import io.flutter.FlutterInjector
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import org.json.JSONObject


const val CALL_CHANNEL_ID = "calls_channel_id"
const val CALL_CHANNEL_NAME = "Calls"

class MainActivity : FlutterActivity(), LifecycleObserver {

    private lateinit var tokenChannel: MethodChannel

    companion object {
        var isBackground = true
        var navigateKey = MutableLiveData<String>()
        lateinit var channel: MethodChannel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        navigateKey.observe(this){
            Log.d("TAG", "onCreate: navigate key in main :${navigateKey.value}")
            if (it == "navigateToCall"){
                FlutterInjector.instance().flutterLoader().startInitialization(this)
                FlutterInjector.instance().flutterLoader().ensureInitializationCompleteAsync(this,null,
                    Handler(Looper.getMainLooper())
                ){
                    channel = MethodChannel(flutterEngine!!.dartExecutor.binaryMessenger,"navigateMethod")
                    channel.invokeMethod("navigateToCall",null)
                }
            }
        }
        Common.token.observe(this){
            initAndConnectStringee(it,null)
        }
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
        setupTokenChannel(flutterEngine)
    }

    fun initAndConnectStringee(token: String, client: StringeeClient?){
        val mClient = client ?: StringeeClient(this)
        mClient.connect(token)
        mClient.setConnectionListener(object : StringeeConnectionListener{
            override fun onConnectionConnected(p0: StringeeClient?, p1: Boolean) {
            }

            override fun onConnectionDisconnected(p0: StringeeClient?, p1: Boolean) {
            }

            override fun onIncomingCall(stringeeCall: StringeeCall?) {
                Log.d("TAG", "onIncomingCall: stringee data: $stringeeCall")
                Common.maps[stringeeCall!!.callId] = stringeeCall
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

    private fun setupTokenChannel(flutterEngine: FlutterEngine) {
        tokenChannel = MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            "com.studyguide.mightyid/token"
        )
        tokenChannel.setMethodCallHandler { call, result ->
            when (call.method) {
                "sendToken" -> {
                    Common.token.value = call.argument("token")
                    val dataSave = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                    val editor: SharedPreferences.Editor = dataSave.edit()
                    editor.putString("token", Common.token.value)
                    editor.apply()
                    result.success(null)
                }
            }
        }
    }
}


