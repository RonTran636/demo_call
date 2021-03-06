package com.demo_call.utils

import android.content.Intent
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.json.JSONObject

object IntentUtils {
    inline fun <reified T> retrieveDataFromFcm(message: RemoteMessage): T {
        val params = message.data as Map<String, Any>
        return Gson().fromJson(params.toString(), T::class.java)
    }

    fun<T> Intent.putInfoExtra(name:String, value:T){
        this.putExtra(name, Gson().toJson(value))
    }

    inline fun <reified T> Intent.getInfoExtra(name:String):T {
        return Gson().fromJson(this.getStringExtra(name),T::class.java)
    }
}