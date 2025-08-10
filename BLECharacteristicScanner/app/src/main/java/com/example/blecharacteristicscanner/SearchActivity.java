package com.example.blecharacteristicscanner;

import android.Manifest;
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
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import android.content.Intent;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SearchActivity extends AppCompatActivity
{
    ImageView toolbarBackButton;

    RecyclerView deviceListView;
    DeviceListAdapter deviceListAdapter;


    private List<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.search), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        toolbarBackButton = findViewById(R.id.toolbar_back_button);
        toolbarBackButton.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        stopScanning();
                        finish();
                    }
                }
        );

        deviceListView = findViewById(R.id.viewer_search);
        deviceListView.setLayoutManager(new LinearLayoutManager(this));

        deviceListAdapter = new DeviceListAdapter(this);
        deviceListAdapter.setOnItemClickListener(new DeviceListAdapter.OnItemClickListener()
        {
            @Override
            public void onItemClick(int position)
            {
                if (position < bluetoothDevices.size())
                {
                    stopScanning();

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("BLEDevice", bluetoothDevices.get(position));
                    setResult(RESULT_OK, resultIntent);

                    finish();
                }
            }
        });

        deviceListView.setAdapter(deviceListAdapter);


        checkBLEPermissions();
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled())
        {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            startScanning();
        }
    }



    public void startScanning()
    {
        try
        {
            bluetoothLeScanner.startScan(leScanCallback);
        }
        catch (SecurityException e)
        {
            // ошЫбка
        }
    }

    public void stopScanning()
    {
        try
        {
            bluetoothLeScanner.stopScan(leScanCallback);
        }
        catch (SecurityException e)
        {
            // ошЫбка
        }
    }


    // *******************************************************************************************************************
    //  Permissions
    // *******************************************************************************************************************
    private void checkBLEPermissions()
    {
        String[] permissions = { Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION };

        for (String permission : permissions)
        {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) // Запрашиваем по одному разрешению
                ActivityCompat.requestPermissions(this, new String[]{permission}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted)
                startScanning();
            else
            {
                // ошЫбка
            }
        }
    }



    // *******************************************************************************************************************
    //  Scan Callback
    // *******************************************************************************************************************
    private final Set<String> deviceAddresses = new HashSet<>();

    private final ScanCallback leScanCallback = new ScanCallback()
    {
        @Override
        public void onScanResult(int callbackType, ScanResult result)
        {
            super.onScanResult(callbackType, result);
            if (result != null && result.getDevice() != null)
            {
                try
                {
                    String name = getTrueBLEDeviceName(result);
                    String address = result.getDevice().getAddress();
                    int strength = result.getRssi();

                    Device newDevice = new Device(name, address, strength);

                    if (deviceAddresses.add(result.getDevice().getAddress())) // true если такого устройства ещё нет
                    {
                        deviceListAdapter.addItem(newDevice);
                        deviceListView.scrollToPosition(deviceListAdapter.getItemCount() - 1);

                        bluetoothDevices.add(result.getDevice());
                    }
                }
                catch (SecurityException e)
                {
                    // ошЫбка
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };



    // *******************************************************************************************************************
    //  Bluetooth GATT Callback
    // *******************************************************************************************************************
//    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
//        @Override
//        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
//        {
//            if (newState == BluetoothProfile.STATE_CONNECTED)
//            {
//                try
//                {
//                    gatt.discoverServices(); // Отправляем запрос на поиск служб
//                }
//                catch (SecurityException e)
//                {
//                    // ошЫбка
//                }
//            }
//        }

//        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, int status)
//        {
//            if (status == BluetoothGatt.GATT_SUCCESS)
//            {
//                BluetoothGattService genericAccessService = gatt.getService(UUID.fromString(UUID_SERVICE_GENERIC));
//                if (genericAccessService != null)
//                {
//                    BluetoothGattCharacteristic nameCharacteristic = genericAccessService.getCharacteristic(UUID.fromString(UUID_CHARACTERISTIC_NAME));
//                    if (nameCharacteristic != null)
//                    {
//                        try
//                        {
//                            gatt.readCharacteristic(nameCharacteristic);
//                        }
//                        catch (SecurityException e)
//                        {
//                            // ошЫбка
//                        }
//                    }
//                }
//            }
//        }

//        @Override
//        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
//        {
//            if (status == BluetoothGatt.GATT_SUCCESS)
//            {
//                if (characteristic.getUuid().equals(UUID.fromString(UUID_CHARACTERISTIC_NAME)))
//                {
//                    String deviceName = characteristic.getStringValue(0);
//                    String address = gatt.getDevice().getAddress();
//                    int id = deviceListAdapter.getItemIdByDeviceAddress(address);
//
//                    deviceListAdapter.setItemName(id, deviceName);
//                }
//            }
//        }
//    };

    String getTrueBLEDeviceName(ScanResult result)
    {
        String deviceName = "";
        boolean nameIsFind = false;

        if (result.getScanRecord() != null)
        {
            nameIsFind = true;
            deviceName = result.getScanRecord().getDeviceName();
        }

        if (deviceName == null || deviceName.isEmpty())
        {
            nameIsFind = true;
            try
            {
                deviceName = result.getDevice().getName();
            }
            catch (SecurityException e)
            {
                // ошЫбка
            }
        }

        if (deviceName == null || deviceName.isEmpty())
            deviceName = "Unnamed Device";

        return deviceName;
    }
}