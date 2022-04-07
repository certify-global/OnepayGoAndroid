package com.onepay.onepaygo.api.response

data class RefreshTokenResponse (val refresh_token : String, val access_token : String?, val error : String)