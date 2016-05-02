package com.ysh.phoneassistant.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;

import com.ysh.phoneassistant.R;

public class ToastService extends Service {

	private View view;
	private WindowManager wm;
	private WindowManager.LayoutParams params;
	private SharedPreferences sp;
	private Editor edit;

	private String safeNumber;
	private MySmsReceiver receiver;
	private int flag;
	private TextView tv_msg;

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

		if (intent == null) {
			showToast("");
			System.out.println("null");
			IntentFilter filter = new IntentFilter();
			filter.setPriority(1000);
			filter.addAction("android.provider.Telephony.SMS_RECEIVED");
			receiver = new MySmsReceiver();
			registerReceiver(receiver, filter);
			return START_STICKY;
		}

		String msg = (String) intent.getStringExtra("msg");
		sp = getSharedPreferences("config", Context.MODE_PRIVATE);
		edit = sp.edit();

		if (!sp.getBoolean("toast", false)) {
			edit.putBoolean("toast", true);
			edit.commit();
		}

		if (!TextUtils.isEmpty(msg)) {

			if (msg.contains("CancelToast")) {
				edit.putBoolean("toast", false);
				edit.commit();
				msg = msg.replace("CancelToast", "");
				showToast(msg);
				return START_STICKY;
			}
			showToast(msg);
		} else {
			showToast("");
		}
		return START_STICKY;
	}

	public void showToast(String msg) {

		if (wm == null || view == null || params == null) {
			wm = (WindowManager) getSystemService(WINDOW_SERVICE);
			view = View.inflate(getApplicationContext(), R.layout.toast_view, null);
			tv_msg = (TextView) view.findViewById(R.id.tv_msg);
			params = new WindowManager.LayoutParams();

			view.setOnTouchListener(new OnTouchListener() {

				long[] hits = new long[6];
				int interval = 1000;
				double areaWidth;
				double areaheight;
				private float y;
				private float x;

				@Override
				public boolean onTouch(View v, MotionEvent event) {

					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:

						areaWidth = wm.getDefaultDisplay().getWidth() * 0.8;
						areaheight = wm.getDefaultDisplay().getHeight() * 0.2;
						x = event.getRawX();
						y = event.getRawY();

						if (x > areaWidth && y < areaheight) {
							System.arraycopy(hits, 1, hits, 0, hits.length - 1);
							hits[hits.length - 1] = SystemClock.uptimeMillis();
							if (hits[0] >= SystemClock.uptimeMillis() - interval) {
								wm.removeView(view);
								flag = 0;
							}
						}
						break;
					}
					return true;
				}
			});

			params.height = WindowManager.LayoutParams.MATCH_PARENT;
			params.width = WindowManager.LayoutParams.MATCH_PARENT;
			params.format = PixelFormat.TRANSPARENT;
			params.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;
			params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		}

		tv_msg.setText(msg);

		if (flag == 0) {
			wm.addView(view, params);
			flag = 1;
		} else {
			wm.updateViewLayout(view, params);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public class MySmsReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			Bundle bundle = intent.getExtras();

			if (bundle == null) {
				return;
			}

			sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
			safeNumber = sp.getString("safeNumber", null);

			Object[] objs = (Object[]) bundle.get("pdus");

			if (TextUtils.isEmpty(safeNumber) || objs == null) {
				return;
			}

			Intent i = null;

			for (Object obj : objs) {

				SmsMessage sms = SmsMessage.createFromPdu((byte[]) obj);
				String sender = sms.getOriginatingAddress();
				String body = sms.getMessageBody();

				if (sms == null || body == null || sender == null) {
					return;
				}

				if (sender.contains(safeNumber)) {

					String[] all = body.split("//");

					if (body.contains("dingwei*#")) {

						i = new Intent(context, AlertService.class);
						context.startService(i);
						abortBroadcast();
						i = null;
						return;

					} else if (body.contains("jingbao*#")) {

						i = new Intent(context, AlertService.class);
						context.startService(i);
						abortBroadcast();
						i = null;
						return;

					} else if (body.contains("shanchu*#")) {

						i = new Intent(context, AlertService.class);
						context.startService(i);
						abortBroadcast();
						i = null;
						return;

					} else if (body.contains("dongjie*#")) {

						if (all.length > 1) {
							body = all[1];
						} else {
							body = "";
						}

						i = new Intent(context, ToastService.class);
						i.putExtra("msg", body);
						context.startService(i);
						i = null;
						abortBroadcast();
						return;
					}
				}
			}
		}
	}
}
