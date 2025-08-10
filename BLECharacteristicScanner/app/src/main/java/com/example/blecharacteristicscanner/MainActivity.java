package com.example.blecharacteristicscanner;


import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/*
    TODO: Почини Список ХАРАКТЕРИСТИК пересозданием ServiceAdapter
    TODO: Нарисуй иконку. Допустим белый значок bluetooth на голубом фоне
*/

public class MainActivity extends AppCompatActivity
{
    TextView toolbarName;
    TextView toolbarMAC;
    ImageView toolbarBluetoothButton;

    RotateAnimation deviceSearchAnimator;
    ImageView deviceSearchAnimation;

    RecyclerView serviceList;
    ServiceListAdapter serviceAdapter;

    BluetoothDevice BLEDevice;
    private BluetoothGatt BLEGatt;

    final int image_load = R.drawable.load;
    final int image_error = R.drawable.error;




    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        toolbarMAC = findViewById(R.id.toolbar_mac);
        toolbarName = findViewById(R.id.toolbar_name);

        toolbarBluetoothButton = findViewById(R.id.toolbar_bluetooth_button);
        toolbarBluetoothButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(v.getContext(), SearchActivity.class);

                if (BLEGatt != null)
                {
                    try
                    {
                        BLEGatt.disconnect();
                        BLEGatt.close();
                        BLEGatt = null;
                    }
                    catch (SecurityException e)
                    {
                        // ошЫбка
                    }
                }

                activityResultLauncher.launch(intent);
            }
        });


        serviceList = findViewById(R.id.viewer_main);
        serviceList.setLayoutManager(new LinearLayoutManager(this));

        serviceAdapter = new ServiceListAdapter(this);
        serviceList.setAdapter(serviceAdapter);

        deviceSearchAnimation = findViewById(R.id.services_search_animation);
        deviceSearchAnimation.setVisibility(View.INVISIBLE);
    }

    // *******************************************************************************************************************
    //  Bluetooth GATT Callback
    // *******************************************************************************************************************
    final private  BluetoothGattCallback gattCallback = new BluetoothGattCallback()
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED)
            {
                try
                {
                    gatt.discoverServices();
                }
                catch (SecurityException e)
                {
                    // ошЫбка
                }
            }
            else if (status == 133)
            {
                try
                {
                    gatt.close();
                }
                catch (SecurityException e)
                {
                    // ошЫбка
                }

                showError();
            }
            else
            {
                try
                {
                    gatt.close();
                }
                catch (SecurityException e)
                {
                    // ошЫбка
                }

                showError();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                for (BluetoothGattService service : gatt.getServices())
                {
                    String serviceUUID = service.getUuid().toString();
                    List<String> characteristicUUIDs = new ArrayList<>();

                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics())
                    {
                        characteristicUUIDs.add(characteristic.getUuid().toString());
                    }

                    Service serviceBuffer = new Service(serviceUUID, characteristicUUIDs);


                    MainActivity.this.runOnUiThread(() ->
                    {
                        serviceAdapter.addItem(serviceBuffer);
                    });
                }

                if (BLEGatt != null)
                {
                    try
                    {
                        BLEGatt.disconnect();
                        BLEGatt.close();
                        BLEGatt = null;
                    }
                    catch (SecurityException e)
                    {
                        // ошЫбка
                    }
                }

            }

            stopAnimation();
        }
    };


    // *******************************************************************************************************************
    //  Результат выполнения SearchActivity
    // *******************************************************************************************************************
    final private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->
    {
        if (result.getResultCode() == RESULT_OK)
        {
            Intent data = result.getData();
            BluetoothDevice device = data != null ? data.getParcelableExtra("BLEDevice") : null;
            if (device != null)
            {
                BLEDevice = device;

                try
                {
                    startAnimation();

                    String MACBuffer = device.getAddress();
                    String NameBuffer = device.getName();
                    NameBuffer = NameBuffer == null ? "Unnamed Device" : NameBuffer;

                    toolbarName.setText(NameBuffer);
                    toolbarName.setTextSize((NameBuffer.length() >= 25) ? 21 : 25);

                    toolbarMAC.setText(MACBuffer);

                    serviceAdapter = new ServiceListAdapter(this);
                    serviceList.setAdapter(serviceAdapter);

                    BLEGatt = BLEDevice.connectGatt(MainActivity.this, false, gattCallback);
                }
                catch (SecurityException e)
                {
                    // ошЫбка
                }
            }
        }
    });


    // *******************************************************************************************************************
    //  Анимация загрузки
    // *******************************************************************************************************************
    void startAnimation()
    {
        deviceSearchAnimation.setImageResource(image_load);

         deviceSearchAnimator = new RotateAnimation(
                0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        deviceSearchAnimator.setDuration(1000);
        deviceSearchAnimator.setInterpolator(new LinearInterpolator());
        deviceSearchAnimator.setRepeatCount(Animation.INFINITE);

        deviceSearchAnimation.startAnimation(deviceSearchAnimator);
        deviceSearchAnimation.setVisibility(View.VISIBLE);
    }

    void stopAnimation()
    {
        deviceSearchAnimation.setVisibility(View.INVISIBLE);
        deviceSearchAnimation.clearAnimation();
    }

    void showError()
    {
        stopAnimation();

        deviceSearchAnimation.setRotation(0);
        deviceSearchAnimation.setImageResource(image_error);
        deviceSearchAnimation.setVisibility(View.VISIBLE);
    }
}