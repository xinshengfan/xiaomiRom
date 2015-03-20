package com.communication.ui;

import java.io.IOException;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.IBinder;

import com.codoon.xiaomiupdata.MyXiaomiApp;
import com.communication.data.CLog;
import com.communication.data.FileManager;

public class MusicPlayService extends Service {

	private InnerReceiver receiver;
	private MediaPlayer player; // 播放器；
	private boolean isPause;

	// 创建一个内部广播接收器，用于接收广播
	private class InnerReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 取出action
			String action = intent.getAction();
			CLog.i("info", "Service接收的广播：" + action);
			if (G.ACTION_PLAY.equals(action)) {
				play();
			} else if (G.ACTION_PAUSE.equals(action)) {
				pause();
			} else if (G.ACTION_EXIT.equals(action)) {
				stopSelf();
			}
		}

	}

	public void play() {
		// 是否处于暂停状态
		CLog.i("info", "播放音乐");
		if (isPause) {
			player.start();
		} else {
			try {
				// File file = new File(new FileManager().getOtherPath(this)
				// + MyXiaomiApp.MUSICNAME);
				// CLog.i("info", "音乐是否存在？" + (file.exists()));
				player.reset();
				player.setDataSource(new FileManager().getOtherPath(this)
						+ MyXiaomiApp.MUSICNAME);
				player.prepare();
				player.start();
				// 发送一个广播，当前音乐已变
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void pause() {
		if (player.isPlaying()) {
			player.pause();
			isPause = true;
		}
	}

	public void next() {
		isPause = false;
		play();

	}

	@Override
	public void onCreate() {
		CLog.i("info", "Service创建");
		super.onCreate();
		// 初始化相关变量
		isPause = false;
		player = new MediaPlayer();
		player.setLooping(true);// 循环播放
		player.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				next();
			}
		});

		// 注册该广播　
		receiver = new InnerReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(G.ACTION_PLAY);
		filter.addAction(G.ACTION_EXIT);
		filter.addAction(G.ACTION_NEXT);
		filter.addAction(G.ACTION_PAUSE);
		filter.addAction(G.ACTION_PREVIOUS);
		filter.addAction(G.ACTION_MODE_CHANGED);
		filter.addAction(G.ACTION_SEEK_TO);
		filter.addAction(G.ACTION_NEW_MUSIC);
		filter.addAction(G.ACTION_WIDGET_ISPLAY);
		registerReceiver(receiver, filter);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		CLog.i("info", "结束MusicPlayService");
		// 销毁播放器
		player.release();
		// 取消广播注册　
		unregisterReceiver(receiver);
		// 销毁工作线程
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
