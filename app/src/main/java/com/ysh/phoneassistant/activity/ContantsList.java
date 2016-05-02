package com.ysh.phoneassistant.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ysh.phoneassistant.R;
import com.ysh.phoneassistant.domain.ContactsInfo;

import java.util.ArrayList;
import java.util.List;

public class ContantsList extends Activity {

	private ListView contactsList;
	private List<ContactsInfo> list;
	private TextView tv_contantsCount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		list = getContacts();
		int contantsCount;

		if (list != null && (contantsCount = list.size()) > 0) {

			setContentView(R.layout.activity_contactslist);
			tv_contantsCount = (TextView) findViewById(R.id.tv_contantsCount);
			tv_contantsCount.setText("共" + contantsCount + "人");

			contactsList = (ListView) findViewById(R.id.lv_contactsList);
			contactsList.setAdapter(new MyAdapter());
			contactsList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

					String phoneNum = list.get(position).getPhoneNum();

					Intent intent = new Intent();
					intent.putExtra("phoneNum", phoneNum);
					setResult(0, intent);
					finish();
				}
			});
		} else {
			setContentView(R.layout.activity_no_contacts);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		list = null;
		contactsList = null;
		tv_contantsCount = null;
	}

	private class MyAdapter extends BaseAdapter {

		private View view = null;
		private ViewHolder holder;

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				view = View.inflate(ContantsList.this, R.layout.contacslist_item_view, null);
				holder = new ViewHolder();
				holder.tv_name = (TextView) view.findViewById(R.id.tv_name);
				holder.tv_teleNumber = (TextView) view.findViewById(R.id.tv_PhoneNumber);
				view.setTag(holder);
			} else {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			}

			holder.tv_name.setText(list.get(position).getName());
			holder.tv_teleNumber.setText(list.get(position).getPhoneNum());

			return view;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
	}

	static class ViewHolder {
		TextView tv_name;
		TextView tv_teleNumber;
	}

	private List<ContactsInfo> getContacts() {

		Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
		Uri dataUri = Uri.parse("content://com.android.contacts/data");

		Cursor idCursor = getContentResolver().query(uri, new String[] { "_id" }, null, null, null);

		if (idCursor != null && idCursor.getCount() > 0) {

			list = new ArrayList<ContactsInfo>();

			while (idCursor.moveToNext()) {

				String id = idCursor.getString(0);

				if (id == null) {
					continue;
				}

				Cursor dataCursor = getContentResolver().query(dataUri, new String[] { "mimetype", "data1" }, "raw_contact_id = ?", new String[] { id }, null);

				if (dataCursor != null && dataCursor.getCount() > 0) {

					ContactsInfo info = new ContactsInfo();

					while (dataCursor.moveToNext()) {

						String mimeType = dataCursor.getString(0);
						String data = dataCursor.getString(1);

						if (data == null || data == "") {
							break;
						}

						if ("vnd.android.cursor.item/name".equals(mimeType)) {
							info.setName(data);
						} else if ("vnd.android.cursor.item/phone_v2".equals(mimeType)) {
							info.setPhoneNum(data);
						}
					}

					if (info.getName() != null && info.getPhoneNum() != null) {
						list.add(info);
					}
					dataCursor.close();
				}
			}
			idCursor.close();
		}
		return list;
	}
}
