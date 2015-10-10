package com.isosystems.smartmaid;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.isosystems.smartmaid.connection.ConnectionManager;
import com.isosystems.smartmaid.settings.FragmentSettingsLog;
import com.isosystems.smartmaid.settings.SettingsActivity;
import com.isosystems.smartmaid.utils.RoomsManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    ImageButton mSettingsButton;
    ImageButton mHideRoomsButton;

    ImageButton mMuteSoundButton;
    boolean isSoundMuted = false;

    RoomsManager mRoomsManager;

    MessagesReceiver mReceiver;

    int mStartingRoomNumber;
    int mNumberOfRooms;

    ConnectionManager.ConnectionMode mMode;
    ConnectionManager mConnectionManager;

    String wifi_name;
    private String password;
    private Boolean socket_endless_timeout;
    private String socket_ip;
    private int socket_port;
    private String socket_greetings_message;

    ImageView mWifiIcon;
    ImageView mPowerIcon;

    // Состояние соединения с контроллером
    Boolean isConnected = false;

    MyApplication mApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                handleUncaughtException(thread, e);
            }
        });

        mApplication = (MyApplication) getApplicationContext();

        // Считывание настроек
        setPreferences();

        // Создаем менеджер подключений
        mConnectionManager = new ConnectionManager(mMode, MainActivity.this, wifi_name, password, socket_endless_timeout, socket_ip, socket_port, socket_greetings_message);
        if (mConnectionManager.getConnectionMode() == ConnectionManager.ConnectionMode.USB) {
            FragmentSettingsLog.updateLog("Режим подключения: USB", getApplicationContext());
            mConnectionManager.startUSBReceiveService();
        } else {
            FragmentSettingsLog.updateLog("Режим подключения: WIFI", getApplicationContext());
        }
    }

    public void handleUncaughtException(Thread thread, Throwable e) {
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


    @Override
    protected void onStart() {
        super.onStart();

        mWifiIcon = (ImageView) findViewById(R.id.image_wifi);
        mPowerIcon = (ImageView) findViewById(R.id.image_plug);

        setPowerIcon(isSupplyEnabled());
        setConnectionIcon(false);

        // Установка полного экрана
        setFullScreen();

        mRoomsManager = new RoomsManager(getWindow().getDecorView().getRootView(), MainActivity.this, mStartingRoomNumber, mNumberOfRooms);

        // Кнопка "Настройки"
        mSettingsButton = (ImageButton) findViewById(R.id.settings_button);
        mSettingsButton.setOnClickListener(mSettingsButtonListener());

        // Кнопка показа/скрытия неактивных комнат
        mHideRoomsButton = (ImageButton) findViewById(R.id.hide_rooms);
        mHideRoomsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mApplication.isRoomHiding) {
                    mHideRoomsButton.setImageResource(R.drawable.show);
                    mApplication.isRoomHiding = false;
                    mRoomsManager.showEmptyRooms(true);
                } else {
                    mHideRoomsButton.setImageResource(R.drawable.hide);
                    mApplication.isRoomHiding = true;
                    mRoomsManager.showEmptyRooms(false);
                }
            }
        });

        // Кнопка отключения/включения звукового сигнала
        mMuteSoundButton = (ImageButton) findViewById(R.id.sound_button);
        mMuteSoundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSoundMuted) {
                    mMuteSoundButton.setImageResource(R.drawable.sound);
                    isSoundMuted = false;
                    ((MyApplication) getApplicationContext()).soundMessages.setEnable_sound(true);
                } else {
                    mMuteSoundButton.setImageResource(R.drawable.mute);
                    isSoundMuted = true;
                    ((MyApplication) getApplicationContext()).soundMessages.setEnable_sound(false);

                }
            }
        });

        // Установка значений диапазона комнат
        TextView rooms = (TextView) findViewById(R.id.left_rooms);
        rooms.setText(String.valueOf(mStartingRoomNumber) + " — " + String.valueOf(mStartingRoomNumber + mNumberOfRooms / 2 - 1));
        rooms = (TextView) findViewById(R.id.right_rooms);
        rooms.setText(String.valueOf(mStartingRoomNumber + mNumberOfRooms / 2) + " — " + String.valueOf(mStartingRoomNumber + mNumberOfRooms));

        mReceiver = new MessagesReceiver();

        setupMessagesReceiver();
    }

    private void setupMessagesReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectionManager.MESSAGE_VALUE);
        filter.addAction(ConnectionManager.MESSAGE_ALARM);
        filter.addAction(Globals.BROADCAST_INTENT_POWER_SUPPLY_CHANGED);
        filter.addAction(ConnectionManager.WIFI_CONNECTED);
        filter.addAction(ConnectionManager.WIFI_DISCONNECTED);
        filter.addAction(ConnectionManager.USB_CONNECTED);
        filter.addAction(ConnectionManager.USB_DISCONNECTED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mConnectionManager.unregisterReceiver();
        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        setPowerIcon(isSupplyEnabled());
        setConnectionIcon(isConnected);
    }

    /**
     * Настройка полного экрана
     */
    private void setFullScreen() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Запрет на отключение экрана
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Process proc = null;
        String ProcID = "79"; //HONEYCOMB AND OLDER

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ProcID = "42"; //ICS AND NEWER
        }

        try {
            proc = Runtime.getRuntime().exec(new String[]{"su", "-c", "service call activity " + ProcID + " s16 com.android.systemui"});
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            proc.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Включение полноэкранного режим планшета
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        decorView.setSystemUiVisibility(8);
        // <<-----------------------------------
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mConnectionManager.doUnbindService();
        try {
            Runtime.getRuntime().exec("am startservice --user 0 -n com.android.systemui/.SystemUIService");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Считывание настроек
     */
    private void setPreferences() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext().getApplicationContext());

        // Тип подключения
        String connection_type = prefs.getString("connection_type", "0");
        if (connection_type.equals("0")) {
            mMode = ConnectionManager.ConnectionMode.WIFI;
        } else if (connection_type.equals("1")) {
            mMode = ConnectionManager.ConnectionMode.USB;
        }

        wifi_name = prefs.getString("wifi_name", "YAM-AP-00000002");

        password = prefs.getString("wifi_password", "12345678");

        socket_ip = prefs.getString("socket_ip", "192.168.4.1");

        String s = prefs.getString("socket_port", "12345");
        try {
            socket_port = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        socket_endless_timeout = prefs.getBoolean("socket_timeout_endless", true);

        socket_greetings_message = prefs.getString("socket_message", "DisplayNumber:1");


        boolean enable_sound = prefs.getBoolean("use_sound_signal", false);
        //((MyApplication)getApplicationContext()).soundMessages.setEnable_sound(enable_sound);

        // Стартовый гостиничный номер
        s = prefs.getString("room_starting_index", "100");
        try {
            mStartingRoomNumber = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        // Количество номеров
        s = prefs.getString("room_quantity", "50");
        try {
            mNumberOfRooms = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        // Звуковой сигнал
        s = prefs.getString("signals_list", "0");
        int sound_index = 0;
        try {
            sound_index = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        int soundResource = 0;
        switch (sound_index) {
            case 0:
                soundResource = R.raw.ding;
                break;
            case 1:
                soundResource = R.raw.noway;
                break;
            case 2:
                soundResource = R.raw.jobdone;
                break;
            case 3:
                soundResource = R.raw.goh;
                break;
            case 4:
                soundResource = R.raw.suppressed;
                break;
            case 5:
                soundResource = R.raw.croak;
                break;
        }
        ((MyApplication) getApplicationContext()).soundMessages.setPlayerSound(getApplicationContext(), soundResource);
    }

    /**
     * Кнопка "Настройки"
     *
     * @return
     */
    View.OnClickListener mSettingsButtonListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                final View dialog_view = inflater.inflate(
                        R.layout.fragment_dialog_check_password, null);

                // Включение полноэкранного режим планшета
                int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

                dialog_view.setSystemUiVisibility(uiOptions);
                dialog_view.setSystemUiVisibility(8);


                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Введите пароль для входа в настройки:")
                        .setView(dialog_view)
                        .setPositiveButton("Войти",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {

                                        String password = ((EditText) dialog_view
                                                .findViewById(R.id.checkpassword_dialog_password))
                                                .getText().toString();

                                        Boolean correct_password = false;
                                        if (password.equals(Globals.SERVICE_PASSWORD)) {
                                            correct_password = true;
                                        }

                                        if (correct_password) {
                                            // Пароль правильный

                                            Intent intent = new Intent(
                                                    MainActivity.this,
                                                    SettingsActivity.class);
                                            startActivity(intent);
                                        } else {
                                            // Пароль неправильный
                                            Toast.makeText(MainActivity.this, "Неверный пароль", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                        .setNegativeButton("Отмена",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                    }
                                }).create().show();
            }
        };
    }

    public class MessagesReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectionManager.MESSAGE_VALUE)) {
                String message = intent.getStringExtra(ConnectionManager.MESSAGE_EXTRA);
                mRoomsManager.updateData(message);
            } else if (intent.getAction().equals(Globals.BROADCAST_INTENT_POWER_SUPPLY_CHANGED)) {
                setPowerIcon(isSupplyEnabled());
            } else if (intent.getAction().equals(ConnectionManager.WIFI_CONNECTED) ||
                    intent.getAction().equals(ConnectionManager.USB_CONNECTED)) {
                FragmentSettingsLog.updateLog("Сеть подключена, меняем значок на зеленый",getApplicationContext());
                isConnected = true;
                setConnectionIcon(true);
            } else if (intent.getAction().equals(ConnectionManager.WIFI_DISCONNECTED) ||
                    intent.getAction().equals(ConnectionManager.USB_DISCONNECTED)) {
                FragmentSettingsLog.updateLog("Сеть отключена, меняем значок на крансный",getApplicationContext());
                isConnected = false;
                setConnectionIcon(false);
            }
        }
    }

    private Boolean isSupplyEnabled() {
        Intent intent = mApplication.registerReceiver(null, new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED));
        int plugged = 0;
        plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

        boolean result = (plugged != 0 && plugged != -1);
        return result;
    }

    private void setConnectionIcon(boolean state) {
        if (mConnectionManager.getConnectionMode() == ConnectionManager.ConnectionMode.USB) {
            if (state) {
                mWifiIcon.setImageResource(R.drawable.usb_on);
            } else {
                mWifiIcon.setImageResource(R.drawable.usb_off);
            }
        } else if (mConnectionManager.getConnectionMode() == ConnectionManager.ConnectionMode.WIFI) {
            if (state) {
                mWifiIcon.setImageResource(R.drawable.wifi_on);
            } else {
                mWifiIcon.setImageResource(R.drawable.wifi_off);
            }
        }
    }

    private void setPowerIcon(boolean state) {
        if (state) {
            mPowerIcon.setImageResource(R.drawable.power_on);
        } else {
            mPowerIcon.setImageResource(R.drawable.power_off);
        }
    }
}
