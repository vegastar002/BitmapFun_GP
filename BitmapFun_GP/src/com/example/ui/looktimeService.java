package com.example.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.example.Utils;
import com.free.hardcore.wp9.R;
import com.szy.update.UpdateManager;

public class looktimeService extends Service {

	public CheckWavTask mCheckWavTask = null;
	public Timer timer = new Timer(true);
	
	private final static int HANDLER_MSG_CHECK_AND_DOWNLOAD = 1;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		mCheckWavTask = new CheckWavTask();
		timer.schedule(mCheckWavTask, 0, 3600*1000);//one hour
	}
	
	public class CheckWavTask extends TimerTask {
		 
		public void run() {
//			Log.i("", "服务已启动 "+ c.get(Calendar.HOUR_OF_DAY));
//			mCheckWavTask.cancel();
			//v2.1改
			Calendar c = Calendar.getInstance();//可以对每个时间域单独修改
			if ( c.get(Calendar.HOUR_OF_DAY) >= 16  &&  c.get(Calendar.HOUR_OF_DAY) <= 22 ){
				
				Message message = new Message();
	    	    message.what = HANDLER_MSG_CHECK_AND_DOWNLOAD;
	    	    mbHandler.sendMessage(message);
			}
		}
	}
	
	
	Handler mbHandler = new Handler(new Callback() {
		
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLER_MSG_CHECK_AND_DOWNLOAD:
				UpdateManager manager = new UpdateManager(looktimeService.this);
				// 检查软件更新
				try {
					if ( manager.isUpdate(Utils.getPicFromOpenWebsite2()) ){
//						Log.i("", "发现新版本");
						
						NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);               
						Notification notify = new Notification();
						notify.icon = R.drawable.new_version;
						notify.tickerText = getString(R.string.notifi_downtext, getString(R.string.app_name));
						notify.flags = Notification.FLAG_AUTO_CANCEL;   
						
						Intent intent = new Intent(looktimeService.this, notifyActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);           
						//PendingIntent
						PendingIntent contentIntent = PendingIntent.getActivity(
						        looktimeService.this, 
						        R.string.app_name, 
						        intent, 
						        PendingIntent.FLAG_ONE_SHOT);
						                 
						notify.setLatestEventInfo(
						        looktimeService.this,
						        getString(R.string.notifi_uptext), 
						        getString(R.string.notifi_downtext, getString(R.string.app_name)), 
						        contentIntent);
						nm.notify(R.string.app_name, notify);
						
						
					}else {
//						Log.i("", "没有发现新版本");
					}
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;

			default:
				break;
			}
			// TODO Auto-generated method stub
			return false;
		}
	});

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	

}
