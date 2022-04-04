package com.onepay.onepaygo.tdynamo

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Build
import java.util.*

class CustomScanCallback : ScanCallback() {
    override fun onBatchScanResults(results: List<ScanResult>) {
        val resultsIt = results.listIterator()
        while (resultsIt.hasNext()) {
            val result = resultsIt.next()
            processScanResult(result)
        }
    }

    override fun onScanFailed(errorCode: Int) {}
    override fun onScanResult(callbackType: Int, result: ScanResult) {
        processScanResult(result)
    }

    private fun processScanResult(result: ScanResult?) {
        if (Build.VERSION.SDK_INT < 21) {
            return
        }
        var found = false
        if (result != null) {
            val scanRecord = result.scanRecord
            val device = result.device
            if (scanRecord != null) {
                val uuidList = TDynamoUtils.getInstance().parseUUIDs(scanRecord.bytes)
                val uuidListIt: ListIterator<UUID> = uuidList.listIterator()
                while (uuidListIt.hasNext()) {
                    val scanUuid = uuidListIt.next()
                    if (scanUuid.compareTo(TDynamoUtils.mServiceUuid) == 0) {
                        found = true
                    }
                }
            }
            if (found && device != null) {
                TDynamoUtils.getInstance().addDevice(device)
            }
        }
    }
}