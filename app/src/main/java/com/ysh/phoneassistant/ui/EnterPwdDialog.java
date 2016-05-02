package com.ysh.phoneassistant.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.View;

import com.ysh.phoneassistant.R;

public class EnterPwdDialog extends Dialog {

	public EnterPwdDialog(Context context) {
		super(context, R.style.MyDialog);
		
		View view = View.inflate(context, R.layout.dialog_enter_pwd, null);
		setCanceledOnTouchOutside(false);
		setContentView(view);
	}
}
