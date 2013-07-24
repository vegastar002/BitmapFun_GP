package com.example.ui;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Hashtable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import cn.waps.AppConnect;

import com.android.hardcore.crashreport.CrashReportingApplication;
import com.example.Utils;
import com.example.cache.DiskCache;
import com.free.hardcore.wp9.R;
import com.inmobi.androidsdk.IMAdInterstitial;
import com.inmobi.androidsdk.IMAdInterstitialListener;
import com.inmobi.androidsdk.IMAdListener;
import com.inmobi.androidsdk.IMAdRequest;
import com.inmobi.androidsdk.IMAdRequest.ErrorCode;
import com.inmobi.androidsdk.IMAdView;
import com.pad.android.iappad.AdController;
import com.szy.update.UpdateManager;
import com.tapjoy.TapjoyAwardPointsNotifier;
import com.tapjoy.TapjoyConnect;
import com.tapjoy.TapjoyConnectFlag;
import com.tapjoy.TapjoyDisplayAdNotifier;
import com.tapjoy.TapjoyEarnedPointsNotifier;
import com.tapjoy.TapjoyFullScreenAdNotifier;
import com.tapjoy.TapjoyNotifier;
import com.tapjoy.TapjoySpendPointsNotifier;
import com.tapjoy.TapjoyVideoNotifier;

public class MoreActivity extends Activity implements Callback,
TapjoyNotifier, TapjoyFullScreenAdNotifier, TapjoySpendPointsNotifier, TapjoyDisplayAdNotifier, TapjoyAwardPointsNotifier, TapjoyEarnedPointsNotifier, TapjoyVideoNotifier{
	TextView mTitleView;
	
	private static final int APP_CLEAR_CACHE = 0;
	private static final int APP_GOOD_RATE = 1;
	private static final int APP_FEED_BACK = 2;
	private static final int APP_RECOMM_APP = 3;
	private static final int APP_SHARE_APP = 4;
	private static final int APP_VER_INFO = 5;
	private static final int APP_ABOUT_APP = 6;
	
	private static final int HANDLER_MSG_1 = 1;
	
	private ListView melistview;
	Button category_add_category;
	LinearLayout adLinearLayout;
	View adView;
	ImageButton category_finish;
	Handler mHandler;
	
	private IMAdView mIMAdView;
	private IMAdInterstitial mIMAdInterstitial;
	private IMAdRequest mAdRequest;
	
	AdController myController;
	UpdateManager manager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.more_activity);
		prepareView();
		mTitleView.setText(R.string.category_more);
		mHandler = new Handler(this);
		
		//广告
		Hashtable<String, String> flags = new Hashtable<String, String>();
		flags.put(TapjoyConnectFlag.ENABLE_LOGGING, "false");
		TapjoyConnect.requestTapjoyConnect(getApplicationContext(), "8b084d4d-6c47-43db-9690-5ddfa6e74ea5", "mxqcDoBW6s4pG5zJ2QTQ", flags);
		TapjoyConnect.getTapjoyConnectInstance().setEarnedPointsNotifier(this);
		
//		myController = new AdController(this, "383276956");
//		myController.setAdditionalDockingMargin(50);
//		myController.loadAd();
		
//		mIMAdView = (IMAdView) findViewById(R.id.imAdview_more);
//		mAdRequest = new IMAdRequest();
//		mAdRequest.setTestMode(false);
//		mIMAdView.setIMAdRequest(mAdRequest);
//		mIMAdView.loadNewAd(mAdRequest);
//		mIMAdView.setIMAdListener(mIMAdListener);
		
		mIMAdInterstitial = new IMAdInterstitial(this,"b3984fc8084b45788bb3e3feae329cc6");
		mIMAdInterstitial.setIMAdInterstitialListener(mIMAdInListener);
//		mIMAdView.loadNewAd();
		
		category_add_category = (Button) findViewById(R.id.category_add_category);
		category_add_category.setOnClickListener(mBackListener);
		category_add_category.setVisibility(View.GONE);
		
		String [] names = {
				getApplication().getResources().getString(R.string.more_clear),
				getApplication().getResources().getString(R.string.more_rate),
				getApplication().getResources().getString(R.string.more_feedback),
				getApplication().getResources().getString(R.string.more_recommend),
				getApplication().getResources().getString(R.string.more_share),
				getApplication().getResources().getString(R.string.more_version),
				getApplication().getResources().getString(R.string.more_about),
			};
		
		
		melistview = (ListView) findViewById(R.id.melistview);
		category_finish = (ImageButton) findViewById(R.id.category_finish);
		category_finish.setVisibility(View.GONE);
		
		melistview.setAdapter(new ArrayAdapter<String>(this, R.layout.fav_item, R.id.fav_title, names));
		melistview.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
				case APP_CLEAR_CACHE:
//					Log.i("", "测试： "+getString(R.string.alertDialog_msg, "344535223445"));
					//计算cache目录大小
					String outputSize;
					String fts3 = Environment.getExternalStorageDirectory().getAbsolutePath() 
					+	File.separator + DiskCache.DISK_CACHE_DIR;
					try {
						long size = getFileSize(new File(fts3));
						float scale = (float)size / (1024*1024);
						if ( scale < 1 ){
							scale = scale * 1000;
							DecimalFormat fnum = new DecimalFormat("##0.00");  
							String dd=fnum.format(scale);
							outputSize = dd +"KB";
//							Log.i("", "path: " + fts3 +"  cache目录大小： "+ dd +"KB");
						}else {
							DecimalFormat fnum = new DecimalFormat("##0.00");  
							String dd=fnum.format(scale);
							outputSize = dd +"MB";
//							Log.i("", "path: " + fts3 +"  cache目录大小： "+ dd +"MB");
						}
						
						handlerClear_Cache(outputSize);
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case APP_GOOD_RATE:
					openMarketDetails(MoreActivity.this, getPackageName());
					break;
				case APP_FEED_BACK:
					Intent data=new Intent(Intent.ACTION_SENDTO);  
			        data.setData(Uri.parse("mailto:energon8809@gmail.com"));  
			        data.putExtra(Intent.EXTRA_SUBJECT, "feedback for something");  
			        data.putExtra(Intent.EXTRA_TEXT, "");  
			        startActivity(data);
					break;
				case APP_RECOMM_APP:
//					TapjoyConnect.getTapjoyConnectInstance().showOffers();
					AppConnect.getInstance(MoreActivity.this);
					AppConnect.getInstance(MoreActivity.this).showAppOffers(MoreActivity.this);
					break;
				case APP_SHARE_APP:
					Intent intent = new Intent(Intent.ACTION_SEND);
					intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_subject));
					intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_msg, " http://play.google.com/store/apps/details?id=" + getPackageName())   );
					intent.setType("text/*");
					startActivity(Intent.createChooser(intent, "Share Using"));
					break;
				case APP_VER_INFO:
					manager = new UpdateManager(MoreActivity.this);
					
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							// 检查软件更新
							try {
								if ( manager.isUpdate(Utils.getPicFromOpenWebsite2()) ){
									Message msg = new Message();
									msg.what = HANDLER_MSG_1;
									mHandler.sendMessage(msg);
									
								}else {
									int vs = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
								}
							} catch (NotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
					}).start();
					
					break;
				case APP_ABOUT_APP:
//					Log.i("", "跳转about");
					Intent mIntent = new Intent(MoreActivity.this, aboutActivity.class);
					startActivity(mIntent);
					break;
				default:
					break;
				}
			}
		});
	}
	
	private OnClickListener mBackListener = new OnClickListener() {
        
		public void onClick(View v) {
			finish();
		}
        
    };
    
	public long getFileSize(File f) throws Exception {
		long size = 0;
		File flist[] = f.listFiles();
		for (int i = 0; i < flist.length; i++) {
			if (flist[i].isDirectory()) {
				size = size + getFileSize(flist[i]);
			} else {
				size = size + flist[i].length();
			}
		}
		return size;
	}
	
	public void handlerClear_Cache(String outputSize){
		new AlertDialog.Builder(MoreActivity.this)
				.setTitle(android.R.string.dialog_alert_title)
//				.setIcon(android.R.drawable.ic_menu_delete)
				.setMessage(getString(R.string.alertDialog_msg, outputSize))
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,	int which) {
							}
						})
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,	int which) {
								String s1Path = Environment.getExternalStorageDirectory().getAbsolutePath() +
										File.separator + DiskCache.DISK_CACHE_DIR;
								deleteAllFile(s1Path);
								Toast.makeText(getApplicationContext(), getString(R.string.alertDialog_cleaned),
									     Toast.LENGTH_LONG).show();
							}
						}).show();
	}
	
	public void deleteAllFile(String s1Path){
		File file = new File(s1Path);
		if (file.exists()) {
			File[] fileList = file.listFiles();
			if (fileList != null) {
				int len = fileList.length;
				for (int i = 0; i < len; ++i) {
					int posAsset = -1;
					if (fileList[i].isDirectory()) {
						// deleteDir(list[i].getPath()) ;
					} else {
						if (posAsset == -1) {
							fileList[i].delete();
						}
					}
				}
			}
		}
	}

	private void prepareView() {
		mTitleView = (TextView) findViewById(R.id.title_text);
	}
	
	public static void openMarketDetails(final Context context,	String packageName) {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("market://details?id=" + packageName));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		} catch (final ActivityNotFoundException e) {
			e.printStackTrace();
		}
	}



	@Override
	public void videoComplete() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void videoError(int arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void videoStart() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void earnedTapPoints(int arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void getAwardPointsResponse(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void getAwardPointsResponseFailed(String arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void getDisplayAdResponse(View arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void getDisplayAdResponseFailed(String arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void getSpendPointsResponse(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void getSpendPointsResponseFailed(String arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void getFullScreenAdResponse() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void getFullScreenAdResponseFailed(int arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void getUpdatePoints(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void getUpdatePointsFailed(String arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
	private IMAdListener mIMAdListener = new IMAdListener() {

		@Override
		public void onShowAdScreen(IMAdView adView) {

		}

		@Override
		public void onDismissAdScreen(IMAdView adView) {
		}

		@Override
		public void onAdRequestFailed(IMAdView adView, ErrorCode errorCode) {
		}

		@Override
		public void onAdRequestCompleted(IMAdView adView) {
		}
		
		@Override
		public void onLeaveApplication(IMAdView adView) {
		}
	};

	private IMAdInterstitialListener mIMAdInListener = new IMAdInterstitialListener() {

		@Override
		public void onShowAdScreen(IMAdInterstitial adInterstitial) {
		}

		@Override
		public void onDismissAdScreen(IMAdInterstitial adInterstitial) {

		}

		@Override
		public void onAdRequestFailed(IMAdInterstitial adInterstitial,
				ErrorCode errorCode) {
		}

		@Override
		public void onAdRequestLoaded(IMAdInterstitial adInterstitial) {
		}
		
		@Override
		public void onLeaveApplication(IMAdInterstitial adInterstitial) {
		}
	};

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case HANDLER_MSG_1:
			manager.showNoticeDialog();
			break;

		default:
			break;
		}
		return false;
	}


}
