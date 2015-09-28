package com.isosystems.smartmaid.connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.isosystems.smartmaid.Globals;

public class old_WifiReceiver extends BroadcastReceiver {

    /**
     * 1) Идет проверка на состояние сети.
     * Если произошло подключение к сети, но сеть
     * имеет неверное название, то отсылается броадкаст на переподключение
     *
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
                if (!mWifiManager.getConnectionInfo().getSSID().equals(Globals.sWifiName)
                        && !mWifiManager.getConnectionInfo().getSSID().equals("\"" + Globals.sWifiName + "\"")) {
                    Intent i = new Intent();
                    i.setAction("SMART.WIFI.RECONNECT");
                    context.sendBroadcast(i);
                }
            }
        } else if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
            int status = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
            if (status == WifiManager.WIFI_STATE_ENABLED) {
                Intent i = new Intent();
                i.setAction("SMART.WIFI.BINDSERVICE");
                context.sendBroadcast(i);
            } else if (status == WifiManager.WIFI_STATE_DISABLED) {
                Intent i = new Intent();
                i.setAction("SMART.WIFI.BINDSERVICE");
                context.sendBroadcast(i);
            }
        }
    } //onReceive
} // old_WifiReceiver