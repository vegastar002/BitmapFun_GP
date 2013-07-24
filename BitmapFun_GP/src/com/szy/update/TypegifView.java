package com.szy.update;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.android.hardcore.crashreport.CrashReportingApplication;
import com.example.ui.bopoType;
import com.free.hardcore.wp9.R;


public class TypegifView extends View implements Runnable {
	
	TypegifOpenHelper gHelper;
	public int delta;
	String title;
	Bitmap bmp;
	byte [] svtte;
	InputStream iosp;
	private Activity contextgs;
	private String ksname;
	private Activity activityContext;
	
	public TypegifView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init(context);
	}
	
	//construct - refer for xml
	public TypegifView(Context context, AttributeSet attrs) throws FileNotFoundException {
        super(context, attrs);
        init(context);
        
        activityContext = (Activity) context;
        ksname = activityContext.getIntent().getStringExtra("sname");
        
        Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				HttpPost httpRequest = new HttpPost("http://androidvote.duapp.com/CheckAccount");
				List<BasicNameValuePair> Vaparams = new ArrayList<BasicNameValuePair>();
				Vaparams.add(new BasicNameValuePair("flash", ksname));
				try {
					httpRequest.setEntity(new UrlEncodedFormEntity(Vaparams, HTTP.UTF_8));
					HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);

					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						svtte = new byte[1024*1024];
						svtte = EntityUtils.toByteArray( (HttpEntity)httpResponse.getEntity());
//						InputStream sbs = new ByteArrayInputStream(svdata); 
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
        
        thread.start();
		try {
			thread.join();
		} catch (Exception e) {
			// TODO: handle exception
		}
//		
		iosp = new ByteArrayInputStream(svtte);
        
		
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TypegifView);
        int n = ta.getIndexCount();
		
        for(int i =0;i < n;i++){
        	int attr = ta.getIndex(i); 
        	
        	switch(attr){
        	case R.styleable.TypegifView_src:
        		int id = ta.getResourceId(R.styleable.TypegifView_src, 0);
//        		setSrc(id);
        		setSrc(context);
        	case R.styleable.TypegifView_delta:
        		int idelta = ta.getInteger(R.styleable.TypegifView_delta, 1);
        		setDelta(idelta);
        		
        	default:
        		break;
        	}
        	
        }
        
        ta.recycle();
	}
	
	public void init(Context context){
		
	}
	
	
	public void setSrc(int id) {
		gHelper = new TypegifOpenHelper();
		gHelper.read(this.getResources().openRawResource(id));

		bmp = gHelper.getFrame(0);
		Thread updateTimer = new Thread(this);
		updateTimer.start();
	}
	
	public void setSrc(Context context) throws FileNotFoundException{
		try {
			gHelper = new TypegifOpenHelper();
			gHelper.read(iosp);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		 bmp=gHelper.getFrame(0);
		 Thread updateTimer =new Thread(this);
		 updateTimer.start();
	}
	
	
	public void setDelta(int is){
		delta = is;
	}
	
	//to meaure its Width & Height
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec));
    }
	
    private int measureWidth(int measureSpec) {
        return gHelper.getWidth();
    }

    private int measureHeight(int measureSpec) {
        return gHelper.getHeigh();
    }
	
	@SuppressLint("DrawAllocation")
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		canvas.drawBitmap(bmp, 0,0,new Paint());
		bmp=gHelper.nextBitmap();
		
	}

	public void run() {
		// TODO Auto-generated method stub
		while(true){
	    	  try{
	    		this.postInvalidate();
	    		Thread.sleep(gHelper.nextDelay()/delta) ; 
	    	  }catch(Exception ex){
	    		  
	    	  }
	      }
	}

}
