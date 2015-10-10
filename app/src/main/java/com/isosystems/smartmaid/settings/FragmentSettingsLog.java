/*
 * SmartHotel, created by NickGodov on 30.09.15 10:46.
 * Last modified: 30.09.15 10:46
 *
 * This software is protected by copyright law and international treaties.
 * Unauthorized reproduction or distribution of this program, or any portion of it, may result in severe
 * civil and criminal penalties, and will be prosecuted to the maximum extent possible under law.
 *
 */

package com.isosystems.smartmaid.settings;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.isosystems.smartmaid.Globals;
import com.isosystems.smartmaid.MyApplication;
import com.isosystems.smartmaid.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FragmentSettingsLog extends Fragment {

    View rootView;
    TextView mLog;
    Button mLogButton;

    MyApplication mApplication;

    public static StringBuilder logBuilder;

    LogReceiver mReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_log,
                container, false);
        mApplication = (MyApplication) getActivity().getApplicationContext();

        mReceiver = new LogReceiver();

        mLogButton = (Button) rootView.findViewById(R.id.log_button);
        mLogButton.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {
                                              saveLog();
                                              logBuilder = new StringBuilder();
                                              mLog.setText("");
                                          }
                                      }
        );

        mLog = (TextView) rootView.findViewById(R.id.log);
        mLog.setText(logBuilder.toString());

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Globals.BROADCAST_INTENT_UPDATELOG);
        getActivity().registerReceiver(mReceiver, filter);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mReceiver != null) {
            this.getActivity().unregisterReceiver(mReceiver);
        }
    }

    public static void updateLog(String message, Context c) {

        if (logBuilder == null) {
            logBuilder = new StringBuilder();
        }
        logBuilder.append(getCurrentTimeStamp());
        logBuilder.append("> ");
        logBuilder.append(message);
        logBuilder.append("\n");

        Intent i = new Intent();
        i.setAction(Globals.BROADCAST_INTENT_UPDATELOG);
        c.getApplicationContext().sendBroadcast(i);
    }

    public static String getCurrentTimeStamp() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            String currentTimeStamp = dateFormat.format(new Date()); // Find todays date
            return currentTimeStamp;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    private void saveLog() {
        if (isExternalStorageWritable()) {

            File externalFilesDir = Environment.getExternalStorageDirectory();
            File externalLogDirectory = new File(externalFilesDir + File.separator
                    + Globals.EXTERNAL_ROOT_DIRECTORY + File.separator
                    + Globals.EXTERNAL_LOGS_DIRECTORY);
            externalLogDirectory.mkdirs();

            String fname = "log-" + System.currentTimeMillis() + ".txt";
            File file = new File(externalLogDirectory, fname);
            if (file.exists()) file.delete();
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(file, false));
                out.write(logBuilder.toString());
                out.flush();
                out.close();
                Toast.makeText(mApplication, "Файл записан, папка:" + externalLogDirectory.toString() + " файл: " + fname, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(mApplication, "Исключение при попытен записать файл", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(mApplication, "Внешнее хранилище недоступно", Toast.LENGTH_LONG).show();
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public class LogReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Globals.BROADCAST_INTENT_UPDATELOG)) {
                mLog.setText(logBuilder.toString());
            }
        }
    }
}
