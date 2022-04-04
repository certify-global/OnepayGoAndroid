package com.onepay.onepaygo.api.request

import java.io.Serializable

data class ApiKeyRequest (val terminalId : String, val gatewayId : String) : Serializable