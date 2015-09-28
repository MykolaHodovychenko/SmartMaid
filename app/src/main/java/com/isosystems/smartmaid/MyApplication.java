/*
 * Мобильное приложение для проекта "Умный дом"
 * 
 * author: Годовиченко Николай
 * email: nick.godov@gmail.com
 * last edit: 11.09.2014
 */

package com.isosystems.smartmaid;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import com.isosystems.smartmaid.utils.SoundMessages;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class MyApplication extends Application {

	public SoundMessages soundMessages;

	public boolean isRoomHiding = false;

	public boolean wifiConnection = true;

	@Override
	public void onCreate() {
		super.onCreate();

		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable e) {
				handleUncaughtException(thread, e);
			}
		});

		soundMessages = new SoundMessages(getApplicationContext());
	}

	public void handleUncaughtException (Thread thread, Throwable e)
	{
		e.printStackTrace(); // not all Android versions will print the stack trace automatically

		try {
			extractLogToFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void extractLogToFile() throws IOException {
		PackageManager manager = this.getPackageManager();
		PackageInfo info = null;
		try {
			info = manager.getPackageInfo(this.getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e2) {
		}
		String model = Build.MODEL;
		if (!model.startsWith(Build.MANUFACTURER))
			model = Build.MANUFACTURER + " " + model;

		// Make file name - file must be saved to external storage or it wont be readable by
		// the email app.
		String path = Environment.getExternalStorageDirectory().getPath();
		String fullName = path + "/smartmaid_crashlog" + String.valueOf(System.currentTimeMillis());

		// Extract to file.
		File file = new File(fullName);
		InputStreamReader reader = null;
		FileWriter writer = null;
		try {
			// For Android 4.0 and earlier, you will get all app's log output, so filter it to
			// mostly limit it to your app's output.  In later versions, the filtering isn't needed.
			String cmd = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) ?
					"logcat -d -v time MyApp:v dalvikvm:v System.err:v *:s" :
					"logcat -d -v time";

			// get input stream
			Process process = Runtime.getRuntime().exec(cmd);
			reader = new InputStreamReader(process.getInputStream());

			// write output stream
			writer = new FileWriter(file);
			writer.write("Android version: " + Build.VERSION.SDK_INT + "\n");
			writer.write("Device: " + model + "\n");
			writer.write("App version: " + (info == null ? "(null)" : info.versionCode) + "\n");

			char[] buffer = new char[10000];
			do {
				int n = reader.read(buffer, 0, buffer.length);
				if (n == -1)
					break;
				writer.write(buffer, 0, n);
			} while (true);

			reader.close();
			writer.close();
		} catch (IOException e) {
			if (writer != null)
				try {
					writer.close();
				} catch (IOException e1) {
				}
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e1) {
				}
		}

		System.exit(1);
	}
}