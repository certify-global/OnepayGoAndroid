package com.onepay.miura.data

import com.onepay.miura.api.response.TerminalResponse

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