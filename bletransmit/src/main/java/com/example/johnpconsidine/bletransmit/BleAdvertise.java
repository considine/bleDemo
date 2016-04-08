package com.example.johnpconsidine.bletransmit;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

/**
 * Created by johnpconsidine on 4/8/16.
 */
public class BleAdvertise {


    public static final String TAG = BleAdvertise.class.getSimpleName();
    public static final ParcelUuid LOC_SERVICE = ParcelUuid.fromString("00001809-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    public void startAdvertising(byte[] data_out){ //the input is byte[] array, and any double should be transformed
        if (mBluetoothLeAdvertiser == null) return;

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY) //3 modes: LOW_POWER, BALANCED, LOW_LATENCY
                .setConnectable(false)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM) // ULTRA_LOW, LOW, MEDIUM, HIGH
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(true)
                .addServiceUuid(LOC_SERVICE)
                .addServiceData(LOC_SERVICE,data_out)
                .build();

        mBluetoothLeAdvertiser.startAdvertising(settings, data, mAdvertiseCallback);
    }

    public void stopAdvertising() {
        if (mBluetoothLeAdvertiser == null) return;

        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
    }

    public void restartAdvertising(byte[] data_out) {
        stopAdvertising();
        startAdvertising(data_out);
    }

    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.i(TAG, "LE Advertise Started.");
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.w(TAG, "LE Advertise Failed: "+errorCode);
        }
    };

    //When advertising loc data, transform float to byte[] array
    public static byte[] FloatToArray(float Value) {
        int accum = Float.floatToRawIntBits(Value);
        byte[] byteRet = new byte[4];
        byteRet[0] = (byte)(accum & 0xFF);
        byteRet[1] = (byte)((accum>>8) & 0xFF);
        byteRet[2] = (byte)((accum>>16) & 0xFF);
        byteRet[3] = (byte)((accum>>24) & 0xFF);
        return byteRet;
    }

    //concatenate two byte[] array of latitude and byte[] array of longitude.
    public static byte[] ArrayConcat(byte[] Lat, byte[] Long){
        byte[] ret = new byte[Lat.length + Long.length];

        System.arraycopy(Lat, 0, ret, 0, Lat.length);
        System.arraycopy(Long, 0, ret, Lat.length, Long.length);
        return ret;
    }






    public void initAdvertising(Context context) {

        /************* start ble scan, always on ****************/

        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();

        beginAdvertising();













    }
    public void beginAdvertising () {
        //call ble start advertising;
        // Loc loc = Application.getPlace(0);
        Log.v(TAG, "lat is " + "41.6764" + "long is "+ "-86.2520");
        byte[] Lat = FloatToArray(41.6764f);
        byte[] Long = FloatToArray(-86.2520f);
        byte[] Loc_Ad = ArrayConcat(Lat, Long);
        Log.v(TAG, "loc in byte " + Loc_Ad);

        restartAdvertising(Loc_Ad);
    }


}
