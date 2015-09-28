package com.isosystems.smartmaid.connection;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class USBReceiveService extends IntentService {

	UsbManager usbManager;
	HashMap<String, UsbDevice> deviceList;
	UsbDevice usbDevice;
	UsbInterface usbInterface;
	UsbEndpoint usbEndPointIn;
	UsbDeviceConnection usbConnection;

	StringBuilder mMessageBuffer;
	static Handler mMessageHandler;

	static Handler mBufferCleanHandler;
	int mBufferClearTimeout = 2000;

	public USBReceiveService() {
		super("USBReceive");
	}

	public void onCreate() {
		super.onCreate();

		mMessageBuffer = new StringBuilder();

		mBufferCleanHandler = new Handler();
		mBufferCleanHandler.postDelayed(mBufferClearRunnable,
				mBufferClearTimeout);

		mMessageHandler = new Handler() {
			public void handleMessage(Message msg) {
				Bundle bundle = msg.getData();

				String message = bundle.getString("incoming_message");
				mMessageBuffer.append(message);

				Pattern p = Pattern.compile("[@&$#](.*?)¶");
				Matcher m = p.matcher(mMessageBuffer);
				while (m.find()) {
					messageProcess(m.group());
				}

				mMessageBuffer = new StringBuilder(m.replaceAll(""));
			} // handle message
		}; // handler

		try {
			usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
			deviceList = usbManager.getDeviceList();
		} catch (Exception e) {
			e.printStackTrace();
			this.stopSelf();
			return;
		}

		if (deviceList.isEmpty()) {
			this.stopSelf();
			return;
		}

		for (UsbDevice device : deviceList.values()) {
			if ((device.getProductId() == 257)
					&& (device.getVendorId() == 65535)) {
				try {
					usbDevice = device;
				} catch (Exception e) {
					e.printStackTrace();
					this.stopSelf();
					return;
				}
			}// if
		}// for

		if (usbDevice == null) {
			this.stopSelf();
			return;
		}

		for (int i = 0; i < usbDevice.getInterfaceCount(); i++) {
			UsbInterface tempInterfce = usbDevice.getInterface(i);
			if (tempInterfce.getEndpointCount() > 1) {
				usbInterface = tempInterfce;
			}
		}

		if (usbInterface == null) {
			this.stopSelf();
			return;
		}

		for (int i = 0; i < usbInterface.getEndpointCount(); i++) {

			UsbEndpoint tempPoint = usbInterface.getEndpoint(i);
			if (tempPoint.getDirection() == UsbConstants.USB_DIR_IN) {
				usbEndPointIn = tempPoint;
			}
		}// for

		if (usbEndPointIn == null) {
			this.stopSelf();
			return;
		}

		try {
			usbConnection = usbManager.openDevice(usbDevice);
		} catch (Exception e) {
			e.printStackTrace();
			this.stopSelf();
			return;
		}

		Intent i = new Intent();
		i.setAction(ConnectionManager.USB_CONNECTED);
		getApplicationContext().sendBroadcast(i);
	}

	private Runnable mBufferClearRunnable = new Runnable() {
		public void run() {
			mMessageBuffer = new StringBuilder();

			mBufferCleanHandler.removeCallbacks(mBufferClearRunnable);
			mBufferCleanHandler.postDelayed(mBufferClearRunnable,
					mBufferClearTimeout);
		}
	};

	private Boolean checkUsbDevice() {
		UsbManager manager;
		HashMap<String, UsbDevice> dList;

		manager = (UsbManager) getSystemService(Context.USB_SERVICE);
		dList = manager.getDeviceList();

		if (dList.isEmpty())
			return false;

		for (UsbDevice device : dList.values()) {
			if ((device.getProductId() == 257)
					&& (device.getVendorId() == 65535)) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		while (checkUsbDevice()) {
			byte[] mReadBuffer = new byte[128];
			int transferred = -1;

			try {
				usbConnection.claimInterface(usbInterface, true);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}

			try {
				transferred = usbConnection.bulkTransfer(usbEndPointIn,
						mReadBuffer, mReadBuffer.length, 0);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}

			try {
				usbConnection.releaseInterface(usbInterface);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}

			if (transferred >= 0) {

				mBufferCleanHandler.removeCallbacks(mBufferClearRunnable);
				mBufferCleanHandler.postDelayed(mBufferClearRunnable,
						mBufferClearTimeout);

				String mReceivedMessage = new String(mReadBuffer, 0,
						transferred, Charset.forName("windows-1251"));

				Bundle b = new Bundle();
				b.putString("incoming_message", mReceivedMessage);

				Message msg = new Message();
				msg.setData(b);

				mMessageHandler.sendMessage(msg);

			} // end if transferred
		} // end while
	} // end onHandleIntent

	private void messageProcess(String message) {
		Intent i = new Intent();
		message = message.substring(0, message.length() - 1);

		switch (message.charAt(0)) {
			case '$':
				if (message.length() > 2) {
					String alarmMessage = message.substring(2);
					i.setAction(ConnectionManager.MESSAGE_ALARM);
					i.putExtra(ConnectionManager.MESSAGE_EXTRA, alarmMessage);
				} else {
					i.setAction(ConnectionManager.ERROR_MESSAGE_SHORT);
				}
				break;
			case '&':
				i.putExtra(ConnectionManager.MESSAGE_EXTRA, message);
				i.setAction(ConnectionManager.MESSAGE_VALUE);
				break;
			case '@':
				i.putExtra(ConnectionManager.MESSAGE_EXTRA, message);
				i.setAction(ConnectionManager.MESSAGE_FORMSCREEN);
				break;
			case '#':
				i.putExtra(ConnectionManager.MESSAGE_EXTRA, message);
				i.setAction(ConnectionManager.MESSAGE_FORMSCREEN_FORCED);
				break;
			default:
				i.setAction(ConnectionManager.ERROR_MESSAGE_UNKNOWN_TYPE);
				break;
		}
		getApplicationContext().sendBroadcast(i);
	} // end method

	public void onDestroy() {
		try {
			try {
				usbConnection.releaseInterface(usbInterface);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (mBufferCleanHandler != null) {
				mBufferCleanHandler.removeCallbacks(mBufferClearRunnable);
			}

			Intent i = new Intent();
			i.setAction(ConnectionManager.USB_DISCONNECTED);
			getApplicationContext().sendBroadcast(i);

			super.onDestroy();
		} catch (Exception e) {
			e.printStackTrace();
			super.onDestroy();
		}
	}
}