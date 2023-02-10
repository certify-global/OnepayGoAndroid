package com.onepay.onepaygo.data

import com.onepay.onepaygo.api.response.TerminalResponse

object TerminalDataSource {

    private var terminalList = arrayListOf<TerminalResponse>()

    fun addTerminalList(dataList: List<TerminalResponse>) {
        terminalList.clear()
        terminalList = dataList.filter {
            it.Active == "true" && !it.TerminalType.equals("ECOMM")
        } as ArrayList<TerminalResponse>
    }


    fun getTerminalList() = terminalList

    fun getTerminalByID(terminalId: Int): String {
        try {
            val terminalResponse = terminalList.filter { it.Id == terminalId }
            return terminalResponse.get(0).TerminalName
        } catch (e: Exception) {
            return ""
        }
        return ""
    }

}