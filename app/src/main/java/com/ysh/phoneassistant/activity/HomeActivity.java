package com.ysh.phoneassistant.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ysh.phoneassistant.R;
import com.ysh.phoneassistant.ui.EnterPwdDialog;
import com.ysh.phoneassistant.ui.SetPwdDialog;
import com.ysh.phoneassistant.utils.MD5Utils;

public class HomeActivity extends Activity {

	private SharedPreferences sp;
	private Editor edit;
	private EditText et_pwd;
	private EditText et_confirmPwd;
	private Button bt_ok;
	private Button bt_cancel;
	private Animation anim;
	private Vibrator vibrator;
	private Dialog dialog;
	private TextView tv_antitheft;
	private TextView tv_phoneInfo;
	private String password;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		sp = getSharedPreferences("config", MODE_PRIVATE);
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		tv_antitheft = (TextView) findViewById(R.id.tv_antitheft);
		tv_phoneInfo = (TextView) findViewById(R.id.tv_phoneInfo);

		tv_antitheft.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				password = sp.getString("password", null);
				if (TextUtils.isEmpty(password)) {
					showSetPwdDialog();
				} else {
					showEnterDialog();
				}
			}
		});

		tv_phoneInfo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

			}
		});
	}

	private void showEnterDialog() {
		dialog = new EnterPwdDialog(this);
		bt_ok = (Button) dialog.findViewById(R.id.bt_ok);
		et_pwd = (EditText) dialog.findViewById(R.id.et_pwd);
		bt_cancel = (Button) dialog.findViewById(R.id.bt_cancel);

		bt_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String password = et_pwd.getText().toString().trim();
				String password2 = sp.getString("password", null);

				if (TextUtils.isEmpty(password)) {
					animAndVibrator(et_pwd);
				} else if (MD5Utils.md5(password).equals(password2)) {
					dialog.dismiss();
					Intent intent = new Intent(HomeActivity.this, AntitheftActivity.class);
					startActivity(intent);
				} else {
					Toast.makeText(HomeActivity.this, "密码不正确", Toast.LENGTH_SHORT).show();
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

	public void showSetPwdDialog() {

		dialog = new SetPwdDialog(this);

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
					Toast.makeText(HomeActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
					dialog.dismiss();
					Intent intent = new Intent(HomeActivity.this, AntitheftActivity.class);
					startActivity(intent);
				} else {
					Toast.makeText(HomeActivity.this, "两次密码不一致", Toast.LENGTH_SHORT).show();
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
		anim = AnimationUtils.loadAnimation(HomeActivity.this, R.anim.shake);
		et.startAnimation(anim);
		vibrator.vibrate(70);
	}
}
