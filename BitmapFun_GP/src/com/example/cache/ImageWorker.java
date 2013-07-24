package com.example.cache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.example.Utils;
import com.example.cache.ImageCache.ImageCacheParams;
import com.free.hardcore.wp9.*;

public enum ImageWorker {

	INSTANCE;

	private ImageCache mImageCache;

	private static Context mContext;

	private volatile boolean onScreen = true;

	private Bitmap mLoadingBitmap;

	private ExecutorService searchThreadPool;
	private OnHandleCacheListener mIHandleCache;
	private HashMap<String, ImageCacheParams> params;

	private Handler mHandler;

	private ImageWorker() {
		mImageCache = ImageCache.createCache();
		searchThreadPool = Executors.newFixedThreadPool(20);
		mHandler = new Handler();
		mIHandleCache = new OnHandleCacheListener() {

			@Override
			public void onSetImage(final ImageView imageView,
					final Bitmap bitmap) {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						imageView.setImageBitmap(bitmap);
					}
				});
			}

			@Override
			public void onError(final ImageView imageView) {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						if (imageView != null) {
							imageView.setImageResource(R.drawable.error);
						}
					}
				});
			}
		};

	}

	public static ImageWorker newInstance(Context context) {
		mContext = context;
		return INSTANCE;
	}

	public void loadBitmap(final String path, final ImageView imageView) {
		Bitmap result = mImageCache.getBitmapFromMem(path);
		if (result != null && !result.isRecycled()) {
			mIHandleCache.onSetImage(imageView, result);
		} else if (cancelWork(imageView, path)) {
			final SearchTask task = new SearchTask(path, imageView,	mIHandleCache);
			Drawable d = imageView.getDrawable();
			if (d != null) {
				d.setCallback(null);
				d = null;
			}
			final AsyncDrawable asyncDrawable = new AsyncDrawable(mContext.getResources(), mLoadingBitmap, task);
			imageView.setImageDrawable(asyncDrawable);
			searchThreadPool.execute(task);
		}

	}

	protected boolean cancelWork(final ImageView view, final String path) {
		SearchTask task = getSearchTask(view);
		if (task != null) {
			final String taskPath = task.getPath();
			if (TextUtils.isEmpty(taskPath) || !taskPath.equals(path)) {
//				Log.i("foyo", "cancelWork.taskPath: " + taskPath);
				task.cancelWork();
			} else {
//				Log.i("foyo", "task is exist");
				return false;

			}
		} else {
//			Log.i("foyo", "new  task");
		}
		return true;
	}

	public static void cancelWork(final ImageView imageView) {
		final SearchTask bitmapWorkerTask = getSearchTask(imageView);
		if (bitmapWorkerTask != null) {
			bitmapWorkerTask.cancelWork();
		}
	}

	public static SearchTask getSearchTask(final ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getTask();
			}
		}
		return null;
	}

	/**
	 * Set placeholder bitmap that shows when the the background thread is
	 * running.
	 * 
	 * @param resId
	 */
	public void setLoadingImage(final int resId) {
		mLoadingBitmap = BitmapFactory.decodeResource(mContext.getResources(),
				resId);
	}

	public class SearchTask implements Runnable {

		String path;
		volatile boolean stop = false;
		OnHandleCacheListener mIHandleCache;

		private WeakReference<ImageView> mImageViewReference;

		// 停止掉任务
		public void cancelWork() {
			stop = true;
		}

		public SearchTask(final String path, final ImageView imageView,
				final OnHandleCacheListener mIHandleCache) {
			this.path = path;
			mImageViewReference = new WeakReference<ImageView>(imageView);
			this.mIHandleCache = mIHandleCache;
		}

		public String getPath() {
			return path;
		}

		@Override
		public void run() {
			Bitmap bitmap = null;
			if (mImageCache != null && !stop && getAttachedImageView() != null
					&& onScreen) {
				bitmap = mImageCache.getBitmapFromDiskCache(path);
//				Log.e("foyo", "find  bitmap from disk  == " + bitmap);
			}
			if (bitmap == null && mImageCache != null && !stop
					&& getAttachedImageView() != null && onScreen) {
				try {
					File file = downloadBitmap(path);
					bitmap = mImageCache.getBitmapFromDiskCache(file);
					if (bitmap == null) {
						mIHandleCache.onError(getAttachedImageView());
					}
//					Log.e("foyo", "download  bitmap  == " + bitmap);
				} catch (IOException e) {
					mIHandleCache.onError(getAttachedImageView());
					e.printStackTrace();
				}

			}

			if (bitmap != null && mImageCache != null && !stop
					&& getAttachedImageView() != null && onScreen) {
				mImageCache.addBitmapToCache(path, bitmap);
				ImageView imageView = getAttachedImageView();
				if (imageView != null && !stop) {
					mIHandleCache.onSetImage(imageView, bitmap);
				} else {
					bitmap.recycle();
					bitmap = null;

				}
			}

		}

		/**
		 * Returns the ImageView associated with this task as long as the
		 * ImageView's task still points to this task as well. Returns null
		 * otherwise.
		 */
		private ImageView getAttachedImageView() {
			final ImageView imageView = mImageViewReference.get();
			final SearchTask bitmapWorkerTask = ImageWorker
					.getSearchTask(imageView);

			if (this == bitmapWorkerTask) {
				return imageView;
			}

			return null;
		}

	}

	public void setOnScreen(String tag, boolean onScreen) {
		this.onScreen = onScreen;
		if (!onScreen) {
			mImageCache.clearCaches();
			shutdownThreadPool();
		} else {
			restartThreadPool();
			mImageCache.setCacheParams(getParams(tag));
		}
	}

	private void restartThreadPool() {
		synchronized (searchThreadPool) {
			if (searchThreadPool.isTerminated()
					|| searchThreadPool.isShutdown()) {
				searchThreadPool = null;
				searchThreadPool = Executors.newFixedThreadPool(1);
			}
		}

	}

	public void shutdownThreadPool() {
		searchThreadPool.shutdown();
	}

	/**
	 * Set a new CacheParams.
	 */
	public void addParams(String tag, ImageCacheParams cacheParams) {
		if (params == null) {
			params = new HashMap<String, ImageCache.ImageCacheParams>();
		}
		params.put(tag, cacheParams);
		mImageCache.setCacheParams(cacheParams);
	}

	/**
	 * Get a CacheParams by flag.
	 */
	public ImageCacheParams getParams(String tag) {
		return params.get(tag);
	}

	public class AsyncDrawable extends BitmapDrawable {
		private final WeakReference<SearchTask> task;

		public AsyncDrawable(Resources res, Bitmap bitmap, SearchTask searchTask) {
			super(res, bitmap);
			task = new WeakReference<SearchTask>(searchTask);
		}

		public SearchTask getTask() {
			return task.get();
		}
	}

	private File downloadBitmap(String urlString) throws IOException {

		final DiskCache cache = DiskCache.openCache();

		final File cacheFile = new File(cache.createFilePath(urlString));

		if (cache.containsKey(urlString)) {
			return cacheFile;
		}
		Utils.disableConnectionReuseIfNecessary();
		HttpURLConnection urlConnection = null;
		BufferedOutputStream out = null;

		final URL url = new URL(urlString);
		urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setReadTimeout(10 * 1000);
		urlConnection.setConnectTimeout(5 * 1000);
		final InputStream in = new BufferedInputStream(
				urlConnection.getInputStream(), Utils.IO_BUFFER_SIZE);
		out = new BufferedOutputStream(new FileOutputStream(cacheFile),
				Utils.IO_BUFFER_SIZE);
//		Log.e("foyo", "in    " + in);
		int b;
		while ((b = in.read()) != -1) {
			out.write(b);
		}

		out.flush();
		out.close();
		cacheFile.setLastModified(System.currentTimeMillis());
		return cacheFile;
	}

}
