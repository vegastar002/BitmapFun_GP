package com.example.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.free.hardcore.wp9.*;

public class aboutActivity extends Activity {

	Button category_add_category;
	ImageButton category_finish;
	TextView title_text;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_activity);
		
		category_finish = (ImageButton) findViewById(R.id.category_finish);
		category_finish.setVisibility(View.GONE);
		
		
		category_add_category = (Button) findViewById(R.id.category_add_category);
		category_add_category.setOnClickListener(mBackListener);
		
		title_text = (TextView) findViewById(R.id.title_text);
		title_text.setText(getString(R.string.more_about));
	}
	
	private OnClickListener mBackListener = new OnClickListener() {
        
		public void onClick(View v) {
			finish();
		}
        
    };

}
