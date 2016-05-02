package com.ysh.phoneassistant.service;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.text.TextUtils;

import com.ysh.phoneassistant.receiver.mAdminReceiver;

public class WipeDataService extends Service{

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String safeNumber = intent.getStringExtra("safeNumber");
		DevicePolicyManager devicePolicy = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
		ComponentName cname = new ComponentName(this, mAdminReceiver.class);
		try{
			if(devicePolicy.isAdminActive(cname)) {
				devicePolicy.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
			}else {
				if(!TextUtils.isEmpty(safeNumber)) {
					SmsManager sm = SmsManager.getDefault();
					sm.sendTextMessage(safeNumber, null, "很抱歉，您未激活设备管理器，无法远程销毁数据！", null, null);
					Timer timer = new Timer();
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							stopSelf();
						}
					}, 20000);
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
			devicePolicy.wipeData(0);
		}
		return super.onStartCommand(intent, flags, startId);
	}
}
