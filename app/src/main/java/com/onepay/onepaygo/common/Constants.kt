package com.onepay.onepaygo.common

class Constants {

    companion object{
        const val moto = "MOTO"
        const val retail = "RETAIL"
        const val ecomm = "ECOMM"
        const val referrerUrl = "onepay.com"
        const val user = "USER"
        const val source = "SOURCE"
        const val onepayGoApp = "onepayGoApp"
        const val location = "Location"
        const val signature = "SIGNATURE"
    }
    enum class marketCode {
        M, R, E
    }
    enum class DeviceType {
        MAGTEK, MIURA
    }
}