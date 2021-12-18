package com.demo_call.utils

import androidx.lifecycle.MutableLiveData
import com.stringee.call.StringeeCall

object Common {
    var customerId: Int?=null
    var token = MutableLiveData<String>()
    var maps : HashMap<String, StringeeCall> = HashMap()
}