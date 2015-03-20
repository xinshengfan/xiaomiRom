package com.codoon.xiaomiupdata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import cn.com.smartdevices.bracelet.BraceletApp;
import cn.com.smartdevices.bracelet.Keeper;

import com.codoon.xiaomiupdata.netUtils.AccessoryInfo;
import com.codoon.xiaomiupdata.netUtils.HandlerException;
import com.codoon.xiaomiupdata.xmlUtil.Perference;
import com.communication.ble.CodoonXiaomiDeviceUpgradeManager;
import com.communication.data.CLog;
import com.communication.data.FileManager;
import com.communication.ui.parser.LeDeviceListAdapter;

/**
 * 小米-->cod_mi 文件名: Mili.fw;即通过更改名字的方法让小米app刷入修改后的固件;<br>
 * cod_mi-->cod_mi_x 文件名: COD_MI.bin ; 即在以后升级中持续更新<br>
 * cod_mi-->小米 文件名:Mili_back.fw; 即用codoon协议刷回小米的Rom;恢复原来状态<br>
 * 
 * @author FANFAN
 * 
 */
public class MyXiaomiApp extends BraceletApp {
	public static MyXiaomiApp instance;
	public static final String MUSICNAME = "huafangguliang.mp3";
	public static final String CODOONFILENAME = "Codoon_upgrade.bin";
	public static final String XIAOMIFILENAME = "Mili_back.fw";
	private FileManager fileManager;
	private String productId;
	private BluetoothAdapter mBluetoothAdapter;
	public static ArrayList<Activity> activities;

	private int currentModel;
	public static final int UPGRADE_CODOON = 1;
	public static final int UPGRADE_BACK_XIAOMI = 2;
	public static final int UPGRADE_XIAOMI_CODOON = 3;

	private BluetoothDevice currentDevice;
	private LeDeviceListAdapter currentLeDeviceListAdapter;

	private CodoonXiaomiDeviceUpgradeManager mUpgradeManager;
	private boolean isCodoonCboot = false;
	private AccessoryInfo currentInfo;
	private Perference pf;
	private boolean isHadHintUpgrade; // 是否已经提示过更新

	public static boolean isUpgradeCodMi = true; // 是否成功刷机成功标志

	public boolean isHadHintUpgrade() {
		return isHadHintUpgrade;
	}

	public void setHadHintUpgrade(boolean isHadHintUpgrade) {
		this.isHadHintUpgrade = isHadHintUpgrade;
	}

	public AccessoryInfo getCurrentInfo() {
		return currentInfo;
	}

	public void setCurrentInfo(AccessoryInfo currentInfo) {
		this.currentInfo = currentInfo;
	}

	public boolean isCodoonCboot() {
		return isCodoonCboot;
	}

	public void setCodoonCboot(boolean isCodoonCboot) {
		this.isCodoonCboot = isCodoonCboot;
	}

	public CodoonXiaomiDeviceUpgradeManager getmUpgradeManager() {
		return mUpgradeManager;
	}

	public void setmUpgradeManager(
			CodoonXiaomiDeviceUpgradeManager mUpgradeManager) {
		this.mUpgradeManager = mUpgradeManager;
	}

	public BluetoothDevice getCurrentDevice() {
		return currentDevice;
	}

	public void setCurrentDevice(BluetoothDevice currentDevice) {
		this.currentDevice = currentDevice;
	}

	public LeDeviceListAdapter getCurrentLeDeviceListAdapter() {
		return currentLeDeviceListAdapter;
	}

	public void setCurrentLeDeviceListAdapter(
			LeDeviceListAdapter currentLeDeviceListAdapter) {
		this.currentLeDeviceListAdapter = currentLeDeviceListAdapter;
	}

	public int getCurrentModel() {
		return currentModel;
	}

	public void setCurrentModel(int currentModel) {
		this.currentModel = currentModel;
	}

	public BluetoothAdapter getmBluetoothAdapter() {
		return mBluetoothAdapter;
	}

	public void setmBluetoothAdapter(BluetoothAdapter mBluetoothAdapter) {
		this.mBluetoothAdapter = mBluetoothAdapter;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getProductId() {
		return productId;
	}

	public void addAcitivty(Activity activity) {
		activities.add(activity);
	}

	/**
	 * 完全退出
	 */
	public void exitAll() {
		for (Activity activity : activities) {
			activity.finish();
		}
		System.exit(0);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		fileManager = new FileManager();
		activities = new ArrayList<Activity>();
		instance = this;
		isHadHintUpgrade = false;
		pf = new Perference(this);
		// 日志开关，发布时要设为false
		CLog.isDebug = true;
		Keeper.keepDebugFlag(false);
		copyFile();
		// readProductId();
		// 全局异常信息监控初始化
		HandlerException handlerException = HandlerException.getInstance();
		handlerException.initVariable(this);
	}

	public void copyFile() {
		// 复制文件
		if (pf.isFirstUsed()) {
			CLog.i("info", "第一次安装，则先删除");
			deleteFileFirst(fileManager.getOtherPath(this), CODOONFILENAME);
			deleteFileFirst(fileManager.getOtherPath(this), MUSICNAME);
			deleteFileFirst(fileManager.getOtherPath(this), XIAOMIFILENAME);
		}
		copyFileToSD(fileManager.getOtherPath(this), CODOONFILENAME);
		copyFileToSD(fileManager.getOtherPath(this), MUSICNAME);
		// 升级后要不要把以前的文件删除？
		copyFileToSD(fileManager.getOtherPath(this), XIAOMIFILENAME);
	}

	private void deleteFileFirst(String otherPath, String codoonfilename2) {
		File SDFile = new File(otherPath + codoonfilename2);
		if (SDFile.exists()) {
			CLog.i("info", "第一次安装，文件存在，删除" + codoonfilename2);
			SDFile.delete();
		}

	}

	private void copyFileToSD(String filepath2, String filename2) {
		File fileToSD = new File(filepath2 + filename2);
		if (!fileToSD.exists()) {
			CLog.i("info", "开始复制" + filename2);
			startCopyFile(fileToSD, filename2);
		} else {
			CLog.i("info", "文件" + fileToSD.getAbsolutePath() + " 已存在");
		}

	}

	private void startCopyFile(final File fileToSD, final String filename2) {
		new Thread() {
			public void run() {
				InputStream ins = null;
				FileOutputStream fos = null;
				try {
					fileToSD.createNewFile();
					ins = getResources().getAssets().open(filename2);
					fos = new FileOutputStream(fileToSD);
					byte[] data = new byte[8 * 1024];
					int len;
					while ((len = ins.read(data)) != -1) {
						fos.write(data, 0, len);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (ins == null || fos == null) {
							return;
						}
						ins.close();
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
		}.start();

	}

	public static MyXiaomiApp getInstance() {
		if (null == instance) {
			instance = new MyXiaomiApp();
		}
		return instance;
	}

	public void finished() {
		CLog.i("info", "结束小米蓝牙服务");
		// Intent localIntent = new Intent();
		// localIntent
		// .setAction("com.xiaomi.hm.health.ACTION_DEVICE_UNBIND_APPLICATION");
		// sendBroadcast(localIntent);
		BraceletApp.BLEService.stopSelf();
		// BraceletApp.BLEService.onDestroy();
		// stopService(name)
	}

}
