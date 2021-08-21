package com.zero.library;

import androidx.appcompat.app.AppCompatActivity;

import com.zero.support.BluetoothService;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothActivity extends AppCompatActivity {

    BluetoothService bluetoothService;
    Intent service;
    ServiceConnection connection;

    TextView receivedData;
    EditText sendData;
    Button send;
    Button serviceButton;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        receivedData = findViewById(R.id.textViewReceived);
        sendData = findViewById(R.id.editTextSend);
        send = findViewById(R.id.buttonSend);
        serviceButton = findViewById(R.id.buttonService);
        listView = findViewById(R.id.listView);
        service = new Intent(this, BluetoothService.class);
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder binder) {
                bluetoothService = ((BluetoothService.BlueBinder)binder).getService();
                bluetoothService.setItemViewGroup((position, list) -> {
                    View root = LayoutInflater.from(getApplicationContext()).inflate(R.layout.support_item_view, null);
                    TextView name = root.findViewById(R.id.deviceName);
                    TextView address = root.findViewById(R.id.deviceAddress);
                    name.setText(list.get(position).getName());
                    address.setText(list.get(position).getAddress());
                    root.setOnClickListener((view) -> bluetoothService.connect(list.get(position)));
                    return root;
                });
                listView.setAdapter(bluetoothService.getDefaultAdapter());
                bluetoothService.setStatusListener(new BluetoothService.StatusListener() {
                    @Override
                    public void onConnectionError(String msg) {
                        receivedData.setText(msg);
                    }

                    @Override
                    public void onConnecting(BluetoothDevice device) {
                        Toast.makeText(getApplicationContext(), "Connecting...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onConnected() {
                        send.setActivated(true);
                        send.setOnClickListener((v) -> bluetoothService.sendString(sendData.getText().toString()));
                    }

                    @Override
                    public void onDisconnected() {
                        send.setActivated(false);
                        send.setOnClickListener(null);
                    }

                    @Override
                    public void onError(String message) {
                        receivedData.setText(message);
                    }

                    @Override
                    public void onData(String data) {
                        receivedData.append(data);
                    }
                });
                setStarted();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {}
        };

        if (BluetoothService.isStopped(this)) {
            setStopped();
        } else {
            bindService(service, connection, BIND_IMPORTANT);
        }
    }

    @Override
    protected void onPause() {
        unbindService(connection);
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (BluetoothService.isStopped(this)) {
            setStopped();
        } else {
            bindService(service, connection, BIND_IMPORTANT);
        }
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            startService(service);
            bindService(service, connection, BIND_IMPORTANT);
        } else {
            Toast.makeText(this, "Bluetooth isn't on!", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("SetTextI18n")
    private void setStopped() {
        send.setActivated(false);
        serviceButton.setText("Start");
        serviceButton.setOnClickListener((v) -> {
            if (BluetoothAdapter.getDefaultAdapter() == null) {
                Toast.makeText(this, "Bluetooth doesn't supported!", Toast.LENGTH_SHORT).show();
            } else if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                //noinspection deprecation
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1);
            } else {
                startService(service);
                bindService(service, connection, BIND_IMPORTANT);
            }
        });
    }
    @SuppressLint("SetTextI18n")
    private void setStarted() {
        send.setActivated(bluetoothService.isConnected());
        serviceButton.setText("Stop");
        serviceButton.setOnClickListener((v) -> {
            unbindService(connection);
            startService(service);
        });
    }
}