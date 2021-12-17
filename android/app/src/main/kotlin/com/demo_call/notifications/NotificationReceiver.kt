package com.demo_call.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.demo_call.*
import com.demo_call.activities.cancelNotification
import com.demo_call.utils.IntentUtils.getInfoExtra
import com.demo_call.models.RequestCall
import com.demo_call.models.StringeePayload
import com.stringee.StringeeClient
import com.stringee.call.StringeeCall
import com.stringee.call.StringeeCall2


class NotificationReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {
        val payload = intent.getInfoExtra<StringeePayload>(CALL_PAYLOAD)
        val callId = intent.getStringExtra(CALL_ID)
        val client = StringeeClient(context)
        val callerUserId = payload.data.from.alias
        val calleeUserId = payload.data.to.alias
        val stringeeCall = StringeeCall(client, calleeUserId, callerUserId)
        when (intent.action) {
            CALL_STATE_ACCEPT, ACTION_CALL_ACCEPT -> {
                Log.d("NotificationReceiver", "onReceive: CALL_STATE_ACCEPT, payload: $payload")
                context.cancelNotification(callId.hashCode())
                // Handle connect call in Android's side
                print(stringeeCall.callId)

                stringeeCall.answer()
            }

            CALL_STATE_REJECT, ACTION_CALL_REJECT -> {
                Log.d("NotificationReceiver", "onReceive: CALL_STATE_REJECT, payload: $payload")
                context.cancelNotification(callId.hashCode())
                // Handle reject call
                stringeeCall.reject()
            }
        }
    }

//    private fun sendResponseRequestCall(callId: Int, topicId: String, response: String) {
//        val myService = ServiceCentral()
//        val disposable = CompositeDisposable()
//        disposable.add(
//            myService.sendResponseRequestCall(callId, response, topicId)
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeWith(object : DisposableSingleObserver<JsonObject>() {
//                    //                    override fun onComplete() {
////                        Timber.tag("sendResponseRequestCall").d("$response :onComplete")
////                    }
//                    override fun onSuccess(t: JsonObject?) {
//                        Log.d("TAG", "onSuccess: data: $t")
//                    }
//
//                    override fun onError(error: Throwable) {
//                        Timber.tag("sendResponseRequestCall").e(error)
//                    }
//                })
//        )
//    }

//    private fun startJitsiMeeting(context: Context, dataReceive: RequestCall) {
//        JitsiMeetUtils.establishConnection(dataReceive.serverMeet!!)
//        val option = JitsiMeetUtils.configurationMeeting(dataReceive)
//        JitsiMeetUtils.launch(context, option,true, dataReceive)
//    }

//    private fun retrieveCurrentAccountInfo(context: Context) {
//        if (Common.token.isNullOrEmpty()){
//            val dataSave = context.getSharedPreferences("PREFERENCE", AppCompatActivity.MODE_PRIVATE)
//            val customerId = dataSave.getInt("customer_id", -1)
//            val token = dataSave.getString("token","")
//            Common.customerId = customerId
//            Common.token = token
//            Log.d("TAG", "retrieveCurrentAccountInfo: json: $customerId, $token")
//        }
//    }
}