package com.isosystems.smartmaid.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

import com.isosystems.smartmaid.R;

public class SoundMessages {

	Handler handler;
	MediaPlayer player;

	boolean enable_sound = true;
	
	public SoundMessages (Context c) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(c.getApplicationContext());

		//enable_sound = prefs.getBoolean("use_sound_signal", false);
		String s = prefs.getString("signals_list","0");

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

		player = MediaPlayer.create(c.getApplicationContext(), soundResource);
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				player.start();
			}
		};
	}

	public void setEnable_sound(boolean enable) {
		this.enable_sound = enable;
	}

	public void setPlayerSound(Context context, int id) {
		player = MediaPlayer.create(context.getApplicationContext(), id);
	}

	public void playAlarmSound() {
		if (enable_sound) {
			handler.sendEmptyMessage(0);
		}
	}

}