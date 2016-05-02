package com.ysh.phoneassistant.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ysh.phoneassistant.service.CheckSimInfoService;

public class BootCompleteReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent i = new Intent(context, CheckSimInfoService.class);
		context.startService(i);
	}
}
