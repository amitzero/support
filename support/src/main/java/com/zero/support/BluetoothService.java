package com.zero.support;

import android.app.ActivityManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothService extends Service
{
	private static final boolean DEBUG = false;

	public final String DEFAULT = "com.zero.print";
	private SharedPreferences sharedPreferences;
	private String defaultDevice;

	private BluetoothAdapter bluetoothAdapter;
	private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private BluetoothSocket btSocket = null;
	private boolean keep_reading = false;

	private ArrayList<BluetoothDevice> deviceList;
	private Adapter adapter = null;
	private ViewSetup viewSetup;
	private StatusListener statusListener;

	private final BlueBinder binder = new BlueBinder();
	private final BlueBroadcastReceiver broadcastReceiver = new BlueBroadcastReceiver();

	public String getMe() {
		return "Hello this is tester!";
	}

	@Override
	public IBinder onBind(Intent intent) {
		printMsg("Bind");
		return binder;
	}

	@Override
	public void onRebind(Intent intent) {
		printMsg("Rebind");
	}

	@Override
	public boolean onUnbind(Intent intent) {
		printMsg("Unbind");
		return true;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		printMsg("StartCommand");
		sharedPreferences = getSharedPreferences(DEFAULT, MODE_MULTI_PROCESS);
		if (sharedPreferences.contains(DEFAULT)) {
			defaultDevice = sharedPreferences.getString(DEFAULT, "");
		}
		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		printMsg("Destroy");
		if (isConnected()) {
			disconnect();
		}
		super.onDestroy();
	}


	public void setStatusListener(StatusListener statusListener) {
		this.statusListener = statusListener;
	}

	public void setItemViewGroup(ViewSetup viewGroup) {
		viewSetup = viewGroup;
	}

	public Adapter getDefaultAdapter() {
		return adapter;
	}

	public void init() {
		if(BluetoothAdapter.getDefaultAdapter() == null) {
			Toast.makeText(this, "This device doesn't support bluetooth!", Toast.LENGTH_SHORT).show();
		} else if(!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
			Toast.makeText(this, "Turn on bluetooth first!", Toast.LENGTH_SHORT).show();
		} else {
			bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			adapter = new Adapter();

			if (defaultDevice != null && !defaultDevice.isEmpty()) {
				for (BluetoothDevice device : deviceList) {
					if (device.getAddress().equals(defaultDevice)) {
						connect(device);
						break;
					}
				}
			}
		}
	}

	public void connect(BluetoothDevice device) {
		sharedPreferences.edit().putString(DEFAULT, device.getAddress()).apply();
		if (statusListener != null) statusListener.onConnecting(device);
		new Thread() {
			@Override
			public void run() {
				try
				{
					if (BluetoothAdapter.getDefaultAdapter().isDiscovering())
					{
						BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
					}
					btSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
					btSocket.connect();
				}
				catch (IOException e)
				{
					if (statusListener != null) statusListener.onConnectionError("Socket connection error"+e);
				}

				new Thread()
				{
					@Override
					public void run()
					{
						while (true)
						{
							if (btSocket != null && btSocket.isConnected())
							{
								if (statusListener != null) statusListener.onConnected();
								keep_reading = true;
								listen();
								break;
							}
						}
					}
				}.start();
			}
		}.start();
	}

	public void send(byte[] data) {
		if (btSocket != null) {
			try {
				if(btSocket.isConnected()) {
					btSocket.getOutputStream().write(data);
					btSocket.getOutputStream().flush();
				} else if (statusListener != null) statusListener.onError("Bluetooth Socket is disconnected");
			}
			catch (IOException exception) {
				try {
					btSocket.close();

				} catch (IOException e) {
					if (statusListener != null) statusListener.onError("Get OutputStream error on closing" + e);
				}
				try {
					btSocket.connect();
					btSocket.getOutputStream().write(data);
					btSocket.getOutputStream().flush();
				} catch (IOException e) {
					if (statusListener != null) statusListener.onError("Get OutputStream error on reconnecting" + e);
				}
			}
		}
	}

	private void listen() {
		new Thread(() -> {
			byte[] dataBuffer = new byte[1024];
			while (keep_reading) {
				if (btSocket != null) {
					try {
						if (btSocket.getInputStream().available() != 0) {
							//noinspection ResultOfMethodCallIgnored
							btSocket.getInputStream().read(dataBuffer);
							String data = new String(dataBuffer, StandardCharsets.UTF_8);
							if (statusListener != null) statusListener.onData(data);
						}
					} catch (IOException e) {
						if (statusListener != null) statusListener.onError("Input stream was disconnected");
					}
				}
			}
		}).start();
	}

	public boolean sendString(String data) {
		if (DEBUG) {
			printMsg("Invoice length: "+data.length());
			return true;
		} else {
			if (isConnected()) {
				send(data.getBytes());
				return true;
			} else {
				Toast.makeText(getApplicationContext(), "Printer isn't connected!", Toast.LENGTH_SHORT).show();
				return false;
			}
		}
	}

	private void printMsg(String msg) {
		if (DEBUG) {
			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
		}
	}

	public boolean isConnected() {
		if(btSocket == null) return false;
		return btSocket.isConnected();
	}

	public void disconnect() {
		if (btSocket != null)
		{
			keep_reading = false;

			try
			{
				btSocket.close();
				btSocket = null;
				if (statusListener != null) statusListener.onDisconnected();
			}
			catch (IOException e)
			{
				if (statusListener != null) statusListener.onConnectionError("socket close error");
			}
		}
	}

	public void discover() {
		registerReceiver(broadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		bluetoothAdapter.startDiscovery();
	}

	public void cancelDiscover() {
		bluetoothAdapter.cancelDiscovery();
		unregisterReceiver(broadcastReceiver);
	}

	public static boolean isStopped(Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo info : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (BluetoothService.class.getName().equals(info.service.getClassName())) {
				return false;
			}
		}
		return true;
	}

	public interface StatusListener {
		void onConnectionError(String msg);
		void onConnecting(BluetoothDevice device);
		void onConnected();
		void onDisconnected();
		void onError(String message);
		void onData(String data);
	}

	public interface ViewSetup {
		View returnView(int position, ArrayList<BluetoothDevice> list);
	}

	public class BlueBinder extends Binder {
		public BluetoothService getService() {
			return BluetoothService.this;
		}
	}

	public class Adapter extends BaseAdapter {
		public Adapter(ArrayList<BluetoothDevice> list) {
			deviceList = list;
		}
		public Adapter() {
			if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
				deviceList = new ArrayList<>();
				Set<BluetoothDevice> set = bluetoothAdapter.getBondedDevices();
				deviceList.addAll(set);
			} else {
				deviceList = null;
			}
		}
		@Override
		public int getCount() {
			if (deviceList != null) {
				return deviceList.size();
			}
			return 0;
		}
		@Override
		public BluetoothDevice getItem(int p1) {
			if (deviceList != null) {
				return deviceList.get(p1);
			}
			return null;
		}
		@Override
		public long getItemId(int p1) {
			return p1;
		}
		@Override
		public View getView(int p1, View p2, ViewGroup p3) {
			if(viewSetup != null)
				return viewSetup.returnView(p1, deviceList);
			else
				return null;
		}
	}

	private class BlueBroadcastReceiver extends  BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action))
			{
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (deviceList != null)
					deviceList.add(device);
				if (adapter != null)
					adapter.notifyDataSetChanged();
			}
		}
	};

}
