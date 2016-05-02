package com.ysh.phoneassistant.activity;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ysh.phoneassistant.R;
import com.ysh.phoneassistant.receiver.mAdminReceiver;
import com.ysh.phoneassistant.ui.AlertOpenAdminDialog;
import com.ysh.phoneassistant.ui.SetPwdDialog;
import com.ysh.phoneassistant.utils.MD5Utils;

public class AntitheftActivity extends Activity {

	private TextView bt_startBind;
	private EditText et_safeNumber;
	private SharedPreferences sp;
	private Editor edit;
	private TextView tv_bindInfo;
	private LinearLayout ll_edit;
	private ImageButton bt_selectContacts;

	private TelephonyManager tm;
	private LinearLayout ll_bindInfo;
	private Animation anim;
	private Vibrator vibrator;
	private AlertOpenAdminDialog alert;
	private Button bt_cancel;
	private Button bt_ok;

	private ComponentName cname;
	private DevicePolicyManager devicePolicy;
	private String safeNumber;


	private TextView tv_back;
	private TextView tv_setPwd;

	private SetPwdDialog dialog;
	private EditText et_pwd;
	private EditText et_confirmPwd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_antitheft);

		ll_edit = (LinearLayout) findViewById(R.id.ll_edit);
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		tv_bindInfo = (TextView) findViewById(R.id.tv_bindInfo);
		ll_bindInfo = (LinearLayout) findViewById(R.id.ll_bindInfo);
		bt_startBind = (TextView) findViewById(R.id.bt_startBind);
		et_safeNumber = (EditText) findViewById(R.id.et_safeNumber);
		bt_selectContacts = (ImageButton) findViewById(R.id.bt_selectContants);
		tv_back = (TextView) findViewById(R.id.tv_back);
		tv_setPwd = (TextView) findViewById(R.id.tv_setPwd);

		et_safeNumber.setFocusableInTouchMode(false);

		et_safeNumber.setFocusableInTouchMode(true);

		bt_startBind.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (ll_edit.isShown()) {
					startBinding();
				} else {
					removeBinding();
				}
			}
		});

		bt_selectContacts.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				vibrator.vibrate(30);
				Intent intent = new Intent(AntitheftActivity.this, ContantsList.class);
				startActivityForResult(intent, 0);
			}
		});

		tv_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				back();
			}
		});

		tv_setPwd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showSetPwdDialog();
			}
		});

	}

	public void showSetPwdDialog() {
		dialog = new SetPwdDialog(AntitheftActivity.this);

		bt_ok = (Button) dialog.findViewById(R.id.bt_ok);
		et_pwd = (EditText) dialog.findViewById(R.id.et_pwd);
		bt_cancel = (Button) dialog.findViewById(R.id.bt_cancel);
		et_confirmPwd = (EditText) dialog.findViewById(R.id.et_confirmPwd);

		bt_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String password = et_pwd.getText().toString().trim();
				String password2 = et_confirmPwd.getText().toString().trim();

				if (TextUtils.isEmpty(password)) {
					animAndVibrator(et_pwd);
				} else if (TextUtils.isEmpty(password2)) {
					animAndVibrator(et_confirmPwd);
				} else if (password.equals(password2)) {
					password = MD5Utils.md5(password);
					edit = sp.edit();
					edit.putString("password", password);
					edit.commit();
					Toast.makeText(AntitheftActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
					dialog.dismiss();
					Intent intent = new Intent(AntitheftActivity.this, AntitheftActivity.class);
					startActivity(intent);
				} else {
					Toast.makeText(AntitheftActivity.this, "两次密码不一致", Toast.LENGTH_SHORT).show();
				}
			}
		});

		bt_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	public void animAndVibrator(EditText et) {
		anim = AnimationUtils.loadAnimation(AntitheftActivity.this, R.anim.shake);
		et.startAnimation(anim);
		vibrator.vibrate(70);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		back();
	}

	public void back() {
		Intent i = new Intent(AntitheftActivity.this, HomeActivity.class);
		startActivity(i);
		overridePendingTransition(R.anim.tran_in, R.anim.tran_out);
		finish();
	}

	public void startBinding() {

		safeNumber = et_safeNumber.getText().toString().trim();

		if (TextUtils.isEmpty(safeNumber)) {
			AnimAndVibrate();
		} else if (safeNumber.length() < 11) {
			AnimAndVibrate();
			Toast.makeText(this, "请正确输入", Toast.LENGTH_SHORT).show();
		} else {
			if (!isAdminActived()) {
				alertOpenAdmin();
			} else {
				edit = sp.edit();
				edit.putString("safeNumber", safeNumber);
				edit.putString("simSerialNumber", getSimSerialNumber());
				edit.commit();
				setBindInfo(safeNumber);
			}
		}
	}

	public void alertOpenAdmin() {
		alert = new AlertOpenAdminDialog(this);
		bt_cancel = (Button) alert.findViewById(R.id.bt_cancel);
		bt_ok = (Button) alert.findViewById(R.id.bt_ok);

		bt_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				alert.dismiss();
				activateAdmin();
			}
		});

		bt_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				alert.dismiss();
			}
		});
		alert.show();
	}

	public void removeBinding() {
		edit = sp.edit();
		edit.putString("safeNumber", null);
		edit.putString("simSerialNumber", "");
		edit.commit();
		bt_startBind.setText("开启保护");
		ll_bindInfo.setVisibility(View.GONE);
		ll_edit.setVisibility(View.VISIBLE);
	}

	public void AnimAndVibrate() {
		anim = AnimationUtils.loadAnimation(this, R.anim.shake);
		et_safeNumber.startAnimation(anim);
		vibrator.vibrate(70);
	}

	public String getSimSerialNumber() {
		tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		return tm.getSimSerialNumber();
	}

	public void setBindInfo(String safeNumber) {
		ll_bindInfo.setVisibility(View.VISIBLE);
		tv_bindInfo.setText("安全号码: " + safeNumber);
		ll_edit.setVisibility(View.GONE);
		bt_startBind.setText("解除绑定");
	}

	public void activateAdmin() {
		cname = new ComponentName(AntitheftActivity.this, mAdminReceiver.class);
		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, cname);
		intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "激活设备管理器，使用远程锁屏，删除数据等高级功能");
		startActivity(intent);
	}

	public boolean isAdminActived() {
		devicePolicy = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
		cname = new ComponentName(AntitheftActivity.this, mAdminReceiver.class);
		if (devicePolicy.isAdminActive(cname)) {
			return true;
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (data == null) {
			return;
		}

		String phoneNum = data.getStringExtra("phoneNum");
		if (!TextUtils.isEmpty(phoneNum)) {
			phoneNum = phoneNum.replace("-", "").trim();
			et_safeNumber.setText(phoneNum);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (sp == null) {
			sp = getSharedPreferences("config", MODE_PRIVATE);
		}

		String safeNumber = sp.getString("safeNumber", null);

		if (!TextUtils.isEmpty(safeNumber) && isAdminActived()) {
			setBindInfo(safeNumber);
		}
	}
}
