package com.noosxe.bleapp;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by noosxe on 2/16/17.
 */

public class ServiceActivity extends AppCompatActivity {

    private String mServiceUUID;
    private BluetoothGattService mService;
    private List<BluetoothGattCharacteristic> mChars = new ArrayList<>();
    private MyArrayAdapter myArrayAdapter;
    private ListView mCharsList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.action_bar_service);
        myToolbar.setTitle("Characteristics");
        setSupportActionBar(myToolbar);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mServiceUUID = bundle.getString("service");

        mService = DeviceActivity.mBluetoothGatt.getService(UUID.fromString(mServiceUUID));
        mChars = mService.getCharacteristics();

        myArrayAdapter = new MyArrayAdapter(this, android.R.layout.simple_list_item_1);
        mCharsList = (ListView) findViewById(R.id.char_list);
        mCharsList.setAdapter(myArrayAdapter);
    }

    private class MyArrayAdapter extends ArrayAdapter<String> {
        MyArrayAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public int getCount() {
            return mChars.size();
        }

        @Nullable
        @Override
        public String getItem(int position) {
            return mChars.get(position).getUuid().toString();
        }
    }
}
