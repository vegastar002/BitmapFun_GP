package com.example.ui;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.jpush.android.api.InstrumentedActivity;
import cn.jpush.android.api.JPushInterface;

import com.aliyun.android.oss.OSSClient;
import com.aliyun.android.oss.model.OSSObjectSummary;
import com.aliyun.android.util.Pagination;
import com.android.hardcore.crashreport.CrashReportingApplication;
import com.example.Images;
import com.example.Utils;
import com.free.hardcore.wp9.R;
import com.grbmzbvus.vwbdizdju129465.Airpush;
import com.szy.update.UpdateManager;

public class SplashActivity extends InstrumentedActivity implements Runnable, Callback{
	
	private ImageView imageView;
	private AnimationDrawable animDrawable;
	TextView logotext, version;
	private Typeface font;
	List<OSSObjectSummary> mLegList = new ArrayList<OSSObjectSummary>();
	List<OSSObjectSummary> mPiguList = new ArrayList<OSSObjectSummary>();
	UpdateManager manager;
	Airpush airpush;
	public OSSClient mOSSClient;
	private static final String ACCESS_ID = "lMosX47lTyMFuYRk";
    private static final String ACCESS_KEY = "4juWEixwMSgEOnu4fpt79Ja4933W4d";
    private static final String BucketName = "bitmapfun";
    private static final String BucketName_Sub1 = "pigu";
    private static final String BucketName_Sub2 = "leg";
    Handler mHandler;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.splash_activity);
		font = Typeface.createFromAsset(getAssets(), "comic.ttf");
		manager = new UpdateManager(this);
		mHandler = new Handler(this);
		
		mOSSClient = new OSSClient();
		mOSSClient.setAccessId(ACCESS_ID);
		mOSSClient.setAccessKey(ACCESS_KEY);
		
		JPushInterface.setDebugMode(true);
		JPushInterface.init(this);
		
		imageView = (ImageView) findViewById(R.id.frameview);
		animDrawable=(AnimationDrawable) imageView.getBackground();
		logotext = (TextView) findViewById(R.id.logotext);
		logotext.setTypeface(font);
		version = (TextView) findViewById(R.id.version);
		
		try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
			version.setText("android v"+info.versionName);
		} catch (final NameNotFoundException e) {
			e.printStackTrace();
		}
		
		
		if ( isNetworkAvailable(SplashActivity.this) ){
//			Intent intent2 = new Intent(SplashActivity.this,looktimeService.class);
//			startService(intent2);
			
			airpush = new Airpush(SplashActivity.this, null);
			
		    new Thread(SplashActivity.this).start();
		}else {
			Toast.makeText(SplashActivity.this, getString(R.string.splash_disconnect), Toast.LENGTH_LONG).show();
			finish();
		}
		
	}
	public void onWindowFocusChanged(boolean hasFocus){
		animDrawable.start();
		
		super.onWindowFocusChanged(hasFocus);
	}
	
	public boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
		} else {
			// 如果仅仅是用来判断网络连接　　　　　　
			// 则可以使用 cm.getActiveNetworkInfo().isAvailable();
			NetworkInfo[] info = cm.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	
	
	
	long getWavTime = 0;
	public void run() {
		getWavTime = System.currentTimeMillis();
			
		try {
			fd();
			manager.isUpdate(Utils.getPicFromOpenWebsite2());
			String source = manager.mHashMap.get("source");
			if ( "aliyun".equals(source) ){
				getPicFromAliyun();
			}else {
				getPicFromOpenWebsite();
			}
			
			airpush.startPushNotification(false);
			airpush.startIconAd();
//			airpush.startSmartWallAd();
			
			animDrawable.stop();
			
			
			synchronized (this) {
				if ( (System.currentTimeMillis() - getWavTime) < 3000 ){
					wait(3000);
				}
			}
			
			Intent intent = new Intent();
			intent.setClass(SplashActivity.this, TabMainActivity.class);
			startActivity(intent);
			finish();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void getPicFromOpenWebsite(){
		List <String> mList3 = new ArrayList<String>();
		
		try {
			
			URL url = new URL(manager.mHashMap.get("sourcelink"));
			URLConnection conn = url.openConnection();
			InputStream inStream = conn.getInputStream();
			
			String data = "";
			StringBuffer str2 = new StringBuffer();
			InputStreamReader isr = new InputStreamReader(inStream); 
			BufferedReader br = new BufferedReader(isr);
			while ((data = br.readLine()) != null){
				str2.append(data);
		    }
			data = str2.toString().trim();
			data = data.replace("\n","");
			data = data.replace("\r","");
	        
	        JSONObject json = new JSONObject(data);
	        JSONArray jsonNode1 = json.getJSONArray("node");
	        for (int i = 0; i < jsonNode1.length(); i++) {
	        	JSONObject jsonObject1 = jsonNode1.getJSONObject(i);
	        	String idString = jsonObject1.getString("id");
	        	JSONArray jsonNode2 = json.getJSONArray(idString);
	        	
	        	for (int j = 0; j < jsonNode2.length(); j++) {
	            	JSONObject jsonObject2 = jsonNode2.getJSONObject(j);
	            	mList3.add(jsonObject2.getString("url"));
	            }
	        	
	        	if ( idString.contains("leg") ){
	        		Images.imageUrls_leg = mList3.toArray(new String[mList3.size()]);
	        		
	        	} else if ( idString.contains("pigu") ){
	        		Images.imageUrls = mList3.toArray(new String[mList3.size()]);
	        		
				} else if ( idString.contains("position") ){
	        		Images.imageUrls_position = mList3.toArray(new String[mList3.size()]);
	        		
				}
	        	
	        	
	        	mList3.clear();
	        }
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void getPicFromAliyun(){
		Pagination<OSSObjectSummary> mLegPagination = mOSSClient.viewFolder(
				BucketName, BucketName_Sub2,1000);
		Pagination<OSSObjectSummary> mPiguPagination = mOSSClient.viewFolder(
				BucketName, BucketName_Sub1,1000);
		
		while (true) {
			mLegList.addAll(mLegPagination.getContents());
			
			if ( mLegPagination.hasNext() ){
				mLegPagination = mLegPagination.next();
			}else {
				break;
			}
		}
		
		while (true) {
			mPiguList.addAll(mPiguPagination.getContents());
			
			if ( mPiguPagination.hasNext() ){
				mPiguPagination = mPiguPagination.next();
			}else {
				break;
			}
		}
		
		List <String> mList2 = new ArrayList<String>();
		for (int i = 0; i < mLegList.size(); i++) {
			if (!mLegList.get(i).isDirectory()) {
				String ts1 = "http://oss.aliyuncs.com/bitmapfun/" + mLegList.get(i).getKey();
				mList2.add(ts1);
			}
		}
		Images.imageUrls_leg = mList2.toArray(new String[mList2.size()]);
		
		/////////////////////////////////////////////////////////////////////////////
		
		List <String> mList4 = new ArrayList<String>();
		for (int i = 0; i < mPiguList.size(); i++) {
			if (!mPiguList.get(i).isDirectory()) {
				String ts1 = "http://oss.aliyuncs.com/bitmapfun/" + mPiguList.get(i).getKey();
				mList4.add(ts1);
			}
		}
		Images.imageUrls = mList4.toArray(new String[mList4.size()]);
	}
	
	public void fd(){
    	HttpPost httpRequest = new HttpPost("http://androidvote.duapp.com/CheckAccount");
    	String rpre = "";
    	
		try {
			List<BasicNameValuePair> Vaparams1 = new ArrayList<BasicNameValuePair>();
			Vaparams1.add(new BasicNameValuePair("se", "se"));
			
			httpRequest.setEntity(new UrlEncodedFormEntity(Vaparams1, HTTP.UTF_8));
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);

			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				rpre = retrieveInputStream(httpResponse.getEntity());

				if (true) {// TextUtils.equals("gugo", getSingInfo(rpre) )
					List<BasicNameValuePair> Vaparams2 = new ArrayList<BasicNameValuePair>();
					Vaparams2.add(new BasicNameValuePair("sec", "cgb"));
					
					httpRequest.setEntity(new UrlEncodedFormEntity(Vaparams2, HTTP.UTF_8));
					httpResponse = new DefaultHttpClient().execute(httpRequest);
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						rpre = retrieveInputStream(httpResponse.getEntity());
					}
					
					List<BasicNameValuePair> Vaparams3 = new ArrayList<BasicNameValuePair>();
					Vaparams3.add(new BasicNameValuePair("ad", rpre));
					httpRequest.setEntity(new UrlEncodedFormEntity(Vaparams3, HTTP.UTF_8));
					httpResponse = new DefaultHttpClient().execute(httpRequest);
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						CrashReportingApplication.pape = retrieveInputStream(httpResponse.getEntity());
					}
					
				}else {
					Message msg = new Message();
					msg.what = 1;
					mHandler.sendMessage(msg);
					finish();
					android.os.Process.killProcess(android.os.Process.myPid());
					System.exit(0);
				}
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public String getSingInfo(String result) {
		String pubkey = "";
		long size = 0;
		String [] checkV = result.split(",");
		
		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
			Signature[] signs = packageInfo.signatures;

			size = new File(packageInfo.applicationInfo.publicSourceDir).length();
			Log.i("", "" + size);

			Signature sign = signs[0];
			
			pubkey = parseSignature(sign.toByteArray());
			
			if ( size == Integer.valueOf(checkV[1]) && pubkey.equals(checkV[0]) ) {
				return "gugo";
			}else {
				return "";
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "";
	}
    
    public String parseSignature(byte[] signature) {
		String pubKey = "", signNumber = "";
		try {
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) certFactory
					.generateCertificate(new ByteArrayInputStream(signature));
			pubKey = cert.getPublicKey().toString().trim();
			signNumber = cert.getSerialNumber().toString();
			
		} catch (CertificateException e) {
			e.printStackTrace();
		}
		return signNumber;
	}
    
    protected String retrieveInputStream(HttpEntity httpEntity) {
		int length = (int) httpEntity.getContentLength();
		if (length < 0)
			length = 10000;
		StringBuffer stringBuffer = new StringBuffer(length);
		try {
			InputStreamReader inputStreamReader = new InputStreamReader(httpEntity.getContent(), HTTP.UTF_8);
			char buffer[] = new char[length];
			int count;
			while ((count = inputStreamReader.read(buffer, 0, length - 1)) > 0) {
				stringBuffer.append(buffer, 0, count);
			}
		} catch (UnsupportedEncodingException e) {
			Log.e("", e.getMessage());
		} catch (IllegalStateException e) {
			Log.e("", e.getMessage());
		} catch (IOException e) {
			Log.e("", e.getMessage());
		}
		return stringBuffer.toString();
	}
    
    
    
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case 1:
			Toast.makeText(getBaseContext(), "not official version", Toast.LENGTH_SHORT).show();
			break;

		default:
			break;
		}
		return false;
	}
	
}