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

import com.free.hardcore.wp9.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.position.FragPosition;
import com.android.view.leg.*;

/**
 * Simple FragmentActivity to hold the main {@link ImageGridFragment} and not much else.
 */
public class ImageGridActivity extends FragmentActivity {
    private static final String TAG = "ImageGridFragment";
    Button category_finish, category_add_category;
    TextView title_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
        	Intent miIntent = getIntent();
        	
        	final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        	
        	if ( miIntent.getStringExtra("origin") != null ){
        		
        		if ( miIntent.getStringExtra("origin").equals("home") ){
        			ft.add(android.R.id.content, new ImageGridFragment(), TAG);
        			
        		} else if ( miIntent.getStringExtra("origin").equals("leg") ) {
        			ft.add(android.R.id.content, new ImageGridForLeg(), TAG);
        			
				} else if ( miIntent.getStringExtra("origin").equals("position") ) {
        			ft.add(android.R.id.content, new FragPosition(), TAG);
        			
				}
        		
        		
        	}
            
            
            ft.commit();
        }
    }
}
