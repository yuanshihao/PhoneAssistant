package com.ysh.phoneassistant.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import com.ysh.phoneassistant.R;

public class AlertService extends Service {

	private MediaPlayer player;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(player == null || !player.isPlaying()){
			player = MediaPlayer.create(this, R.raw.alarm);
			player.setVolume(1.0f, 1.0f);
			player.setLooping(true);
			player.start();
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(player != null) {
			player.stop();
			player = null;
		}
	}
	
}









