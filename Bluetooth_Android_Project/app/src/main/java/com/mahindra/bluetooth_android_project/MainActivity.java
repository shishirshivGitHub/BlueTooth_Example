package com.mahindra.bluetooth_android_project;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    ListView bluetoothListView;
    List<String> bluetoothList;
    BluetoothAdapter mBluetoothAdapter;
    Button scanBluetoothDevice;
    Set<BluetoothDevice> pairedDevices;
    ArrayAdapter<String> bluetoothAdapterView;
    Map<String,String> strings;
    TextView pairedDevice;
    Set<BluetoothDevice> bluetoothDeviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothDeviceList = new HashSet<>();

        bluetoothListView = (ListView)findViewById(R.id.bluetoothListView);
        pairedDevice = (TextView) findViewById(R.id.pairedDevice);

        getPermissions();  // getting the required permission for Bluetooth

        setUpBluetooth();

        setScanBluetoothDevice();

        bluetoothListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String val =(String) adapterView.getItemAtPosition(i);
                List<BluetoothDevice> bluetoothDeviceArrayList = new ArrayList<BluetoothDevice>();
                bluetoothDeviceArrayList.addAll(bluetoothDeviceList);

                for(int p = 0;p<bluetoothDeviceArrayList.size();p++){
                    if(bluetoothDeviceArrayList.get(p).getName().equals(val)){
                        Log.e("check the name of the device here -->> ", bluetoothDeviceArrayList.get(p).getName() +  "  ");
                        pairDevice(bluetoothDeviceArrayList.get(p));
                        pairedDevice.setText(val);

                        IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                        registerReceiver(mPairReceiver, intent);
                        break;

                    }
                }
            }
        });

    }

    void setScanBluetoothDevice(){

        bluetoothList = new ArrayList<>();
        strings = new HashMap();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        mBluetoothAdapter.startDiscovery();

    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    //discovery starts, we can show progress dialog or perform other tasks
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    //discovery finishes, dismis progress dialog
                } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    //bluetooth device found
                    BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    bluetoothDeviceList.add(device);
                    String deviceName = device.getName();
                    String deviceAddress = device.getAddress();

                    strings.put(deviceAddress,deviceName);

                }

            String [] string = new String[strings.size()];
            int i = 0;
            for (Map.Entry<String, String> entry : strings.entrySet()) {

                if(TextUtils.isEmpty(entry.getValue())){
                    string[i] = entry.getKey();
                }else{
                    string[i] = entry.getValue();
                }

                i++;
            }

            bluetoothAdapterView = new ArrayAdapter<String>(MainActivity.this,R.layout.bluetooth_list,string);
            bluetoothListView.setAdapter(bluetoothAdapterView);

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state        = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState    = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    Toast.makeText(MainActivity.this,"Paired",Toast.LENGTH_LONG).show();
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    Toast.makeText(MainActivity.this,"Un Paired",Toast.LENGTH_LONG).show();
                }

            }
        }
    };

    void getPermissions(){

        ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        },MY_PERMISSIONS_REQUEST_READ_CONTACTS);
    }

    void setUpBluetooth(){

         mBluetoothAdapter.enable();
             if(getBlueToothOn()){
             pairedDevices = mBluetoothAdapter.getBondedDevices();
             if(pairedDevices.size() > 0){
                for(BluetoothDevice bluetoothDevice : pairedDevices){
                    String deviceName = bluetoothDevice.getName();
                    pairedDevice.setText(deviceName);
                }
             }
         }
             else {
                 getPermissions();
             }
     }

    private boolean getBlueToothOn(){
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

}
