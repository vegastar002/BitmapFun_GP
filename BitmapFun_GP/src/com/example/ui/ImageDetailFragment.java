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

import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.FloatMath;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.free.hardcore.wp9.R;
import com.example.Images;
import com.example.cache.ImageWorker;

/**
 * This fragment will populate the children of the ViewPager from
 * {@link ImageDetailActivity}.
 */
public class ImageDetailFragment extends Fragment implements OnTouchListener {
	private static final String IMAGE_DATA_EXTRA = "resId";
	private int mImageNum;
	private ImageView mImageView;
	private ImageWorker mImageWorker;
	
	
	private Matrix matrix=new Matrix();
	private Matrix savedMatrix=new Matrix();
	
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;
	
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;

	/**
	 * Factory method to generate a new instance of the fragment given an image
	 * number.
	 * 
	 * @param imageNum
	 *            The image number within the parent adapter to load
	 * @return A new instance of ImageDetailFragment with imageNum extras
	 */
	public static ImageDetailFragment newInstance(int imageNum) {
		final ImageDetailFragment f = new ImageDetailFragment();

		final Bundle args = new Bundle();
		args.putInt(IMAGE_DATA_EXTRA, imageNum);
		f.setArguments(args);

		return f;
	}

	/**
	 * Empty constructor as per the Fragment documentation
	 */
	public ImageDetailFragment() {
	}

	/**
	 * Populate image number from extra, use the convenience factory method
	 * {@link ImageDetailFragment#newInstance(int)} to create this fragment.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mImageNum = getArguments() != null ? getArguments().getInt(IMAGE_DATA_EXTRA) : -1;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate and locate the main ImageView
		final View v = inflater.inflate(R.layout.image_detail_fragment,
				container, false);
		mImageView = (ImageView) v.findViewById(R.id.imageView);
		mImageView.setOnTouchListener(this);
		mImageView.setLongClickable(true);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (ImageDetailActivity.class.isInstance(getActivity())) {
			mImageWorker = ((ImageDetailActivity) getActivity())
					.getImageWorker();

			//v2.1更新
			if ( Images.imageUrls.length != 0 ){
				mImageWorker.loadBitmap(Images.imageUrls[mImageNum], mImageView);
			}
		}

	}

	/**
	 * Cancels the asynchronous work taking place on the ImageView, called by
	 * the adapter backing the ViewPager when the child is destroyed.
	 */
	public void cancelWork() {
		ImageWorker.cancelWork(mImageView);
		mImageView.setImageDrawable(null);
		mImageView = null;
	}
	
	
	private float spacing(MotionEvent event){
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}
	
	private void midPoint(PointF point, MotionEvent event){
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}
	
	
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		int action = (event.getAction()&MotionEvent.ACTION_MASK);//统一单点和多点触摸
    	switch (action) {
		case MotionEvent.ACTION_DOWN:
//			Log.i("", "ACTION_DOWN");
			matrix.set(mImageView.getImageMatrix());
			savedMatrix.set(matrix);
			start.set(event.getX(),event.getY());
			mode=DRAG;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
//			Log.i("", "ACTION_POINTER_DOWN");
			oldDist=this.spacing(event);
			if(oldDist>10f){
				savedMatrix.set(matrix);
				midPoint(mid,event);
				mode=ZOOM;
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
//			Log.i("", "ACTION_POINTER_UP");
			mode=NONE;
			break;
		case MotionEvent.ACTION_MOVE:
//			Log.i("", "ACTION_MOVE");
			if(mode==DRAG){
				matrix.set(savedMatrix);
				matrix.postTranslate(event.getX()-start.x, event.getY()-start.y);
			}else if(mode==ZOOM){
				float newDist=spacing(event);
				if(newDist>10){
					matrix.set(savedMatrix);
					float scale=newDist/oldDist;
					matrix.postScale(scale, scale, mid.x, mid.y);
				}
			}
			break;
		}
		mImageView.setImageMatrix(matrix);
		return true;
	}

}
