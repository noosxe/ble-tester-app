package com.noosxe.bleapp;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
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

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

/**
 * Created by noosxe on 2/15/17.
 */

public class DeviceActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private Menu mActionBarMenu;
    private String mAddress;
    private BluetoothDevice mDevice;
    private BluetoothGatt mBluetoothGatt;
    private ProgressDialog progress;

    private Handler mHandler;

    private boolean mConnected;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        mHandler = new Handler();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.action_bar_device);
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
                List<BluetoothGattService> services = gatt.getServices();

                Log.d(MainActivity.LOG_TAG, "services discovered");

                BluetoothGattService service = gatt.getService(UUID.fromString("12345678-1234-5678-1234-56789abcdef0"));

                if (service == null) {
                    Log.e(MainActivity.LOG_TAG, "service is null");
                    return;
                }

                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString("12345678-1234-5678-1234-56789abcdef1"));
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
}
