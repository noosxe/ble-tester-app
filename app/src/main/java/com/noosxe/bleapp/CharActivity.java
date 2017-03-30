package com.noosxe.bleapp;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.UUID;

/**
 * Created by noosxe on 2/17/17.
 */

public class CharActivity extends AppCompatActivity {

    private String mServiceUUID;
    private BluetoothGattService mService;
    private String mCharacteristicUUID;
    private BluetoothGattCharacteristic mCharacteristic;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_char);

//        Toolbar myToolbar = (Toolbar) findViewById(R.id.action_bar_service);
//        myToolbar.setTitle("Characteristics");
//        setSupportActionBar(myToolbar);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mServiceUUID = bundle.getString("service");
        mCharacteristicUUID = bundle.getString("characteristic");

        mService = DeviceActivity.mBluetoothGatt.getService(UUID.fromString(mServiceUUID));
        mCharacteristic = mService.getCharacteristic(UUID.fromString(mCharacteristicUUID));

        DeviceActivity.mBluetoothGatt.readCharacteristic(mCharacteristic);

        byte[] value = mCharacteristic.getValue();

        Log.d(MainActivity.LOG_TAG, "value reading");

//        myArrayAdapter = new MyArrayAdapter(this, android.R.layout.simple_list_item_1);
//        mCharsList = (ListView) findViewById(R.id.char_list);
//        mCharsList.setAdapter(myArrayAdapter);
    }

}
