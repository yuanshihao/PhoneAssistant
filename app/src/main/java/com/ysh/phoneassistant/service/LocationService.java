package com.ysh.phoneassistant.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

public class LocationService extends Service {

	private String safeNumber;
	private SharedPreferences sp;

	private LocationManager gpsLocationManager;
	private LocationManager networkLocationManager;
	private LocationListener gpsListener;
	private LocationListener networkListener;

	private static boolean locationOK = false;

	private InputStream in;
	private ModifyOffset offset = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		sp = getSharedPreferences("config", MODE_PRIVATE);
		safeNumber = sp.getString("safeNumber", null);

		if (TextUtils.isEmpty(safeNumber)) {
			stopSelf();
		}
		setLocationListener();
	}

	private class MyLocationListener implements LocationListener {

		@Override
		public synchronized void onLocationChanged(Location location) {
			try {
				if (locationOK) {
					return;
				}
				locationOK = true;
				removeUpdates();

				float speed = location.getSpeed();
				double latitude = location.getLatitude();
				double longitude = location.getLongitude();
				String provider = location.getProvider();
				String accuracy = String.valueOf(location.getAccuracy());

				PointDouble point = convertCoordinate(longitude, latitude);
				if (point != null) {
					longitude = point.x;
					latitude = point.y;
				}

				long time = location.getTime();
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String localeTime = formatter.format(time);

				StringBuffer buff = new StringBuffer();
				buff.append("t: ");
				buff.append(localeTime);
				buff.append("\np: ");
				buff.append(provider);
				buff.append("\nj: ");
				buff.append(longitude);
				buff.append("\nw: ");
				buff.append(latitude);
				buff.append("\na: ");
				buff.append(accuracy);
				buff.append("\ns: ");
				buff.append(speed + " m/s");

				SmsManager sm = SmsManager.getDefault();
				sm.sendTextMessage(safeNumber, null, buff.toString(), null, null);

				new Timer().schedule(new TimerTask() {

					@Override
					public void run() {
						onDestroy();
					}
				}, 30000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onProviderDisabled(String provider) {
		}
	}

	public void setLocationListener() {

		try {
			if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
					|| ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

				gpsListener = new MyLocationListener();
				gpsLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
				gpsLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 30, gpsListener);

				networkListener = new MyLocationListener();
				networkLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
				networkLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 30, networkListener);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public PointDouble convertCoordinate(double longitude, double latitude) {

		PointDouble point = null;
		try {
			in = getAssets().open("axisoffset.dat");
			offset = ModifyOffset.getInstance(in);
			point = offset.s2c(new PointDouble(longitude, latitude));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
					in = null;
				}
				offset = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return point;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		removeUpdates();
	}

	public void removeUpdates() {
		if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
				|| ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

			if (networkLocationManager != null && networkListener != null) {

				networkLocationManager.removeUpdates(networkListener);
				networkListener = null;
				networkLocationManager = null;
			}

			if (gpsLocationManager != null && gpsListener != null) {
				gpsLocationManager.removeUpdates(gpsListener);
				gpsListener = null;
				gpsLocationManager = null;
			}
		}
		offset = null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			if (intent != null) {
				setLocationListener();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return START_STICKY;
	}
}
