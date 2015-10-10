package com.isosystems.smartmaid.connection;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.isosystems.smartmaid.Globals;
import com.isosystems.smartmaid.settings.FragmentSettingsLog;
import com.isosystems.smartmaid.utils.Notifications;

/**
 * Менеджер соединения приложения с wifi/usb
 * <p/>
 * Created by NickGodov on 30.08.2015.
 */
public class ConnectionManager {

    public static final String USB_CONNECTED = "smart.connection.usb.connected";
    public static final String USB_DISCONNECTED = "smart.connection.usb.disconnected";
    public static final String OLD_USB_DISCONNECTED = "smart.connection.usb.disconnected";
    public static final String WIFI_CONNECTED = "smart.connection.wifi.connected";
    public static final String WIFI_DISCONNECTED = "smart.connection.wifi.disconnected";

    public static final String ERROR_MESSAGE_SHORT = "smart.connection.error.message.short";
    public static final String ERROR_MESSAGE_UNKNOWN_TYPE = "smart.connection.error.message.unknown.type";

    public static final String MESSAGE_SEND = "smart.connection.message.send";
    public static final String MESSAGE_ALARM = "smart.connection.message.alarm";
    public static final String MESSAGE_VALUE = "smart.connection.message.value";
    public static final String MESSAGE_FORMSCREEN = "smart.connection.message.formscreen";
    public static final String MESSAGE_FORMSCREEN_FORCED = "smart.connection.message.formscreen.forced";
    public static final String MESSAGE_EXTRA = "smart.connection.message.extra";

    private String wifiName;
    private String password;
    private Boolean socket_endless_timeout;
    private String socket_ip;
    private int socket_port;
    private String socket_greetings_message;

    private boolean isConnected;

    SocketService mBoundService = null;
    boolean mIsBound = false;

    WifiReceiver mReceiver;

    Activity activity;

    static ConnectionMode connectionMode = ConnectionMode.WIFI;

    public ConnectionManager(ConnectionMode mode, Activity activity, String wifi_name, String password, boolean socket_endless_timeout, String socket_ip,
                             int socket_port, String socket_greetings_message) {
        this.wifiName = wifi_name;
        this.password = password;
        this.socket_endless_timeout = socket_endless_timeout;
        this.socket_ip = socket_ip;
        this.socket_port = socket_port;
        this.socket_greetings_message = socket_greetings_message;

        ConnectionManager.connectionMode = mode;
        this.activity = activity;
        mReceiver = new WifiReceiver();
        setupReceiver();
    }

    /**
     * Настройка ресивера и регистрация, происходит при старте активити и инициализации менеджера
     */
    private void setupReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WIFI_CONNECTED);
        filter.addAction(WIFI_DISCONNECTED);
        filter.addAction(USB_CONNECTED);
        filter.addAction(USB_DISCONNECTED);

        activity.registerReceiver(mReceiver, filter);
    }

    public ConnectionMode getConnectionMode() {
        return connectionMode;
    }

    /**
     * Запуск сервиса через подключение usb
     *
     * @param context
     */
    public static void staticStartUSBReceiveService(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context.getApplicationContext());

        if (prefs.getString("connection_type", "0").equals("1")) {

            Intent i = new Intent(context, USBReceiveService.class);
            context.startService(i);
        }
    }

    /**
     * запуск сервиса для usb вручную
     */
    public void startUSBReceiveService() {
        if (this.connectionMode == ConnectionMode.USB) {
            Intent i = new Intent(activity, USBReceiveService.class);
            activity.startService(i);
        }
    }


    public Boolean sendMessage(Context context, int index, int value) {
        String messageToSend = processMessage(index, value);

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context.getApplicationContext());

        boolean blockOutput = prefs.getBoolean("block_output_without_connection", true);

        if (blockOutput) {
            if (isConnected) {
                if (connectionMode == connectionMode.USB) {
                    Intent i = new Intent(activity.getApplicationContext(),
                            USBSendService.class);
                    i.putExtra("message", messageToSend);
                    activity.getApplicationContext().startService(i);
                } else if (connectionMode == connectionMode.WIFI) {
                    if (mBoundService != null) {
                        mBoundService.sendMessage(messageToSend);
                    }
                }
                return true;
            } else {
                Notifications.showErrorCrouton(activity, Globals.NOTIFICATION_NO_CONNECTION);
                return false;
            }
        } else {
            if (connectionMode == connectionMode.USB) {
                Intent i = new Intent(activity.getApplicationContext(),
                        USBSendService.class);
                i.putExtra("message", messageToSend);
                activity.getApplicationContext().startService(i);
            } else if (connectionMode == connectionMode.WIFI) {
                if (mBoundService != null) {
                    mBoundService.sendMessage(messageToSend);
                }
            }
            return true;
        }
    }

    private String processMessage(int index, int value) {
        return String.valueOf(index + 100) + "," + String.valueOf(value);
    }

    public Boolean isConnected() {
        return this.isConnected;
    }

    /**
     * Анергистр при остановке активити
     */
    public void unregisterReceiver() {
        if (mReceiver != null) {
            try {
                activity.unregisterReceiver(mReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Бинд сервиса. Происходит при включении модуля wifi
     */
    private void doBindService() {
        Intent i = new Intent(activity, SocketService.class);
        i.putExtra("wifiName", wifiName);
        i.putExtra("password", password);
        i.putExtra("socket_ip", socket_ip);
        i.putExtra("socket_port", socket_port);
        i.putExtra("socket_endless_timeout", socket_endless_timeout);
        i.putExtra("socket_greetings_message", socket_greetings_message);

        try {
            activity.bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity, "cm_dobind error", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Анбинд сервиса, происходит при отключении модуля wifi
     */
    public void doUnbindService() {
        if (mIsBound) {
            activity.unbindService(mConnection);
            mIsBound = false;
        }
    }

    /**
     * Соединение с сервисом
     */
    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                mBoundService = ((SocketService.LocalBinder) service).getService();
                mIsBound = true;
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(activity, "cm_sc_onsc error", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBoundService = null;
        }
    };

    /**
     * Режим соединения (usb/wifi)
     */
    public enum ConnectionMode {
        WIFI,
        USB
    }

    public class WifiReceiver extends BroadcastReceiver {
        /**
         * 1) Идет проверка на состояние сети.
         * Если произошло подключение к сети, но сеть
         * имеет неверное название, то отсылается броадкаст на переподключение
         * <p/>
         * 2) Проверяется состояние wi-fi модуля.
         * Ксли модуль подключен - отсылаем броадкаст на бинд сервиса
         * Ксли модуль отключен - отсылаем броадкаст на анбинд
         *
         * @param context
         * @param intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                NetworkInfo.DetailedState state = info.getDetailedState();

                if (state == NetworkInfo.DetailedState.CONNECTED) {
                    if (!mWifiManager.getConnectionInfo().getSSID().equals(wifiName)
                            && !mWifiManager.getConnectionInfo().getSSID().equals("\"" + wifiName + "\"")) {
                        try {
                            FragmentSettingsLog.updateLog("Подключено к сети " + mWifiManager.getConnectionInfo().getSSID() +
                                    ", сеть неправильная, делаем реконнент", context);
                            if (mBoundService != null) {
                                mBoundService.reconnectToWifi();
                            }
                        } catch (Exception e) {
                            FragmentSettingsLog.updateLog("Исключение при попытке реконнекта, текущая сеть " +
                                    mWifiManager.getConnectionInfo().getSSID(), context);
                            e.printStackTrace();
                            Toast.makeText(activity, "cm_br_reconnect error", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            } else if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int status = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                if (status == WifiManager.WIFI_STATE_ENABLED) {
                    FragmentSettingsLog.updateLog("WIFI модуль включен ", context);
                    doBindService();
                } else if (status == WifiManager.WIFI_STATE_DISABLED) {
                    FragmentSettingsLog.updateLog("WIFI модуль выключен ", context);
                    doUnbindService();
                }
            } else if (intent.getAction().equals(ConnectionManager.WIFI_CONNECTED) ||
                    intent.getAction().equals(ConnectionManager.USB_CONNECTED)) {
                isConnected = true;
            } else if (intent.getAction().equals(ConnectionManager.WIFI_DISCONNECTED) ||
                    intent.getAction().equals(ConnectionManager.USB_DISCONNECTED)) {
                isConnected = false;
            } else if (intent.getAction().equals(ConnectionManager.MESSAGE_SEND)) {
                // TODO: отправка сообщения
            }
        } //onReceive
    } // WifiReceiver

}
