package com.onepay.onepaygo.data

import com.onepay.onepaygo.api.response.TerminalResponse

object TerminalDataSource {

    private var terminalList = arrayListOf<TerminalResponse>()

    fun addTerminalList(dataList: List<TerminalResponse>) {
        terminalList.clear()
        terminalList = dataList.filter {
            it.Active == "true"
        } as ArrayList<TerminalResponse>
    }


    fun getTerminalList() = terminalList

}