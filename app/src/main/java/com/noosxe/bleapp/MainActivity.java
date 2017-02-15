package com.noosxe.bleapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.LinkedHashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public final static String LOG_TAG = "BLE_APP";

    private static final int REQUEST_ENABLE_BT = 10;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 20;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private boolean mScanning;

    private Menu mActionBarMenu;
    private ListView mDeviceList;
    private MyArrayAdapter myArrayAdapter;

    private Map<String, BluetoothDevice> devices = new LinkedHashMap<>();

    private static final long SCAN_PERIOD = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(myToolbar);

        myArrayAdapter = new MyArrayAdapter(this, android.R.layout.simple_list_item_1);

        mDeviceList = (ListView) findViewById(R.id.device_list);

        mDeviceList.setAdapter(myArrayAdapter);
        mDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String deviceAddress = myArrayAdapter.getItem(position);

                Intent intent = new Intent(MainActivity.this, DeviceActivity.class);
                intent.putExtra("address", deviceAddress);
                startActivity(intent);
            }
        });

        mHandler = new Handler();

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (!isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

            return;
        }

        checkLocationPermission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (REQUEST_ENABLE_BT == requestCode) {
            if (!isBluetoothEnabled()){
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                checkLocationPermission();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mActionBarMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan: {
                scaneLeDevice(true);
                return true;
            }
            case R.id.action_cancel_scan: {
                scaneLeDevice(false);
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(LOG_TAG, "found device - " + device.getAddress() + ", RSSI " + rssi);

                            devices.put(device.getAddress(), device);
                            myArrayAdapter.notifyDataSetChanged();


                        }
                    });
                }
            };

    private void checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can perform BLE scanning");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @SuppressLint("NewApi")
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });

                builder.show();
            }
        }
    }

    private boolean isBluetoothEnabled() {
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    private void scaneLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

                    adjustMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            devices.clear();
            myArrayAdapter.notifyDataSetChanged();
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }

        adjustMenu();
    }

    private void adjustMenu() {
        MenuItem startScan = mActionBarMenu.findItem(R.id.action_scan);
        MenuItem stopScan = mActionBarMenu.findItem(R.id.action_cancel_scan);

        if (mScanning) {
            startScan.setVisible(false);
            stopScan.setVisible(true);
        } else {
            startScan.setVisible(true);
            stopScan.setVisible(false);
        }
    }

    private class MyArrayAdapter extends ArrayAdapter<String> {
        MyArrayAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Nullable
        @Override
        public String getItem(int position) {
            BluetoothDevice devicesa[] = new BluetoothDevice[0];
            devicesa = devices.values().toArray(devicesa);
            BluetoothDevice device = devicesa[position];

            return device.getAddress();
        }
    }
}
