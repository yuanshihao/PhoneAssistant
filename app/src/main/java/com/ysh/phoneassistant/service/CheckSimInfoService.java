package com.ysh.phoneassistant.service;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.SmsManager;
import android.text.TextUtils;

public class CheckSimInfoService extends Service {

	private TelephonyManager tm;
	private SharedPreferences sp;
	private Editor edit;
	private PhoneStateListener listener;
	private String safeNumber;
	private String oldSimSerialNumber;
	private String newSimSerialNumber;
	private boolean sendSuccess = false;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		sp = getSharedPreferences("config", Context.MODE_PRIVATE);
		tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

		safeNumber = sp.getString("safeNumber", null);
		if (sp.getBoolean("toast", false)) {
			Intent intent = new Intent(this, ToastService.class);
			startService(intent);
		}

		if (TextUtils.isEmpty(safeNumber)) {
			stopSelf();
			return;
		}

		listener = new MyListener();
		tm.listen(listener, PhoneStateListener.LISTEN_SIGNAL_STRENGTH);
	}

	private class MyListener extends PhoneStateListener {

		@Override
		@Deprecated
		public void onSignalStrengthChanged(int asu) {
			synchronized (MyListener.class) {

				if (sendSuccess) {
					return;
				}

				if (asu >= 5 && asu < 99) {
					newSimSerialNumber = tm.getSimSerialNumber();
					oldSimSerialNumber = sp.getString("simSerialNumber", "");

					if (!oldSimSerialNumber.equals(newSimSerialNumber)) {
						sendSuccess = true;
						edit = sp.edit();
						edit.putString("safeNumber", safeNumber);
						edit.putString("simSerialNumber", newSimSerialNumber);
						edit.commit();
						tm.listen(listener, PhoneStateListener.LISTEN_NONE);
						tm = null;
						sendSms();
						
						Timer timer = new Timer();
						timer.schedule(new TimerTask() {
							@Override
							public void run() {
								stopSelf();
							}
						}, 20000);
					}
				}
			}
		}
	}
	
	public void sendSms() {
		SmsManager sm = SmsManager.getDefault();
		String msg = "来自手机助手: 您的sim卡变更了";
		sm.sendTextMessage(safeNumber, null, msg, null, null);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (tm != null) {
			tm.listen(listener, PhoneStateListener.LISTEN_NONE);
			tm = null;
			listener = null;
		}
	}
}
