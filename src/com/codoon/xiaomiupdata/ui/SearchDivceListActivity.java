package com.codoon.xiaomiupdata.ui;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.codoon.xiaomiupdata.MyXiaomiApp;
import com.codoon.xiaomiupdata.R;
import com.communication.data.CLog;
import com.communication.ui.entity.Constants;
import com.communication.ui.parser.LeDeviceListAdapter;

public class SearchDivceListActivity extends MyBaseActivity {
	private TextView tv_search_list_title;
	private ListView lv_showList;
	private int currentModel;
	private LeDeviceListAdapter mLeDeviceListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_scaned);
		intiData();
		intiView();
		addListener();
	}

	private void addListener() {
		lv_showList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				BluetoothDevice device = mLeDeviceListAdapter.getItem(position)
						.getDevice();
				judgeNameSkip(device);
			}
		});

	}

	protected void judgeNameSkip(BluetoothDevice device) {
		String deviceName = device.getName();
		switch (currentModel) {
		case MyXiaomiApp.UPGRADE_BACK_XIAOMI:
			if (!Constants.TYPE_COD_MI_NAME.equalsIgnoreCase(deviceName)) {
				Toast.makeText(SearchDivceListActivity.this,
						"当前模式还原小米ROM，不能升级其他手环", Toast.LENGTH_SHORT).show();
				return;
			}
			break;
		case MyXiaomiApp.UPGRADE_CODOON:
			if (!Constants.TYPE_CODOON_NAME.equalsIgnoreCase(deviceName)) {
				Toast.makeText(SearchDivceListActivity.this,
						"当前模式为升级咕咚手环，不能升级其他手环", Toast.LENGTH_SHORT).show();
				return;
			}
			break;
		default:
			break;
		}
		MyXiaomiApp.getInstance().setCurrentDevice(device);
		startActivity(new Intent(SearchDivceListActivity.this,
				BraceletConnectActivity.class));
		overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
		SearchDivceListActivity.this.finish();

	}

	private void intiData() {
		currentModel = MyXiaomiApp.getInstance().getCurrentModel();
		mLeDeviceListAdapter = MyXiaomiApp.getInstance()
				.getCurrentLeDeviceListAdapter();

	}

	private void intiView() {
		tv_search_list_title = (TextView) findViewById(R.id.tv_search_list_title);
		switch (currentModel) {
		case MyXiaomiApp.UPGRADE_BACK_XIAOMI:
			tv_search_list_title.setText(R.string.search_bracelet);
			break;
		case MyXiaomiApp.UPGRADE_CODOON:
			tv_search_list_title.setText(R.string.ui_search_codoon_title);
			break;
		default:
			break;
		}
		lv_showList = (ListView) findViewById(R.id.lv_search_device_list);
		if (mLeDeviceListAdapter != null) {
			lv_showList.setAdapter(mLeDeviceListAdapter);
		} else {
			CLog.i("info", "mLeDeviceListAdapter is null !");
		}
	}

	/**
	 * 重新搜索
	 * 
	 * @param v
	 */
	public void researchDevice(View v) {
		startActivity(new Intent(SearchDivceListActivity.this,
				SearchBraceleting.class));
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
		finish();
	}

	public void backnextView(View v) {
		onBackPressed();
	}

}
