package com.example.johnpconsidine.blemap;

import android.bluetooth.le.ScanRecord;
import android.os.ParcelUuid;


public class LocRes {

    private float latitude;
    private float longitude;

    /* Full Bluetooth UUID that defines the Location Service */
    public static final ParcelUuid LOC_SERVICE = ParcelUuid.fromString("00001809-0000-1000-8000-00805f9b34fb");

    public LocRes (ScanRecord loc_record){

        byte[] loc_data = loc_record.getServiceData(LOC_SERVICE);

        if (loc_data != null){
            latitude = parseData(loc_data, "Lat");
            longitude = parseData(loc_data, "Long");
        }
    }

    public float parseData(byte[] serviceData, String flag){
        float out = 0f;

        if (flag.equals("Lat")){
            out = ArrayToFloat(serviceData, 0);
        }else if(flag.equals("Long")){
            out = ArrayToFloat(serviceData, 4);
        }
        return out;
    }

    //transform byte[] array to float
    public float ArrayToFloat(byte[] Array,int Pos)
    {
        int accum = 0;
        accum = Array[Pos+0] & 0xFF;
        accum |= (long)(Array[Pos+1] & 0xFF)<<8;
        accum |= (long)(Array[Pos+2] & 0xFF)<<16;
        accum |= (long)(Array[Pos+3] & 0xFF)<<24;
        return Float.intBitsToFloat(accum);
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }


    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

}