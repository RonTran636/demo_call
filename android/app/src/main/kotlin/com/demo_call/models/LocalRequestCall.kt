package com.studyguide.mightyid.models

import com.google.gson.annotations.SerializedName

data class LocalRequestCall(
    @SerializedName("call_id")
    var callId: String? = null,
    @SerializedName("meeting_id")
    var meetingId: String? = null,
    @SerializedName("is_private_call")
    var isPrivateCall: Boolean? = null,
    @SerializedName("server_meet")
    var serverMeet: String? = null,
    @SerializedName("meeting_type")
    var meetingType : String?=null,
    @SerializedName("topic_id")
    var topicId: String?=null,
    @SerializedName("privacy_mode")
    var privacyMode: String? = null,
    @SerializedName("jwt")
    var jwtToken: String?=null
)