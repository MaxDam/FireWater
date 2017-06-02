package com.max.ibeacon.firewater.common;

import android.bluetooth.BluetoothAdapter;

/**
 * Created by max on 22/02/15.
 */
public class Utils {

    //gestisce lo stato del bluetooth
    public static boolean setBluetooth(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (enable && !isEnabled) {
            return bluetoothAdapter.enable();
        }
        else if(!enable && isEnabled) {
            return bluetoothAdapter.disable();
        }
        return true;
    }
}
