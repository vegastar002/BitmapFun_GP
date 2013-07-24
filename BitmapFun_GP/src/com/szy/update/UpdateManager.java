package com.szy.update;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.example.ui.ImageGridFragment;
import com.free.hardcore.wp9.R;

public class UpdateManager
	{
		/* ������ */
		private static final int DOWNLOAD = 1;
		/* ���ؽ��� */
		private static final int DOWNLOAD_FINISH = 2;
		/* ���������XML��Ϣ */
		public HashMap<String, String> mHashMap;
		/* ���ر���·�� */
		private String mSavePath;
		/* ��¼��������� */
		private int progress;
		/* �Ƿ�ȡ����� */
		private boolean cancelUpdate = false;

		private Context mContext;
		/* ���½���� */
		private ProgressBar mProgress;
		private Dialog mDownloadDialog;
		
		public Handler mHandler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				switch (msg.what)
				{
				// ��������
				case DOWNLOAD:
					// ���ý����λ��
					mProgress.setProgress(progress);
					break;
				case DOWNLOAD_FINISH:
					// ��װ�ļ�
					installApk();
					break;
				default:
					break;
				}
			};
		};

		public UpdateManager(Context context)
		{
			this.mContext = context;
		}

		public boolean isUpdate(String input) throws Exception
		{
			int versionCode = getVersionCode(mContext);
			ParseXmlService service = new ParseXmlService();
			try
			{
				ByteArrayInputStream is = new ByteArrayInputStream(input.getBytes("utf-8"));
				mHashMap = service.parseXml(is);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			
			if (null != mHashMap)
			{
				int serviceCode = Integer.valueOf(mHashMap.get("version"));
				if (serviceCode > versionCode)
				{
					return true;
				}
			}
			return false;
		}
		
		
		private int getVersionCode(Context context)
		{
			int versionCode = 0;
	        
			try
			{
				versionCode = mContext.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
			} catch (NameNotFoundException e)
			{
				e.printStackTrace();
			}
			return versionCode;
		}

		/**
		 * ��ʾ������¶Ի���
		 */
		public void showNoticeDialog()
		{
			// ����Ի���
			AlertDialog.Builder builder = new Builder(mContext);
			builder.setTitle(R.string.soft_update_title);
			builder.setMessage(R.string.soft_update_info);
			// ����
			builder.setPositiveButton(R.string.soft_update_updatebtn, new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
					// ��ʾ���ضԻ���
					showDownloadDialog();
				}
			});
			// �Ժ����
			builder.setNegativeButton(R.string.soft_update_later, new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
				}
			});
			Dialog noticeDialog = builder.create();
			noticeDialog.show();
		}

		/**
		 * ��ʾ������ضԻ���
		 */
		public void showDownloadDialog()
		{
			// ����������ضԻ���
			AlertDialog.Builder builder = new Builder(mContext);
			builder.setTitle(R.string.soft_updating);
			// �����ضԻ������ӽ����
			final LayoutInflater inflater = LayoutInflater.from(mContext);
			View v = inflater.inflate(R.layout.softupdate_progress, null);
			mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
			builder.setView(v);
			// ȡ�����
			builder.setNegativeButton(android.R.string.cancel, new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
					// ����ȡ��״̬
					cancelUpdate = true;
				}
			});
			mDownloadDialog = builder.create();
			mDownloadDialog.show();
			// �����ļ�
			downloadApk();
		}

		/**
		 * ����apk�ļ�
		 */
		private void downloadApk()
		{
			// �������߳��������
			new downloadApkThread().start();
		}

		/**
		 * �����ļ��߳�
		 * 
		 * @author coolszy
		 *@date 2012-4-26
		 *@blog http://blog.92coding.com
		 */
		private class downloadApkThread extends Thread
		{
			@Override
			public void run()
			{
				try
				{
					// �ж�SD���Ƿ���ڣ������Ƿ���ж�дȨ��
					if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
					{
						// ��ô洢����·��
						mSavePath = ImageGridFragment.verXMLDir;
						URL url = new URL(mHashMap.get("url"));
						// ��������
						HttpURLConnection conn = (HttpURLConnection) url.openConnection();
						conn.connect();
						// ��ȡ�ļ���С
						int length = conn.getContentLength();
						// ����������
						InputStream is = conn.getInputStream();

						File file = new File(mSavePath);
						// �ж��ļ�Ŀ¼�Ƿ����
						if (!file.exists())
						{
							file.mkdir();
						}
						File apkFile = new File(mSavePath, mHashMap.get("name"));
						FileOutputStream fos = new FileOutputStream(apkFile);
						int count = 0;
						// ����
						byte buf[] = new byte[1024];
						// д�뵽�ļ���
						do
						{
							int numread = is.read(buf);
							count += numread;
							// ��������λ��
							progress = (int) (((float) count / length) * 100);
							// ���½��
							mHandler.sendEmptyMessage(DOWNLOAD);
							if (numread <= 0)
							{
								// �������
								mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
								break;
							}
							// д���ļ�
							fos.write(buf, 0, numread);
						} while (!cancelUpdate);// ���ȡ���ֹͣ����.
						fos.close();
						is.close();
					}
				} catch (MalformedURLException e)
				{
					e.printStackTrace();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
				// ȡ�����ضԻ�����ʾ
				mDownloadDialog.dismiss();
			}
		};

		/**
		 * ��װAPK�ļ�
		 */
		private void installApk()
		{
			File apkfile = new File(mSavePath, mHashMap.get("name"));
			if (!apkfile.exists())
			{
				return;
			}
			// ͨ��Intent��װAPK�ļ�
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
			mContext.startActivity(i);
		}
	}