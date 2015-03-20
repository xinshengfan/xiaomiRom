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
	private MediaPlayer player; // ��������
	private boolean isPause;

	// ����һ���ڲ��㲥�����������ڽ��չ㲥
	private class InnerReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// ȡ��action
			String action = intent.getAction();
			CLog.i("info", "Service���յĹ㲥��" + action);
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
		// �Ƿ�����ͣ״̬
		CLog.i("info", "��������");
		if (isPause) {
			player.start();
		} else {
			try {
				// File file = new File(new FileManager().getOtherPath(this)
				// + MyXiaomiApp.MUSICNAME);
				// CLog.i("info", "�����Ƿ���ڣ�" + (file.exists()));
				player.reset();
				player.setDataSource(new FileManager().getOtherPath(this)
						+ MyXiaomiApp.MUSICNAME);
				player.prepare();
				player.start();
				// ����һ���㲥����ǰ�����ѱ�
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
		CLog.i("info", "Service����");
		super.onCreate();
		// ��ʼ����ر���
		isPause = false;
		player = new MediaPlayer();
		player.setLooping(true);// ѭ������
		player.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				next();
			}
		});

		// ע��ù㲥��
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
		CLog.i("info", "����MusicPlayService");
		// ���ٲ�����
		player.release();
		// ȡ���㲥ע�ᡡ
		unregisterReceiver(receiver);
		// ���ٹ����߳�
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
