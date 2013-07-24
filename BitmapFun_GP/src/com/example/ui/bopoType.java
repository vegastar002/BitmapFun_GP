package com.example.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;

import cn.waps.AppConnect;

import com.android.hardcore.crashreport.CrashReportingApplication;
import com.free.hardcore.wp9.R;
import com.szy.update.TypegifView;

public class bopoType extends Activity implements OnClickListener{
    /** Called when the activity is first created. */
	
	TypegifView givi;
	RadioButton normalf, fast, veryfast;
	byte[] svdata;
	public String sname = "";
	Button left_array, right_array, category_add_category;
	ImageButton category_finish;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bopo);
        
        TypegifView gv = new TypegifView(bopoType.this);
        
        givi = (TypegifView) findViewById(R.id.givi);
        givi.delta = 1;
        
        left_array = (Button) findViewById(R.id.left_array);
        right_array = (Button) findViewById(R.id.right_array);
        category_add_category = (Button) findViewById(R.id.category_add_category);
        category_finish = (ImageButton) findViewById(R.id.category_finish);
        
        left_array.setOnClickListener(this);
        right_array.setOnClickListener(this);
        category_add_category.setOnClickListener(this);
        category_finish.setOnClickListener(this);
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.left_array:
			if ( givi.delta > 1 ){
				givi.delta--;
			}
			break;
		case R.id.right_array:
			if ( givi.delta < 10 ){
				givi.delta++;
			}
			break;
		case R.id.category_add_category:
			finish();
			break;
		case R.id.category_finish:
			AppConnect.getInstance(bopoType.this);
			AppConnect.getInstance(bopoType.this).showAppOffers(bopoType.this);
			break;

		default:
			break;
		}
	}
    
}