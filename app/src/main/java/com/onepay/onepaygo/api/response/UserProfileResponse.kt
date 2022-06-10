package com.onepay.onepaygo.api.response

data class UserProfileResponse (val userName : String, val first_name : String, val email : String?,
                                val last_name : String?, val phone_number : String, val roleId : Int, val roles : ArrayList<Roles>)
data class Roles(val Id: Int,val Name  :String,val GatewayId : String)