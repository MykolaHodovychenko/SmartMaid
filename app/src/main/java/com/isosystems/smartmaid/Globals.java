package com.isosystems.smartmaid;

/**
 * Данный класс содержит глобальные константы для приложения
 */
public final class Globals {

    // SETTINGS

    public static String sWifiName = "";

    public static final String POWER_SUPPLY_CHANGED = "SMART.POWER_SUPPLY_CHANGED";
    public static final String WIFI_DISCONNECT = "SMART.WIFI.DISCONNECT";
    public static final String WIFI_CONNECTED = "SMART.WIFI.CONNECTED";

    /** Action для Broadcast Receiver для прихода сообщения о смене режима питания устройства */
    public static final String BROADCAST_INTENT_POWER_SUPPLY_CHANGED = "SMARTHOUSE.POWER_SUPPLY_CHANGED";

    public static final String SERVICE_PASSWORD = "924";
}