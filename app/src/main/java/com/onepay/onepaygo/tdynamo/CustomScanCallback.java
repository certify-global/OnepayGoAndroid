package com.onepay.onepaygo.tdynamo;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;

import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

public class CustomScanCallback extends ScanCallback
    {
        @Override
        public void onBatchScanResults(List<ScanResult> results)
        {
            if (results != null)
            {
                ListIterator<ScanResult> resultsIt = results.listIterator();

                while (resultsIt.hasNext())
                {
                    ScanResult result = resultsIt.next();

                    processScanResult(result);
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode)
        {
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result)
        {
            processScanResult(result);
        }

        private void processScanResult(ScanResult result)
        {
            if (android.os.Build.VERSION.SDK_INT < 21)
            {
                return;
            }

            boolean found = false;

            if (result != null)
            {
                ScanRecord scanRecord = result.getScanRecord();
                final BluetoothDevice device = result.getDevice();

                if (scanRecord != null)
                {
                    List<UUID> uuidList = TDynamoUtils.getInstance().parseUUIDs(scanRecord.getBytes());

                    ListIterator<UUID> uuidListIt = uuidList.listIterator();

                    while (uuidListIt.hasNext())
                    {
                        UUID scanUuid = uuidListIt.next();

                        if (scanUuid.compareTo(TDynamoUtils.mServiceUuid) == 0)
                        {
                            found = true;
                        }
                    }
                }

                if (found && (device != null))
                {

                   TDynamoUtils.getInstance().addDevice(device);

                }

            }
        }
    };