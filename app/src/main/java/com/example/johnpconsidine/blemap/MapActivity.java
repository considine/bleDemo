package com.example.johnpconsidine.blemap;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.johnpconsidine.transmit.Loc;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity {
    private static final String TAG = MapActivity.class.getSimpleName();
    private GoogleMap mMap;
    Button bleButton;
    Button networkButton;
    MyReceiver receiver;
    private MapView mMapView;

    /***** Ble Scan code for test only ******/
    /***************** start *********************/

    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothAdapter mBluetoothAdapter;

    private void startScan() {

        //the filter settings
        ScanFilter ResultsFilter = new ScanFilter.Builder()
                //.setDeviceAddress(string)
                //.setDeviceName(string)
                //.setManufacturerData()
                //.setServiceData()
                .setServiceUuid(LocRes.LOC_SERVICE)
                .build();

        ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();
        filters.add(ResultsFilter);

        //scan settings
        ScanSettings settings = new ScanSettings.Builder()
                //.setCallbackType() //int
                //.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE) //AGGRESSIVE, STICKY  //require API 23
                //.setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT) //ONE, FEW, MAX  //require API 23
                //.setReportDelay(0) //0: no delay; >0: queue up
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) //LOW_POWER, BALANCED, LOW_LATENCY
                .build();

        if(mBluetoothLeScanner != null) {
            mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
        }
    }

    private void stopScan() {
        mBluetoothLeScanner.stopScan(mScanCallback);
    }

    //Get the scan callback and process results
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(TAG, "onScanResult");
            processResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {  //should add AdRecord to process the list of results one by one
            Log.d(TAG, "onBatchScanResults: "+results.size()+" results");
            for (ScanResult result : results) {
                processResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.w(TAG, "LE Scan Failed: "+errorCode);
        }

        private void processResult(ScanResult result) {

            //Log.i(TAG, String.valueOf(System.currentTimeMillis())+result);
            LocRes locres = new LocRes(result.getScanRecord());
            Log.v(TAG,"lat is "+ locres.getLatitude()+" long is " + locres.getLongitude()); //The log info. here will show the returned lat and long !!!
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng((double) locres.getLatitude(), (double) locres.getLongitude()))
                    .title("no notes"));
            stopScan();
        }
    };
    /***** Ble Scan code for test only ******/
    /***************** end *********************/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //initialize braod cast receiver
        IntentFilter filter = new IntentFilter(MyReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new MyReceiver();
        registerReceiver(receiver, filter);

        /************* start ble scan, always on ****************/

        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        // it's also okay to put startScan here !!
        //startScan();
        /************** start ble scan, always on ***************/




        //initialize buttons
        bleButton = (Button) findViewById(R.id.bleButton);
        networkButton = (Button) findViewById(R.id.networkButton);

        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(
                R.id.mapview)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        //USe south bends coordinates since most of the pins are here
                        .target(new LatLng(41.6764, -86.2520))      // Sets the center of the map to location user
                        .zoom(10)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


            }
        });


        networkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MapActivity.this, TransmitIntentService.class);
                startService(intent);
                Toast.makeText(MapActivity.this, "Network pins received", Toast.LENGTH_SHORT).show();

            }
        });


        /**************************** the click button will trigger the start of ble Scan *************************************/
        bleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //call ble start advertising;
                // Loc loc = Application.getPlace(0);
                Log.v(TAG, "Scan starts here!");
                startScan();
                Log.v(TAG,"Scan already started!");
            }
        });


    }
    public class MyReceiver extends BroadcastReceiver {

        public static final String ACTION_RESP =
                "com.example.johnpconsidine.blemap.MESSAGE_PROCESSED";


        @Override
        public void onReceive(Context context, Intent intent) {
            for (Loc location : Application.getmPlaces()) {
                Log.v(TAG, "the lat is " + location.getLatitude());
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng((double) location.getLatitude(), (double) location.getLongitude()))
                        .title("no notes"));
                // Log.v(TAG, "The lat is " + location.getParseGeoPoint(Utils.PLACE_OBJECT_LOCATION).getLatitude());
            }

        }
    }

}
