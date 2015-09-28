package com.isosystems.smartmaid.connection;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class UsbAttachEventReceiver extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (getApplicationContext() == null) finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = getIntent();
		if (intent!=null){
			ConnectionManager.staticStartUSBReceiveService(getApplicationContext());
		}
		finish();
	}
}
