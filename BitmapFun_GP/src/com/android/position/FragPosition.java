package com.android.position;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
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
import cn.waps.AppConnect;
import cn.waps.UpdatePointsNotifier;

import com.android.hardcore.crashreport.CrashReportingApplication;
import com.example.Images;
import com.example.cache.ImageWorker;
import com.example.ui.bopoType;
import com.free.hardcore.wp9.BuildConfig;
import com.free.hardcore.wp9.R;
import com.szy.update.UpdateManager;

public class FragPosition extends Fragment implements AdapterView.OnItemClickListener, UpdatePointsNotifier{

	private static final String TAG = "ImageGridFragment";

	private int mImageThumbSize;
	private int mImageThumbSpacing;
	private ImageAdapter mAdapter;
	private ImageWorker mImageWorker;
	
    Button category_finish, category_add_category;
    UpdateManager manager;
    public SharedPreferences prefs;
    
	public FragPosition() {
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
		mImageThumbSize = getResources().getDimensionPixelSize(
				R.dimen.image_thumbnail_size);
		mImageThumbSpacing = getResources().getDimensionPixelSize(
				R.dimen.image_thumbnail_spacing);

		mAdapter = new ImageAdapter(getActivity(), Images.imageUrls_position);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		AppConnect.getInstance(getActivity());
		AppConnect.getInstance(getActivity()).getPoints(this);
//		AppConnect.getInstance(getActivity()).awardPoints(18, this);
		
		
		final View v = inflater.inflate(R.layout.image_grid_fragment, container, false);
		final GridView mGridView = (GridView) v.findViewById(R.id.gridView);
		
		final TextView mTextView = (TextView) v.findViewById(R.id.title_text);
		mTextView.setText(R.string.position_title);
		
		ImageButton mImageButton = (ImageButton) v.findViewById(R.id.category_recomment);
		mImageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
//				TapjoyConnect.getTapjoyConnectInstance().showOffers();
				AppConnect.getInstance(getActivity()).showAppOffers(getActivity());
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

	
	private class ImageAdapter extends BaseAdapter {

		private final Context mContext;
		private int mItemHeight = 0;
		private int mNumColumns = 0;
		private String[] iPmageUrls;
		private GridView.LayoutParams mImageViewLayoutParams;

		public ImageAdapter(Context context, String[] imageUrls) {
			super();
			mContext = context;
			this.iPmageUrls = imageUrls;
			mImageViewLayoutParams = new GridView.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		}

		@Override
		public int getCount() {
			// Size of adapter + number of columns for top empty row
			return iPmageUrls.length;
		}

		@Override
		public Object getItem(int position) {
			return iPmageUrls[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup container) {
			ImageView imageView;
			
			if (convertView == null) {
				imageView = new ImageView(mContext);
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setLayoutParams(mImageViewLayoutParams);
			} else {
				imageView = (ImageView) convertView;
			}

			// Check the height matches our calculated column width
			if (imageView.getLayoutParams().height != mItemHeight) {
				imageView.setLayoutParams(mImageViewLayoutParams);
			}

			CrashReportingApplication.mFinalBitmap.display(imageView, iPmageUrls[position]);
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
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//		Log.i("", ""+ Images.podaString[position] );
		Intent intent = new Intent(getActivity(), bopoType.class);
		intent.putExtra("sname", Images.podaString[position] );
		startActivity(intent);
	}

	@Override
	public void getUpdatePoints(String currencyName, int pointTotal) {
		// TODO Auto-generated method stub
		Log.i("", pointTotal + "");
		
	}

	@Override
	public void getUpdatePointsFailed(String error) {
		// TODO Auto-generated method stub
		
	}

}
