package com.example.ui;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import cn.waps.AdView;
import cn.waps.AppConnect;

import com.example.cache.ImageWorker;
import com.free.hardcore.wp9.R;
import com.grbmzbvus.vwbdizdju129465.Airpush;


@SuppressWarnings("deprecation")
public class TabMainActivity extends TabActivity implements OnClickListener{

	public static TabHost mTabHost;
	public static String TAB_TAG_HOME = "tab1";
	public static String TAB_TAG_SED = "tab2";
	public static String TAB_TAG_POSI = "tab3";
	public static String TAB_TAG_MORE = "tab4";
	
	Intent mHomeItent, mLegItent, mPosIntent, mMoreIntent;
	private Animation left_in, left_out;
	private Animation right_in, right_out;
	
	private static final int INTERVAL_TIME = 5;
	private static final int MILLISECOND_TO_SECOND = 1000;
	private boolean mIsExit;
	private static long sFirstTimePressBackBtn;
	int mCurTabId = R.id.main_pigu;
	Button category_finish, category_add_category;
	TextView title_text;
	
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab_main);
		
		prepareAnim();
		prepareIntent();
		setupIntent();
		prepareView();
		
		mIsExit = false;
	}
	
	private void prepareView() {
		findViewById(R.id.main_pigu).setOnClickListener(this);
		findViewById(R.id.leg).setOnClickListener(this);
		findViewById(R.id.position).setOnClickListener(this);
		findViewById(R.id.more).setOnClickListener(this);
	}
	
	private void prepareAnim() {
		left_in = AnimationUtils.loadAnimation(this, R.anim.left_in);
		left_out = AnimationUtils.loadAnimation(this, R.anim.left_out);

		right_in = AnimationUtils.loadAnimation(this, R.anim.right_in);
		right_out = AnimationUtils.loadAnimation(this, R.anim.right_out);
	}
	
	private void prepareIntent() {
		mHomeItent = new Intent(this, ImageGridActivity.class);
		mHomeItent.putExtra("origin", "home");
		
		mLegItent = new Intent(this, ImageGridActivity.class);
		mLegItent.putExtra("origin", "leg");
		
		mPosIntent = new Intent(this, ImageGridActivity.class);
		mPosIntent.putExtra("origin", "position");
		
		mMoreIntent = new Intent(this, MoreActivity.class);
	}
	
	public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
//        	if (!mIsExit) {
//                Toast.makeText(this, getText(R.string.str_again_exit), Toast.LENGTH_SHORT).show();
//                mIsExit = true;
//                sFirstTimePressBackBtn = System.currentTimeMillis();
//            } else {
//                long interval = (System.currentTimeMillis() - sFirstTimePressBackBtn) / MILLISECOND_TO_SECOND;
//                if (interval < INTERVAL_TIME) {
//                	ImageWorker mImageWorker = ImageWorker.newInstance(this);
//                	mImageWorker.shutdownThreadPool();
//                	finish();
//                } else {
//                    mIsExit = false;
//                    Toast.makeText(this, getText(R.string.str_again_exit), Toast.LENGTH_SHORT).show();
//                }
//            }
        	//v2.1
        	new AlertDialog.Builder(this)
        	.setIcon(R.drawable.ic_launcher_64)
            .setTitle(getString(R.string.exit_rate_title))
            .setMessage(getString(R.string.exit_rate_content))
            .setCancelable(true)
            .setNeutralButton("Exit", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					ImageWorker mImageWorker = ImageWorker.newInstance(getApplicationContext());
                	mImageWorker.shutdownThreadPool();
            		finish();
            		android.os.Process.killProcess(android.os.Process.myPid());
    				System.exit(0);
				}
			})
            .setPositiveButton("Rate", new DialogInterface.OnClickListener() {
            	 
				public void onClick(DialogInterface dialog, int which) {
            		MoreActivity.openMarketDetails(getApplicationContext(), getPackageName());
					ImageWorker mImageWorker = ImageWorker.newInstance(getApplicationContext());
                	mImageWorker.shutdownThreadPool();
					finish();
					android.os.Process.killProcess(android.os.Process.myPid());
					System.exit(0);
            	}
            }).show();
        	
        	return false;
        }
        return super.dispatchKeyEvent(event);
	};
	

	private void setupIntent() {
		mTabHost = getTabHost();
		mTabHost.addTab(buildTabSpec(TAB_TAG_HOME, R.string.category_1,	0, mHomeItent));
		mTabHost.addTab(buildTabSpec(TAB_TAG_SED, R.string.category_2, 0, mLegItent));
		mTabHost.addTab(buildTabSpec(TAB_TAG_POSI, R.string.category_2, 0, mPosIntent));
		mTabHost.addTab(buildTabSpec(TAB_TAG_MORE, R.string.category_more, 0, mMoreIntent));
	}
	
	private TabHost.TabSpec buildTabSpec(String tag, int resLabel, int resIcon,
			final Intent content) {
		return mTabHost
				.newTabSpec(tag)
				.setIndicator(getString(resLabel),null)
				.setContent(content);
	}
	
	public static void setCurrentTabByTag(String tab) {
		mTabHost.setCurrentTabByTag(tab);
	}
	
	public void onClick(View v) {
		if (mCurTabId == v.getId()) {
			return;
		}
		int checkedId = v.getId();
		final boolean o;
		if (mCurTabId < checkedId)
			o = true;
		else
			o = false;
		
		if (o)
			mTabHost.getCurrentView().startAnimation(left_out);
		else
			mTabHost.getCurrentView().startAnimation(right_out);
		
		switch (checkedId) {
		case R.id.main_pigu:
			mTabHost.setCurrentTabByTag(TAB_TAG_HOME);
			break;
		case R.id.leg:
			mTabHost.setCurrentTabByTag(TAB_TAG_SED);
			break;
		case R.id.position:
			mTabHost.setCurrentTabByTag(TAB_TAG_POSI);
			break;
		case R.id.more:
			mTabHost.setCurrentTabByTag(TAB_TAG_MORE);
			break;
		default:
			break;
		}

		if (o)
			mTabHost.getCurrentView().startAnimation(left_in);
		else
			mTabHost.getCurrentView().startAnimation(right_in);
		mCurTabId = checkedId;
	}
	
	
	
	
}
