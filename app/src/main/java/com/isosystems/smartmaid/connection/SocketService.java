package com.isosystems.smartmaid.connection;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SocketService extends Service {

    PrintWriter out;
    InputStream in;
    Socket socket;
    private final IBinder myBinder = new LocalBinder();

    static Handler mMessageHandler;
    StringBuilder mMessageBuffer;

    String mWifiName = "";
    String mWifiPassword = "";
    int mWifiReconnectTimeout = 10000;

    Boolean mSocketEndlessTimeout = true;

    String mSocketIp = "";
    int mSocketPort = 0;
    int mSocketTimeout = 10000;
    String mSocketGreetingsMessage = "";

    // Периодическая очистка буфера
    static Handler mBufferCleanHandler;

    static Handler mSocketAliveHandler;

    // Время после прихода последнего сообщения
    // которое должно пройти для очистки буфера
    int mBufferClearTimeout = 30000;

    Intent i;

    WifiManager mWifiManager;
    WifiConfiguration mWifiConfig;
    int mConfigID;

    @Override
    public IBinder onBind(Intent intent) {
        this.i = intent;
        return myBinder;
    }

    public class LocalBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mMessageBuffer = new StringBuilder();
        mMessageHandler = new Handler() {
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();

                // Сообщение
                String message = bundle.getString("message");

                        // Добавляем сообщение в буфер
                                        mMessageBuffer.append(message);

                // Поиск подстроки, которая начинается с @ или & или $
                // и заканчивается ¶
                Pattern p = Pattern.compile("[@&$#](.*?)¶");
                Matcher m = p.matcher(mMessageBuffer);
                while (m.find()) {
                    messageProcess(m.group());
                }

                // После отправки сообщения на обработку, оно удаляется из
                // буфера
                mMessageBuffer = new StringBuilder(m.replaceAll(""));
            } // handle message
        }; // handler

        mBufferCleanHandler = new Handler();
        mBufferCleanHandler.postDelayed(mBufferClearRunnable,
                mBufferClearTimeout);

        Runnable wifi_connect = new WifiConnect();
        new Thread(wifi_connect).start();
    }

    // Runnable для очистки буфера после N мсек.
    private Runnable mBufferClearRunnable = new Runnable() {
        public void run() {
            // Очистка буфера
            mMessageBuffer = new StringBuilder();

            mBufferCleanHandler.removeCallbacks(mBufferClearRunnable);
            mBufferCleanHandler.postDelayed(mBufferClearRunnable,
                    mBufferClearTimeout);
        }
    };


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

    /**
     * Переподключение к wi-fi
     */
    public void reconnectToWifi() {
        Runnable wifi_connect = new WifiConnect();
        new Thread(wifi_connect).start();
    }

    public boolean sendValue(int index, int value) {
        return sendMessage(String.valueOf(index + 100) + "," + String.valueOf(value));
    }

    public boolean sendMessage(String message) {
        try {
            out = new PrintWriter(new BufferedWriter
                    (new OutputStreamWriter(socket.getOutputStream())), true);
            out.print(message);
            out.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        this.i = intent;

        return START_NOT_STICKY;
    }

    class WifiConnect implements Runnable {
        @Override
        public void run() {

            try {
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(getApplicationContext().getApplicationContext());
                mWifiName = prefs.getString("wifi_name", "YAM-AP-00000002");
                mWifiPassword = prefs.getString("wifi_password", "12345678");
                mWifiReconnectTimeout = 10000;
                mSocketTimeout = 10000;
                mSocketIp = prefs.getString("socket_ip", "192.168.4.1");
                mSocketGreetingsMessage = prefs.getString("socket_message","");
                String s = prefs.getString("socket_port", "12345");
                try {
                    mSocketPort = Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            mWifiManager = (WifiManager) getSystemService(getApplicationContext().WIFI_SERVICE);

            List <WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
            for (int i=0;i<list.size();i++){
                mWifiManager.removeNetwork(list.get(i).networkId);
            }
            mWifiConfig = new WifiConfiguration();
            mWifiConfig.SSID = "\"" + mWifiName + "\"";
            mWifiConfig.preSharedKey = "\"" + mWifiPassword + "\"";
            mConfigID = mWifiManager.addNetwork(mWifiConfig);


            while (true) {
                if (mWifiManager.isWifiEnabled()) {
                    mWifiManager.disconnect();

                    list = mWifiManager.getConfiguredNetworks();
                    for (int i=0;i<list.size();i++){
                        mWifiManager.removeNetwork(list.get(i).networkId);
                    }
                    list = mWifiManager.getConfiguredNetworks();
                    mConfigID = mWifiManager.addNetwork(mWifiConfig);

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    ConnectivityManager connManager = (ConnectivityManager) getApplicationContext().getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                    while (!networkInfo.isConnected()) {

                        int nmb = 0;
                        list = mWifiManager.getConfiguredNetworks();
                        for (int i=0;i<list.size();i++){
                            if (list.get(i).networkId == mConfigID) nmb = i;
                        }

                        mWifiManager.disconnect();
                        mWifiManager.enableNetwork(mConfigID, true);
                        mWifiManager.reconnect();
                        try {
                            Thread.sleep(mWifiReconnectTimeout);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    }

                    try {
                        socket = new Socket(mSocketIp, mSocketPort);

                        if (!mSocketEndlessTimeout) {
                            socket.setSoTimeout(mSocketTimeout);
                        }

                        try {
                            out = new PrintWriter(new BufferedWriter
                                    (new OutputStreamWriter(socket.getOutputStream())), true);
                            in = socket.getInputStream();

                            if (out != null && !out.checkError()) {
                                out.print(mSocketGreetingsMessage);
                                out.flush();
                            } else {
                            }

                            Intent i = new Intent();
                            i.setAction(ConnectionManager.WIFI_CONNECTED);
                            getApplicationContext().sendBroadcast(i);

                            SocketReceive receive = new SocketReceive();
                            AsyncTask<Void, String, Void> receiveTask = receive.execute();

                            while (receiveTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
                            }

                            out.close();
                            in.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    /**
     * Пытаемся закрыть сокет
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        socket = null;
    }

    /**
     * Получение данных
     */
    class SocketReceive extends AsyncTask<Void, String, Void> {
        protected void onProgressUpdate(String... params) {
            // Если пришло сообщение - сброс тайм-аута очистки буфера
            mBufferCleanHandler.removeCallbacks(mBufferClearRunnable);
            mBufferCleanHandler.postDelayed(mBufferClearRunnable,
                    mBufferClearTimeout);

            Bundle bundle = new Bundle();
            bundle.putString("message", params[0]);

            Message msg = new Message();
            msg.setData(bundle);
            mMessageHandler.sendMessage(msg);
        }

        /**
         * Считываем количество байт из pipeline
         * Пока не пришло -1 или асинтаск не закрыт, считываем сообщение
         *
         * @param params
         * @return
         */
        @Override
        protected Void doInBackground(Void... params) {
            try {
                int bytesRead = 0;
                byte[] mData = new byte[4096];

                while ((bytesRead = in.read(mData)) >= 0 && !isCancelled()) {
                    if (bytesRead > 0) {
                        String mReceiveMessage = new String(mData, 0, bytesRead, Charset.forName("windows-1251"));
                        publishProgress(mReceiveMessage);
                    }
                }
            } catch (Exception e) {
                Intent i = new Intent();
                i.setAction(ConnectionManager.WIFI_DISCONNECTED);
                getApplicationContext().sendBroadcast(i);
                e.printStackTrace();
            }
            return null;
        }
    }

}
