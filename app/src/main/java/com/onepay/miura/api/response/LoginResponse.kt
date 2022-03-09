package com.onepay.miura.api.response

class LoginResponse (val userName : String, val emailConfirmed : String, val GatewayId : String?,
                     val UserId : String, val access_token : String?,val token_type : String, val refresh_token : String, val UserType : String?,
                     val terminalId : String, val authxUserId : String?, val Response : String?, val email : String?
)