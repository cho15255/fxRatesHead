package com.example.ratesheads;

import android.os.Bundle;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

public class MainActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		final WindowManager.LayoutParams param=new WindowManager.LayoutParams();
		param.flags=WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		final ImageView view=new ImageView(this);
		view.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		view.setImageResource(R.drawable.ic_launcher);
		final ViewGroup parent=(ViewGroup)view.getParent();
		if(parent!=null)
		  parent.removeView(view);
		param.format=PixelFormat.RGBA_8888;
		param.type=WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		param.gravity=Gravity.CENTER;
		param.width=parent!=null?LayoutParams.WRAP_CONTENT:view.getLayoutParams().width;
		param.height=parent!=null?LayoutParams.WRAP_CONTENT:view.getLayoutParams().height;
		final WindowManager wmgr=(WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		wmgr.addView(view,param);
		
		view.setOnTouchListener(new View.OnTouchListener() {
			  private int initialX;
			  private int initialY;
			  private float initialTouchX;
			  private float initialTouchY;

			  @Override 
			  public boolean onTouch(View v, MotionEvent event) {
			    switch (event.getAction()) {
			      case MotionEvent.ACTION_DOWN:
			        initialX = param.x;
			        initialY = param.y;
			        initialTouchX = event.getRawX();
			        initialTouchY = event.getRawY();
			        return true;
			      case MotionEvent.ACTION_UP:
			        return true;
			      case MotionEvent.ACTION_MOVE:
			        param.x = initialX + (int) (event.getRawX() - initialTouchX);
			        param.y = initialY + (int) (event.getRawY() - initialTouchY);
			        wmgr.updateViewLayout(view, param);
			        return true;
			    }
			    return false;
			  }
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	

}
