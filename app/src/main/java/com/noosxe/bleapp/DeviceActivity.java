package com.noosxe.bleapp;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by noosxe on 2/15/17.
 */

public class DeviceActivity extends AppCompatActivity {

    public static BluetoothGatt mBluetoothGatt;

    private BluetoothAdapter mBluetoothAdapter;
    private Menu mActionBarMenu;
    private String mAddress;
    private BluetoothDevice mDevice;
    private ProgressDialog progress;
    private ListView mServicesList;
    private Handler mHandler;
    private List<BluetoothGattService> mServices = new ArrayList<>();

    private MyArrayAdapter myArrayAdapter;

    private boolean mConnected;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        mHandler = new Handler();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.action_bar_device);

        myToolbar.setTitle("Device");

        setSupportActionBar(myToolbar);

        Intent intent = getIntent();

        Bundle bundle = intent.getExtras();
        mAddress = bundle.getString("address");

        myToolbar.setSubtitle(mAddress);

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mDevice = mBluetoothAdapter.getRemoteDevice(mAddress);

        progress = new ProgressDialog(this);
        progress.setTitle("Connecting");
        progress.setMessage("Connecting to device...");
        progress.setCancelable(false);

        myArrayAdapter = new MyArrayAdapter(this, android.R.layout.simple_list_item_1);
        mServicesList = (ListView) findViewById(R.id.services_list);
        mServicesList.setAdapter(myArrayAdapter);
        mServicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothGattService service = mServices.get(position);

                Intent intent = new Intent(DeviceActivity.this, ServiceActivity.class);
                intent.putExtra("service", service.getUuid().toString());
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mActionBarMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.device_bar_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_connect: {
                connect();
                return true;
            }
            case R.id.action_disconnect: {
                disconnect();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(MainActivity.LOG_TAG, "connected to device");

                mConnected = true;
                mServices.clear();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        myArrayAdapter.notifyDataSetChanged();
                    }
                });

                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(MainActivity.LOG_TAG, "disconnected from device");

                mConnected = false;
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    adjustMenu();
                }
            });
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                mServices = gatt.getServices();

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        myArrayAdapter.notifyDataSetChanged();
                    }
                });

                Log.d(MainActivity.LOG_TAG, "services discovered");
            } else {
                Log.w(MainActivity.LOG_TAG, "onServicesDiscovered received: " + status);
            }
        }
    };

    private void connect() {
        showLoadingIndicator();
        mBluetoothGatt = mDevice.connectGatt(this, false, mGattCallback);
    }

    private void disconnect() {
        mBluetoothGatt.disconnect();
    }

    private void showLoadingIndicator() {
        progress.show();
    }

    private void hideLoadingIndicator() {
        progress.dismiss();
    }

    private void adjustMenu() {
        MenuItem connectButton = mActionBarMenu.findItem(R.id.action_connect);
        MenuItem disconnectButton = mActionBarMenu.findItem(R.id.action_disconnect);

        if (mConnected) {
            connectButton.setVisible(false);
            disconnectButton.setVisible(true);
        } else {
            connectButton.setVisible(true);
            disconnectButton.setVisible(false);
        }

        hideLoadingIndicator();
    }

    private class MyArrayAdapter extends ArrayAdapter<String> {
        MyArrayAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public int getCount() {
            return mServices.size();
        }

        @Nullable
        @Override
        public String getItem(int position) {
            return mServices.get(position).getUuid().toString();
        }
    }
}
