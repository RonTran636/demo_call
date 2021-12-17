package com.demo_call.models

import com.google.gson.annotations.SerializedName

data class RequestCall(
        @SerializedName("messageType")
        var messageType: String?=null,
        @SerializedName("call_id")
        var callId: String? = null,
        @SerializedName("callerCustomerId")
        var callerCustomerId: String? = null,
        @SerializedName("callerName")
        var callerName: String? = null,
        @SerializedName("callerEmail")
        var callerEmail: String? = null,
        @SerializedName("privacy_mode")
        var privacyMode: String? = null,
        @SerializedName("isPrivateCall")
        var isPrivateCall: Boolean? = null,
        @SerializedName("photo_url")
        var callerPhotoURL: String? = null,
        @SerializedName("server_meet")
        var serverMeet: String? = null,
        @SerializedName("meetingType")
        var meetingType: String? = null,
        @SerializedName("meetingId")
        var meetingId: String? = null,
        @SerializedName("topicId")
        var topicId: String? = null,
        //Request response model
        var response: String? = null,
        @SerializedName("jwt")
        var jwtToken: String?=null
)
