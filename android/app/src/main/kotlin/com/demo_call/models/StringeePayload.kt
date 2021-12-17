package com.demo_call.models

// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json            = Json(JsonConfiguration.Stable)
// val stringeePayload = json.parse(StringeePayload.serializer(), jsonString)
import com.google.gson.annotations.SerializedName

data class StringeePayload(
    val stringeePushNotification: Long,
    val data: Data,
    val type: String
)

data class Data(
    @SerializedName("callId")
    val callID: String,

    val serial: Long,
    val callStatus: String,
    val from: From,
    val to: From,

    @SerializedName("projectId")
    val projectID: Long
)

data class From(
    val number: String,
    val alias: String,

    @SerializedName("is_online")
    val isOnline: Boolean,

    val type: String
)

