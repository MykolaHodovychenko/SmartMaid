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

import java.util.HashMap;

public class USBSendService extends IntentService {

	UsbManager usbManager;
	HashMap<String, UsbDevice> deviceList;
	UsbDevice usbDevice;
	UsbInterface usbInterface;
	UsbEndpoint usbEndpointOut;
	UsbDeviceConnection usbConnection;

	Boolean wasSend;

	public USBSendService() {
		super("USBSEND");
	}

	public void onCreate() {
		super.onCreate();

		wasSend = false;

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
        	
        	if ((device.getProductId() == 257) && (device.getVendorId() == 65535)) {
        		try {
        			usbDevice = device;
        		} catch (Exception e) {
        			e.printStackTrace();
        			this.stopSelf();
        			return;
        		}
        	}//if
        }
        
        if (usbDevice == null) {
			this.stopSelf();
			return;
        }

        for (int i = 0; i < usbDevice.getInterfaceCount();i++) {
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

			if (tempPoint.getDirection() == UsbConstants.USB_DIR_OUT) {
				usbEndpointOut = tempPoint;
			}
		}//for

        if (usbEndpointOut == null) {
			this.stopSelf();
			return;
        }
		
		try {
			usbConnection = usbManager.openDevice(usbDevice);
		} catch (Exception e) {
			e.printStackTrace();
			this.stopSelf();
		}
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		String msg;
		try {
			msg = intent.getStringExtra("message");
		} catch (Exception e) {
			e.printStackTrace();
			this.stopSelf();
			return;
		}

		if ((usbConnection !=null) && (usbDevice!=null)) {

			try {
				usbConnection.claimInterface(usbInterface, true);
			} catch (Exception e) {
				e.printStackTrace();
				this.stopSelf();
				return;
			}

			int result = -1;
			try {
				result = usbConnection.bulkTransfer(usbEndpointOut,
						msg.getBytes(), msg.getBytes().length, 0);
				wasSend = true;

			} catch (Exception e) {
				e.printStackTrace();
				this.stopSelf();
				return;
			}

			try {
				usbConnection.releaseInterface(usbInterface);
			} catch (Exception e) {
				e.printStackTrace();
				this.stopSelf();
				return;
			}
		} else {
			wasSend = false;
		}
	}

	public void onDestroy() {
		try {
			super.onDestroy();
		} catch (Exception e) {
			e.printStackTrace();
			super.onDestroy();
		}
	}
}