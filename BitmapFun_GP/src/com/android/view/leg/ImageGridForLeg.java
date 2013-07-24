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

package com.android.view.leg;

import java.util.Hashtable;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyun.android.oss.OSSClient;
import com.android.hardcore.crashreport.CrashReportingApplication;
import com.example.Images;
import com.example.Utils;
import com.example.cache.DiskCache;
import com.example.cache.ImageCache.ImageCacheParams;
import com.example.cache.ImageWorker;
import com.example.ui.ImageDetailActivity;
import com.free.hardcore.wp9.BuildConfig;
import com.free.hardcore.wp9.R;
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


/**
 * The main fragment that powers the ImageGridActivity screen. Fairly straight
 * forward GridView implementation with the key addition being the ImageWorker
 * class w/ImageCache to load children asynchronously, keeping the UI nice and
 * smooth and caching thumbnails for quick retrieval. The cache is retained over
 * configuration changes like orientation change so the images are populated
 * quickly as the user rotates the device.
 */
public class ImageGridForLeg extends Fragment implements
		AdapterView.OnItemClickListener, TapjoyNotifier, TapjoyFullScreenAdNotifier, TapjoySpendPointsNotifier, TapjoyDisplayAdNotifier, TapjoyAwardPointsNotifier, TapjoyEarnedPointsNotifier, TapjoyVideoNotifier {
	private static final String TAG = "ImageGridFragment";
	private static final String IMAGE_CACHE_DIR = "thumbs";

	private int mImageThumbSize;
	private int mImageThumbSpacing;
	private ImageAdapter mAdapter;
	private ImageWorker mImageWorker;
	
	public OSSClient mOSSClient;
	private static final String ACCESS_ID = "lMosX47lTyMFuYRk";
    private static final String ACCESS_KEY = "4juWEixwMSgEOnu4fpt79Ja4933W4d";
    
    Button category_finish, category_add_category;
    
    UpdateManager manager;
    
    public SharedPreferences prefs;
    
	/**
	 * Empty constructor as per the Fragment documentation
	 */
	public ImageGridForLeg() {
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
		mOSSClient = new OSSClient();
		mOSSClient.setAccessId(ACCESS_ID);
		mOSSClient.setAccessKey(ACCESS_KEY);
		
		Hashtable<String, String> flags = new Hashtable<String, String>();
		flags.put(TapjoyConnectFlag.ENABLE_LOGGING, "false");
		TapjoyConnect.requestTapjoyConnect(getActivity(), "8b084d4d-6c47-43db-9690-5ddfa6e74ea5", "mxqcDoBW6s4pG5zJ2QTQ", flags);
//		TapjoyConnect.requestTapjoyConnect(getActivity(), "bba49f11-b87f-4c0f-9632-21aa810dd6f1", "yiQIURFEeKm0zbOggubu", flags);
		TapjoyConnect.getTapjoyConnectInstance().enableDisplayAdAutoRefresh(true);
		TapjoyConnect.getTapjoyConnectInstance().getDisplayAd(this);
		
		mImageThumbSize = getResources().getDimensionPixelSize(
				R.dimen.image_thumbnail_size);
		mImageThumbSpacing = getResources().getDimensionPixelSize(
				R.dimen.image_thumbnail_spacing);

		mAdapter = new ImageAdapter(getActivity(), Images.imageUrls_leg);

		ImageCacheParams cacheParams = new ImageCacheParams();
		cacheParams.reqHeight = mImageThumbSize;
		cacheParams.reqWidth = mImageThumbSize;
		// cacheParams.clearDiskCacheOnStart = true;
		cacheParams.memCacheSize = (1024 * 1024 * Utils.getMemoryClass(getActivity())) / 5;

		// cacheParams.clearDiskCacheOnStart = true;
		mImageWorker = ImageWorker.newInstance(getActivity());
		mImageWorker.addParams(TAG, cacheParams);
		mImageWorker.setLoadingImage(R.drawable.empty_photo);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final View v = inflater.inflate(R.layout.image_grid_fragment,
				container, false);
		final GridView mGridView = (GridView) v.findViewById(R.id.gridView);
		
		final TextView mTextView = (TextView) v.findViewById(R.id.title_text);
		mTextView.setText(getString(R.string.main_leg));
		
		ImageButton mImageButton = (ImageButton) v.findViewById(R.id.category_recomment);
		mImageButton.setVisibility(View.GONE);
		mImageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				TapjoyConnect.getTapjoyConnectInstance().showOffers();
			}
		});
		
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(this);
		mGridView.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						if (mAdapter.getNumColumns() == 0) {
							final int numColumns = (int) Math.floor(mGridView
									.getWidth()
									/ (mImageThumbSize + mImageThumbSpacing));
							if (numColumns > 0) {
								final int columnWidth = (mGridView.getWidth() / numColumns)
										- mImageThumbSpacing;
								mAdapter.setNumColumns(numColumns);
								mAdapter.setItemHeight(columnWidth);
								if (BuildConfig.DEBUG) {
								}
							}
						}
					}
				});

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
//		Log.e("ad", "onResume");
		mImageWorker.setOnScreen(TAG, true);
		// mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		super.onPause();
//		Log.e("ad", "onPause");
		mImageWorker.setOnScreen(TAG, false);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		final Intent i = new Intent(getActivity(), ImageDetailActivityLeg.class);
		i.putExtra(ImageDetailActivity.EXTRA_IMAGE, (int) id);
		startActivity(i);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//		inflater.inflate(R.menu.main_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.clear_cache:
			DiskCache mDiskCache = DiskCache.openCache();
			mDiskCache.clearCache();
			Toast.makeText(getActivity(), R.string.clear_cache_complete, Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * The main adapter that backs the GridView. This is fairly standard except
	 * the number of columns in the GridView is used to create a fake top row of
	 * empty views as we use a transparent ActionBar and don't want the real top
	 * row of images to start off covered by it.
	 */
	private class ImageAdapter extends BaseAdapter {

		private final Context mContext;
		private int mItemHeight = 0;
		private int mNumColumns = 0;
		private String[] imageUrls;
		private GridView.LayoutParams mImageViewLayoutParams;

		public ImageAdapter(Context context, String[] imageUrls) {
			super();
			mContext = context;
			this.imageUrls = imageUrls;
			mImageViewLayoutParams = new GridView.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		}

		@Override
		public int getCount() {
			// Size of adapter + number of columns for top empty row
			return imageUrls.length;
		}

		@Override
		public Object getItem(int position) {
			return imageUrls[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup container) {

			// Now handle the main ImageView thumbnails
			ImageView imageView;
			if (convertView == null) { // if it's not recycled, instantiate and
										// initialize
				imageView = new ImageView(mContext);
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setLayoutParams(mImageViewLayoutParams);
			} else { // Otherwise re-use the converted view
				imageView = (ImageView) convertView;
			}

			// Check the height matches our calculated column width
			if (imageView.getLayoutParams().height != mItemHeight) {
				imageView.setLayoutParams(mImageViewLayoutParams);
			}

			CrashReportingApplication.mFinalBitmap.display(imageView, imageUrls[position]);
//			mImageWorker.loadBitmap(imageUrls[position], imageView);
			return imageView;
		}

		/**
		 * Sets the item height. Useful for when we know the column width so the
		 * height can be set to match.
		 * 
		 * @param height
		 */
		public void setItemHeight(int height) {
			if (height == mItemHeight) {
				return;
			}
			mItemHeight = height;
			mImageViewLayoutParams = new GridView.LayoutParams(
					LayoutParams.MATCH_PARENT, mItemHeight);
			notifyDataSetChanged();
		}

		public void setNumColumns(int numColumns) {
			mNumColumns = numColumns;
		}

		public int getNumColumns() {
			return mNumColumns;
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
	
}
