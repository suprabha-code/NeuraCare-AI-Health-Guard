package com.suprabha.neuracareapp;

import android.Manifest;
import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.*;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class DashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth auth;
    private DatabaseReference userRef;

    private TextView heartRateText, spo2Text, temperatureText, aiAdviceText, bleStatusText;
    private ProgressBar bleProgressBar;
    private Button btnReconnectBLE;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bleScanner;
    private BluetoothGatt bluetoothGatt;

    private final String ESP32_DEVICE_NAME = "NeuraCareESP32";
    private final UUID SERVICE_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    private final UUID CHARACTERISTIC_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSIONS = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        heartRateText = findViewById(R.id.heartRateText);
        spo2Text = findViewById(R.id.spo2Text);
        temperatureText = findViewById(R.id.temperatureText);
        aiAdviceText = findViewById(R.id.aiAdviceText);
        bleStatusText = findViewById(R.id.bleStatusText);
        bleProgressBar = findViewById(R.id.bleProgressBar);
        btnReconnectBLE = findViewById(R.id.btnReconnectBLE);

        btnReconnectBLE.setOnClickListener(v -> {
            bleStatusText.setText("BLE Status: Scanning...");
            bleProgressBar.setVisibility(View.VISIBLE);
            btnReconnectBLE.setVisibility(View.GONE);
            checkPermissionsAndScan();
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bleScanner = bluetoothAdapter.getBluetoothLeScanner();

        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            bleStatusText.setText("BLE Status: Scanning...");
            bleProgressBar.setVisibility(View.VISIBLE);
            checkPermissionsAndScan();
        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void checkPermissionsAndScan() {
        List<String> permissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.BLUETOOTH_SCAN);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), REQUEST_PERMISSIONS);
        } else {
            startBLEScan();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] perms, @NonNull int[] results) {
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean granted = true;
            for (int r : results) {
                if (r != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            if (granted) {
                startBLEScan();
            } else {
                bleStatusText.setText("BLE Status: Permission Denied");
                bleProgressBar.setVisibility(View.GONE);
                btnReconnectBLE.setVisibility(View.VISIBLE);
            }
        }
    }

    private void startBLEScan() {
        ScanCallback scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice device = result.getDevice();
                if (device.getName() != null && device.getName().equals(ESP32_DEVICE_NAME)) {
                    bleScanner.stopScan(this);
                    bluetoothGatt = device.connectGatt(DashboardActivity.this, false, gattCallback);
                }
            }
        };
        bleScanner.startScan(scanCallback);
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                runOnUiThread(() -> {
                    bleStatusText.setText("BLE Status: Connected");
                    bleProgressBar.setVisibility(View.GONE);
                });
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                runOnUiThread(() -> {
                    bleStatusText.setText("BLE Status: Disconnected");
                    bleProgressBar.setVisibility(View.GONE);
                    btnReconnectBLE.setVisibility(View.VISIBLE);
                });
            }
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BluetoothGattService service = gatt.getService(SERVICE_UUID);
            if (service != null) {
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_UUID);
                gatt.setCharacteristicNotification(characteristic, true);
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            String data = characteristic.getStringValue(0);
            runOnUiThread(() -> parseVitals(data));
        }
    };

    private void parseVitals(String data) {
        try {
            String[] parts = data.split("\\|");
            int hr = Integer.parseInt(parts[0].split(":")[1]);
            float temp = Float.parseFloat(parts[1].split(":")[1]);
            int spo2 = Integer.parseInt(parts[2].split(":")[1]);
            boolean alert = Boolean.parseBoolean(parts[3].split(":")[1]);

            heartRateText.setText("HR: " + hr);
            temperatureText.setText("Temp: " + temp + "°C");
            spo2Text.setText("SpO₂: " + spo2 + "%");

            aiAdviceText.setText(alert ? "Emergency detected!" : "Vitals are normal.");

            VitalsModel vitals = new VitalsModel(hr, temp, spo2, alert);
            userRef.child("vitals").setValue(vitals);
        } catch (Exception e) {
            Log.e("Vitals", "Parse error: " + data);
        }
    }
}