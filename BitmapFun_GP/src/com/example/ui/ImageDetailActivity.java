/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.ui;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Images;
import com.example.cache.DiskCache;
import com.example.cache.ImageCache.ImageCacheParams;
import com.example.cache.ImageWorker;
import com.free.hardcore.wp9.R;
import com.inmobi.androidsdk.IMAdInterstitial;
import com.inmobi.androidsdk.IMAdInterstitialListener;
import com.inmobi.androidsdk.IMAdListener;
import com.inmobi.androidsdk.IMAdRequest;
import com.inmobi.androidsdk.IMAdView;
import com.inmobi.androidsdk.IMAdRequest.ErrorCode;
import com.tapjoy.TapjoyAwardPointsNotifier;
import com.tapjoy.TapjoyConnect;
import com.tapjoy.TapjoyConnectFlag;
import com.tapjoy.TapjoyDisplayAdNotifier;
import com.tapjoy.TapjoyEarnedPointsNotifier;
import com.tapjoy.TapjoyFullScreenAdNotifier;
import com.tapjoy.TapjoyNotifier;
import com.tapjoy.TapjoySpendPointsNotifier;
import com.tapjoy.TapjoyVideoNotifier;

public class ImageDetailActivity extends FragmentActivity implements
TapjoyNotifier, TapjoyFullScreenAdNotifier, TapjoySpendPointsNotifier, TapjoyDisplayAdNotifier, TapjoyAwardPointsNotifier, TapjoyEarnedPointsNotifier, TapjoyVideoNotifier{
	private static final String IMAGE_CACHE_DIR = "images";
	public static final String EXTRA_IMAGE = "extra_image";
	private final static String TAG = "ImageDetailActivity";
	
	private final static int HANDLER_MSG_SAVE = 1;
	private final static int HANDLER_MSG_WALLPAPER = 2;
	
	private ImagePagerAdapter mAdapter;
	private ImageWorker mImageWorker;
	private ViewPager mPager;
//	private Handler mHandler = new Handler();
	
	String cachePath;
	String downloadDIR = "", theDLOpenFileString = "";
	int uniquePageNum = 0;//current page number
	
	Button set_wallpaper;
	Button share_button1, download_button1, open_gallery;
	Button category_add_category;
	TextView title_text;
	ImageButton category_finish;
	
	private IMAdView mIMAdView;
	private IMAdInterstitial mIMAdInterstitial;
	private IMAdRequest mAdRequest;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_detail_pager);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);

		downloadDIR = Environment.getExternalStorageDirectory().getAbsolutePath() +
				File.separator + getString(R.string.app_dir);
		if ( !new File(downloadDIR).exists() ){
			new File(downloadDIR).mkdir();
		}
		
		//inmobi广告
//		mIMAdView = (IMAdView) findViewById(R.id.imAdview_detail);
//		mAdRequest = new IMAdRequest();
//		mAdRequest.setTestMode(false);
//		mIMAdView.setIMAdRequest(mAdRequest);
//		mIMAdView.loadNewAd(mAdRequest);
//		mIMAdView.setIMAdListener(mIMAdListener);
//		
//		mIMAdInterstitial = new IMAdInterstitial(this,"b3984fc8084b45788bb3e3feae329cc6");
//		mIMAdInterstitial.setIMAdInterstitialListener(mIMAdInListener);
//		mIMAdView.loadNewAd();
		
		
		Hashtable<String, String> flags = new Hashtable<String, String>();
		flags.put(TapjoyConnectFlag.ENABLE_LOGGING, "false");
		TapjoyConnect.requestTapjoyConnect(getApplicationContext(), "8b084d4d-6c47-43db-9690-5ddfa6e74ea5", "mxqcDoBW6s4pG5zJ2QTQ", flags);
//		TapjoyConnect.requestTapjoyConnect(getApplicationContext(), "bba49f11-b87f-4c0f-9632-21aa810dd6f1", "yiQIURFEeKm0zbOggubu", flags);
		TapjoyConnect.getTapjoyConnectInstance().setEarnedPointsNotifier(this);
		
		
//		Log.i("", "downloadDIR : "+downloadDIR);
		download_button1 = (Button) findViewById(R.id.download_button1);
		download_button1.setOnClickListener(mDownload_button1Listener);
		
		share_button1 = (Button) findViewById(R.id.share_button1);
		share_button1.setOnClickListener(mShare_button1Listener);
		
		set_wallpaper = (Button) findViewById(R.id.set_wallpaper);
		set_wallpaper.setOnClickListener(mSet_WallpaperListener);
		
		
		open_gallery = (Button) findViewById(R.id.open_gallery);
		open_gallery.setOnClickListener(mOpen_galleryListener);
		
		category_finish = (ImageButton) findViewById(R.id.category_finish);
		category_finish.setOnClickListener(mCategory_finish);
		category_finish.setVisibility(View.GONE);
		
		category_add_category = (Button) findViewById(R.id.category_add_category);
		category_add_category.setOnClickListener(mBackListener);
		
		title_text = (TextView) findViewById(R.id.title_text);
		title_text.setText(getString(R.string.detail_title_text));
		
		// Fetch screen height and width, to use as our max size when loading
		// images as this
		// activity runs full screen
		final DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		final int height = displaymetrics.heightPixels;
		final int width = displaymetrics.widthPixels;

		ImageCacheParams cacheParams = new ImageCacheParams();
		cacheParams.reqHeight = height;
		cacheParams.reqWidth = width;
		cacheParams.memoryCacheEnabled = false;
		mImageWorker = ImageWorker.newInstance(this);
		mImageWorker.addParams(TAG, cacheParams);
		mImageWorker.setLoadingImage(R.drawable.empty_photo);

		// Set up ViewPager and backing adapter
		mAdapter = new ImagePagerAdapter(getSupportFragmentManager(),
				Images.imageUrls.length);
		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);
		
		// Set the current item based on the extra passed in to this activity
		final int extraCurrentItem = getIntent().getIntExtra(EXTRA_IMAGE, -1);
		uniquePageNum = extraCurrentItem + 1;
		// Log.i("", "由n进入: "+ extraCurrentItem);
		if (extraCurrentItem != -1) {
			 mPager.setCurrentItem(extraCurrentItem);
		}
		
		mPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				// TODO Auto-generated method stub
				uniquePageNum = position + 1;
//				Log.i("", "当前页面序号： onPageSelected");
				download_button1.setVisibility(View.VISIBLE);
				set_wallpaper.setVisibility(View.GONE);
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				// TODO Auto-generated method stub
			}
		});

	}
	
	private OnClickListener mDownload_button1Listener = new OnClickListener() {
		public void onClick(View v) {
			Message message = new Message();   
    	    message.what = HANDLER_MSG_SAVE;
    	    imageHandler.sendMessage(message);
		}
    };
    
    private OnClickListener mBackListener = new OnClickListener() {
		public void onClick(View v) {
			finish();
		}
    };
    
    private OnClickListener mCategory_finish = new OnClickListener() {
		public void onClick(View v) {
			TapjoyConnect.getTapjoyConnectInstance().showOffers();
		}
    };
    
    
	Handler imageHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLER_MSG_SAVE:
				cachePath = Environment.getExternalStorageDirectory().getAbsolutePath();
				String fts1 = cachePath + File.separator + DiskCache.DISK_CACHE_DIR;
				String ts1;
				ts1 = getExtensionName(Images.imageUrls[uniquePageNum-1]);
				String fts2 = fts1 + File.separator + ts1;
				
				if (new File(fts2).exists()) {
					download_button1.setVisibility(View.GONE);
					set_wallpaper.setVisibility(View.VISIBLE);
					theDLOpenFileString = downloadDIR+File.separator + ts1;
					if ( new File(theDLOpenFileString).exists() ){
						Toast.makeText(getApplicationContext(), getString(R.string.sub_download_again),
							     Toast.LENGTH_LONG).show();
					}else {
						new File(fts2).renameTo(new File(theDLOpenFileString));
						Toast.makeText(getApplicationContext(), getString(R.string.sub_download_ok),
							     Toast.LENGTH_LONG).show();
					}
				}
				break;
			case HANDLER_MSG_WALLPAPER:
				try {
//	                WallpaperManager instance = WallpaperManager.getInstance(ImageDetailActivity.this);
//	                int desiredMinimumWidth = getWindowManager().getDefaultDisplay().getWidth(); 
//	                int desiredMinimumHeight = getWindowManager().getDefaultDisplay().getHeight();
//	                instance.suggestDesiredDimensions(desiredMinimumWidth*2, desiredMinimumHeight);
	                Bitmap yBitmap = BitmapFactory.decodeFile(theDLOpenFileString);
//	                instance.setBitmap(yBitmap);
					
					getApplicationContext().setWallpaper(yBitmap);
					
	                Toast.makeText(ImageDetailActivity.this,getString(R.string.set_wallpaper_ok),Toast.LENGTH_LONG).show();
	            } catch (IOException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            }
				break;
			case 3:
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
        
        
    
    public static String getExtensionName(String filename) {   
        if ((filename != null) && (filename.length() > 0)) {   
            int dot = filename.lastIndexOf('/');   
            if ((dot >-1) && (dot < (filename.length() - 1))) {  
//            	Log.i("", "下载的文件名： "+filename.substring(dot+1));
                return filename.substring(dot+1);   
            }   
        }   
        return filename;   
    }
    
    
    private OnClickListener mShare_button1Listener = new OnClickListener() {
        
		public void onClick(View v) {
			String fts1 = Environment.getExternalStorageDirectory().getAbsolutePath() + 
					File.separator + DiskCache.DISK_CACHE_DIR;
			
			String ts1;
			ts1 = getExtensionName(Images.imageUrls[uniquePageNum-1]);
			String fts2 = fts1 + File.separator + ts1;
			if ( new File(fts2).exists() ){
				Intent it = new Intent(Intent.ACTION_SEND);
				it.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(fts2 )));
				it.setType("image/*");
				startActivity(it);
			}else {
				String ssts1 = downloadDIR + File.separator + ts1;
				Intent it = new Intent(Intent.ACTION_SEND);
				it.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(ssts1 )));
				it.setType("image/*");
				startActivity(it);
			}
		}
    };
    
    private OnClickListener mSet_WallpaperListener = new OnClickListener() {
        
		public void onClick(View v) {
			Message message = new Message();
    	    message.what = HANDLER_MSG_WALLPAPER;
    	    imageHandler.sendMessage(message);
		}
    };
    
    
    private OnClickListener mOpen_galleryListener = new OnClickListener() {
		public void onClick(View v) {
//			Intent it = new Intent(Intent.ACTION_VIEW);     
//			it.setType("vnd.android.cursor.dir/image");     
//			startActivity(it);
			
			
			
			String ts2 = Environment.getExternalStorageDirectory().getAbsolutePath();
			String fts1 = ts2 + File.separator + DiskCache.DISK_CACHE_DIR;
			String ts1 = getExtensionName(Images.imageUrls[uniquePageNum-1]);
			String fts2 = fts1 + File.separator + ts1;
			
			if ( new File(fts2).exists() ){
				//跳转到图库的单个文件
				Intent intent = new Intent(Intent.ACTION_VIEW);
	            Uri mUri = Uri.parse("file://" + fts2);
	            intent.setDataAndType(mUri, "image/*");
	            startActivity(intent);
			}else {
				String ssts1 = downloadDIR + File.separator + ts1;
				Intent intent = new Intent(Intent.ACTION_VIEW);
	            Uri mUri = Uri.parse("file://" + ssts1);
	            intent.setDataAndType(mUri, "image/*");
	            startActivity(intent);
			}
		}
        
    };
    
    
    
    public Bitmap sBitmap(Bitmap b, int w, int h) {
        int width = b.getWidth();
        int height = b.getHeight();
        float scaleWidth = ((float) w) / width;
        float scaleHeight = ((float) h) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);//缩放
        return Bitmap.createBitmap(b, 0, 0, width, height, matrix, true);
    }
    
    
    

	@Override
	public void onResume() {
		super.onResume();
		mImageWorker.setOnScreen(TAG, true);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		super.onPause();
		mImageWorker.setOnScreen(TAG, false);
	}

	/**
	 * Called by the ViewPager child fragments to load images via the one
	 * ImageWorker
	 * 
	 * @return
	 */
	public ImageWorker getImageWorker() {
		return mImageWorker;
	}

	/**
	 * The main adapter that backs the ViewPager. A subclass of
	 * FragmentStatePagerAdapter as there could be a large number of items in
	 * the ViewPager and we don't want to retain them all in memory at once but
	 * create/destroy them on the fly.
	 */
	private class ImagePagerAdapter extends FragmentStatePagerAdapter {
		private final int mSize;

		public ImagePagerAdapter(FragmentManager fm, int size) {
			super(fm);
			mSize = size;
		}

		@Override
		public int getCount() {
			return mSize;
		}

		@Override
		public Fragment getItem(int position) {
//			Log.i("", "ImagePagerAdapter 里选中了： "+position);
			return ImageDetailFragment.newInstance(position);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			final ImageDetailFragment fragment = (ImageDetailFragment) object;
			// As the item gets destroyed we try and cancel any existing work.
			fragment.cancelWork();
			super.destroyItem(container, position, object);
		}
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
	
	

}
