package com.onepay.onepaygo.api.request

import java.io.Serializable

data class LoginRequest (val username : String, val password : String,val grant_type:String) : Serializable