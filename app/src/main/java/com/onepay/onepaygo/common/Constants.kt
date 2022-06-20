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
        const val logout = "LOGOUT"
        const val navUpdate = "navUpdate"
        const val refreshTokenGrantType = "refresh_token"
        const val helpUrl = "https://help.onepay.com/"
        const val supportUrl = "https://onepay.com/contact-support/"
        const val privacyUrl = "https://onepay.com/legal/"
        val deployment = ModeApp.Proved

    }

    enum class ModeApp {
        Demo, Proved
    }
    enum class marketCode {
        M, R, E
    }
    enum class DeviceType {
        MAGTEK, MIURA
    }
    enum class MethodType {
        CC, DB,EBT
    }

    enum class KeyBoard {
        AMOUNT, CARD,CVV,MMYY
    }
    enum class SearchType(val value: Int) {
        All(0),TransactionID(1), CustomerID(2),FirstName(3),LastName(4),Email(5),Phone(6),TransactionAmount(7),CardLast4Digits(8),SourceApplication(9)
    }
enum class SourceApplicationSearch{
    onepayGoApp,WOOCOMMERCE,PLE,Jmeter,PS,VT,EXE,paypage,Snap
}
    enum class Type(val value: Int) {
        AuthOnly(1),
        AuthandCapture(2),
        PriorAuthCapture(3),
        CaptureOnly(4),
        Void(5),
        PartialVoid(6),
        Refund(7),
        ForcedRefund(8),
        Verification(9),
        SignatureEmail(10),
        BalanceInquiry(11)
    }
    enum class Test(val value: Int){
        LiveZero(0),
        TestOne(1)
    }
    enum class ActionCode(val value: String){
        ActionCodeEmpty(""),
        ActionCode_1("1"),
        ActionCode_2("2"),
        ActionCode_3("3"),
        ActionCode_4("4")
    }

}