package com.isosystems.smartmaid;

/**
 * Данный класс содержит глобальные константы для приложения
 */
public final class Globals {

    // SETTINGS

    public static String sWifiName = "";

    /** Папка на sd-card с файлами для приложения */
    public static final String EXTERNAL_ROOT_DIRECTORY = "smartmaid";
    /** Папка в EXTERNAL_ROOT_DIRECTORY где хранятся логи */
    public static final String EXTERNAL_LOGS_DIRECTORY = "logs";
    /** Папка в EXTERNAL_ROOT_DIRECTORY где хранятся картинки для слайд-шоу */
    public static final String EXTERNAL_SCREENSAVER_IMAGES_DIRECTORY = "screensaver";

    public static final String POWER_SUPPLY_CHANGED = "SMART.POWER_SUPPLY_CHANGED";
    public static final String WIFI_DISCONNECT = "SMART.WIFI.DISCONNECT";
    public static final String WIFI_CONNECTED = "SMART.WIFI.CONNECTED";

    /** Action для Broadcast Receiver для прихода сообщения о смене режима питания устройства */
    public static final String BROADCAST_INTENT_POWER_SUPPLY_CHANGED = "SMARTHOUSE.POWER_SUPPLY_CHANGED";

    public static final String BROADCAST_INTENT_UPDATELOG = "SMARTHOTEL.LOG.UPDATE";

    public static final String NOTIFICATION_NO_CONNECTION = "Error: no connection to the server";

    public static final String SERVICE_PASSWORD = "924";
}