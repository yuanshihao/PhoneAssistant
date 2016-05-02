package com.ysh.phoneassistant.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.View;

import com.ysh.phoneassistant.R;

public class AlertOpenAdminDialog extends Dialog {

	public AlertOpenAdminDialog(Context context) {
		super(context, R.style.MyDialog);
		
		View view = View.inflate(context, R.layout.dialog_alert_admin, null);
		setCanceledOnTouchOutside(false);
		setContentView(view);
	}
}
