package com.ysh.phoneassistant.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;

import com.ysh.phoneassistant.service.AlertService;
import com.ysh.phoneassistant.service.LocationService;
import com.ysh.phoneassistant.service.ToastService;
import com.ysh.phoneassistant.service.WipeDataService;

public class SMSReceiver extends BroadcastReceiver {

	private SharedPreferences sp;
	private String safeNumber;

	@Override
	public void onReceive(Context context, Intent intent) {

		Bundle bundle = intent.getExtras();

		if (bundle == null) {
			return;
		}

		sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		safeNumber = sp.getString("safeNumber", null);

		if (TextUtils.isEmpty(safeNumber)) {
			return;
		}

		Object[] objs = (Object[]) bundle.get("pdus");

		if (objs == null) {
			return;
		}

		for (Object obj : objs) {

			SmsMessage sms = SmsMessage.createFromPdu((byte[]) obj);
			String sender = sms.getOriginatingAddress();
			String body = sms.getMessageBody();

			if (sms == null || body == null || sender == null) {
				return;
			}

			if (sender.contains(safeNumber)) {

				String[] total = body.split("//");

				if (body.contains("dingwei*#")) {

					Intent i = new Intent(context, LocationService.class);
					context.startService(i);
					abortBroadcast();
					return;

				} else if (body.contains("jingbao*#")) {

					Intent i = new Intent(context, AlertService.class);
					context.startService(i);
					abortBroadcast();
					return;

				} else if (body.contains("xiaohui*#")) {

					Intent i = new Intent(context, WipeDataService.class);
					i.putExtra("safeNumber", safeNumber);
					context.startService(i);
					abortBroadcast();
					return;

				} else if (body.contains("dongjie*#")) {

					if (total.length > 1) {
						body = total[1];
					} else {
						body = "";
					}

					Intent i = new Intent(context, ToastService.class);
					i.putExtra("msg", body);
					context.startService(i);
					abortBroadcast();
					return;
				}
			}
		}
	}
}
