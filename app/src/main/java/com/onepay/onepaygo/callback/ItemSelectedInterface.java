package com.onepay.onepaygo.callback;

import android.bluetooth.BluetoothDevice;

public interface ItemSelectedInterface {
    void onItemSelected(int pos, BluetoothDevice bluetoothDevice);
}