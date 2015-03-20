package com.communication.ui.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.codoon.xiaomiupdata.R;
import com.communication.ui.entity.Constants;
import com.communication.ui.entity.MyBluetooth;

public class LeDeviceListAdapter extends BaseAdapter {
	private ArrayList<MyBluetooth> mLeDevices;
	private LayoutInflater inflater;

	// private Context context;

	// private AlertDialog.Builder dialog;

	public LeDeviceListAdapter(Context context) {
		// this.context = context;
		this.inflater = LayoutInflater.from(context);
		mLeDevices = new ArrayList<MyBluetooth>();
		// dialog = new Builder(context);
	}

	public void removeDevice(int position) {
		mLeDevices.remove(position);
	}

	public boolean isContainDeviceByAddress(MyBluetooth bluetooth) {
		return mLeDevices.contains(bluetooth);
	}

	/**
	 * 找到信号最强的设备
	 * 
	 * @return
	 */
	public MyBluetooth getMostRssiDevice() {
		MyBluetooth mostRssiDevice = mLeDevices.get(0);
		for (MyBluetooth bluetooth : mLeDevices) {
			if (bluetooth.getRssi() > mostRssiDevice.getRssi()) {
				mostRssiDevice = bluetooth;
			}
		}
		return mostRssiDevice;
	}

	/**
	 * 找到信号最强的CBoot设备
	 * 
	 * @return
	 */
	public MyBluetooth getMostRssiCBootDevice() {
		MyBluetooth mostRssiDevice = mLeDevices.get(0);
		for (MyBluetooth bluetooth : mLeDevices) {
			if (Constants.TYPE_CBOOT_NAME.equalsIgnoreCase((bluetooth
					.getDevice().getName()))) {
				if (bluetooth.getRssi() > mostRssiDevice.getRssi()) {
					mostRssiDevice = bluetooth;
				}
			}
		}
		return mostRssiDevice;
	}

	public void addDevice(MyBluetooth device) {
		if (null == device
				|| TextUtils.isEmpty(device.getDevice().getAddress())
				|| mLeDevices.contains(device)) {
			return;
		}
		// 对设备按信号排序
		Comparator<MyBluetooth> comparator = new Comparator<MyBluetooth>() {

			@Override
			public int compare(MyBluetooth lhs, MyBluetooth rhs) {
				// 信号为负值，加负号
				return -(lhs.getRssi() - rhs.getRssi());
			}

		};
		mLeDevices.add(device);
		Collections.sort(mLeDevices, comparator);
		notifyDataSetChanged();
	}

	public void setDevice(ArrayList<MyBluetooth> list) {
		if (null == list || list.size() <= 0) {
			return;
		}

		mLeDevices.clear();
		mLeDevices.addAll(list);

		notifyDataSetChanged();
	}

	public void clearDeviceList() {
		mLeDevices.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mLeDevices.size();
	}

	@Override
	public MyBluetooth getItem(int position) {
		return mLeDevices.get(position);
	}

	@Override
	public long getItemId(int positioni) {
		return positioni;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null || convertView.getTag() == null) {
			convertView = createView();
		}
		ViewHolder holder = (ViewHolder) convertView.getTag();

		final MyBluetooth device = mLeDevices.get(position);
		final String deviceName = device.getDevice().getName();
		if (!TextUtils.isEmpty(deviceName)) {
			holder.deviceName.setText(deviceName);
		} else {
			holder.deviceName.setText(R.string.unknown_device);
		}
		holder.tvCount.setText("设备" + (position + 1));
		holder.tvDeviceAddress.setText(device.getDevice().getAddress());

		return convertView;
	}

	private View createView() {
		View itemView = inflater.inflate(R.layout.vw_listitemxiaomi, null);

		if (itemView == null) {
			return null;
		}
		ViewHolder holder = new ViewHolder();

		holder.tvCount = (TextView) itemView.findViewById(R.id.tv_device_count);
		holder.deviceName = (TextView) itemView
				.findViewById(R.id.tv_device_name);
		// holder.btRepair = (Button)
		// itemView.findViewById(R.id.bt_list_upgrade);
		holder.tvDeviceAddress = (TextView) itemView
				.findViewById(R.id.tv_device_address);
		itemView.setTag(holder);

		return itemView;
	}

	static class ViewHolder {
		TextView tvCount;
		TextView deviceName;
		TextView tvDeviceAddress;
		// Button btRepair;
	}
}