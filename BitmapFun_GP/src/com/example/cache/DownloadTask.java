package com.example.cache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

import com.example.Utils;

public class DownloadTask implements Runnable {

	String urlString;
	OnDownloadListener downloadListener;
	volatile boolean stop = false;

	public DownloadTask(String urlString, OnDownloadListener downloadListener) {
		this.urlString = urlString;
		this.downloadListener = downloadListener;
	}

	// 停止掉任务
	public void cancelWork() {
//		Log.e("ad", "cancelWork   DownloadTask");
		stop = true;
	}

	@Override
	public void run() {
		if (!stop) {
			try {
//				Log.e("ad", "Download  start");
				downloadListener.onStart();
				File file = downloadBitmap(urlString);
				downloadListener.onFinish(file);
			} catch (IOException e) {
				downloadListener.onError();
				e.printStackTrace();
			}
		} else {
//			Log.e("ad", "OK  OK    OK ");
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
		urlConnection.setReadTimeout(100000);
		urlConnection.setConnectTimeout(20000);
		final InputStream in = new BufferedInputStream(
				urlConnection.getInputStream(), Utils.IO_BUFFER_SIZE);
		out = new BufferedOutputStream(new FileOutputStream(cacheFile),
				Utils.IO_BUFFER_SIZE);

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
