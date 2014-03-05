package com.example.ratesheads;

import java.util.ArrayList;
import java.util.List;

import com.oanda.fxtrade.sdk.FxClient;
import com.oanda.fxtrade.sdk.Price;
import com.oanda.fxtrade.sdk.User;
import com.oanda.fxtrade.sdk.network.LoginListener;
import com.oanda.fxtrade.sdk.network.PriceListener;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
	
    private GestureDetectorCompat mDetector; 
    private WindowManager mWindowManager;
    private View mDeleteView;

	private ArrayAdapter<String> mRatesAdapter;
	private ProgressDialog mDialog;
	private Handler handler;

	private static final int POLL_INTERVAL = 1000; // 3 seconds
	private static final String USERNAME = "mobileusa";
	private static final String PASSWORD = "password1";
	private static final String API_KEY = "0325ee6232373738";
	
	private Boolean isRateVisible;
	private TextView view;
	private Button settingButton;
	private Button tradeButton;
	private FxClient mFxSession;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		isRateVisible = false;
		
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int screenHeight = displaymetrics.heightPixels;
		int screenWidth = displaymetrics.widthPixels;
		
		final WindowManager.LayoutParams headParam = new WindowManager.LayoutParams();
		headParam.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		final ImageView headView = new ImageView(this);
		headView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		headView.setImageResource(R.drawable.ic_launcher);
		ViewGroup parent = (ViewGroup)headView .getParent();
		if (parent != null)
		  parent.removeView(headView );
		headParam.format = PixelFormat.RGBA_8888;
		headParam.gravity = Gravity.TOP;
		headParam.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		headParam.width = (parent != null) ? LayoutParams.WRAP_CONTENT : headView .getLayoutParams().width;
		headParam.height = (parent!=null) ? LayoutParams.WRAP_CONTENT : headView .getLayoutParams().height;
		
		final WindowManager.LayoutParams settingParam = new WindowManager.LayoutParams();
		settingParam.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		settingButton = new Button(this);
		settingButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		parent = (ViewGroup) settingButton.getParent();
		if (parent != null)
			parent.removeView(settingButton);
		settingParam.format = PixelFormat.RGBA_8888;
		settingParam.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		settingParam.gravity = Gravity.TOP | Gravity.LEFT;
		settingParam.width = parent != null ? LayoutParams.WRAP_CONTENT : settingButton
				.getLayoutParams().width;
		settingParam.height = parent != null ? LayoutParams.WRAP_CONTENT : settingButton
				.getLayoutParams().height;
		
		final WindowManager.LayoutParams tradeParam = new WindowManager.LayoutParams();
		tradeParam.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		tradeButton = new Button(this);
		tradeButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		parent = (ViewGroup) tradeButton.getParent();
		if (parent != null)
			parent.removeView(tradeButton);
		tradeParam.format = PixelFormat.RGBA_8888;
		tradeParam.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		tradeParam.gravity = Gravity.TOP | Gravity.RIGHT;
		tradeParam.width = parent != null ? LayoutParams.WRAP_CONTENT : tradeButton
				.getLayoutParams().width;
		tradeParam.height = parent != null ? LayoutParams.WRAP_CONTENT : tradeButton
				.getLayoutParams().height;

		mWindowManager = (WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		mWindowManager.addView(headView ,headParam);

		mDetector = new GestureDetectorCompat(this, this);
		mDetector.setOnDoubleTapListener(this);
		
		mDeleteView = new View(this);
		mDeleteView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 100));
		mDeleteView.setBackgroundColor(Color.RED);
		
		final WindowManager.LayoutParams deleteViewParam = new WindowManager.LayoutParams();
		deleteViewParam.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		deleteViewParam.format = PixelFormat.RGBA_8888;
		deleteViewParam.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		deleteViewParam.width = mDeleteView.getLayoutParams().width;
		deleteViewParam.height = mDeleteView.getLayoutParams().height;
		deleteViewParam.y = screenHeight - mDeleteView.getLayoutParams().height;
		mWindowManager.addView(mDeleteView, deleteViewParam);
		mDeleteView.setVisibility(View.GONE);


		headView.setOnTouchListener(new View.OnTouchListener() {
			  private int initialX;
			  private int initialY;
			  private float initialTouchX;
			  private float initialTouchY;

			  @Override 
			  public boolean onTouch(View v, MotionEvent event) {
			    switch (event.getAction()) {
			      case MotionEvent.ACTION_DOWN:
			        initialX = headParam.x;
			        initialY = headParam.y;
			        initialTouchX = event.getRawX();
			        initialTouchY = event.getRawY();
			        return true;
			      case MotionEvent.ACTION_UP:
			    	  hideDeleteButton();
			    	  
			    	  if (!isRateVisible) {
			    		  mWindowManager.addView(settingButton, settingParam);
			    		  mWindowManager.addView(tradeButton, tradeParam);
			    	  } else {
			    		  mWindowManager.removeView(settingButton);
			    		  mWindowManager.removeView(tradeButton);
			    	  }
			    	  
			    	  isRateVisible = !isRateVisible;
			    	  
			        return true;
			      case MotionEvent.ACTION_MOVE:
			        headParam.x = initialX + (int) (event.getRawX() - initialTouchX);
			        headParam.y = initialY + (int) (event.getRawY() - initialTouchY);
			        mWindowManager.updateViewLayout(headView , headParam);
			        showDeleteButton();
			        
		        	Log.d("View", headParam.y + " " + deleteViewParam.y);
		        	
		        	if (headParam.y + headParam.height > deleteViewParam.y) {
		        		mWindowManager.removeView(headView);
		        		mWindowManager.removeView(settingButton);
			    		mWindowManager.removeView(tradeButton);
			    		isRateVisible = false;
		        		hideDeleteButton();
		        	}

			        return true;
			    }
			    return false;
			  }
		});
			  
		

		handler = new Handler();
		mFxSession = new FxClient(this, API_KEY);

		mDialog = new ProgressDialog(this);
		mDialog.setCancelable(true);
		mDialog.setMessage("Logging in");
		mDialog.show();
		mFxSession.login(USERNAME, PASSWORD, new LoginListener() {
			@Override
			public void onSuccess(User user) {
				mDialog.dismiss();
			}

			@Override
			public void onError(Exception e) {
				mDialog.setMessage("There was a problem logging in");
			}
		});

		Runnable prices = new Runnable() {
			@Override
			public void run() {
				fetchPrices();
				handler.postDelayed(this, POLL_INTERVAL);
			}
		};
		handler.post(prices);
	}

	public void fetchPrices() {
		mFxSession.getPrices(new PriceListener() {
			@Override
			public void onSuccess(List<Price> prices) {
				for (Price price : prices) {
//					view.setText(price.toString());
				}
			}

			@Override
			public void onError(Exception e) {
				Toast.makeText(getApplicationContext(),
						"Problem fetching prices", Toast.LENGTH_SHORT).show();
			}
		}, new ArrayList<String>() {
			{
				add("USD/CAD");
			}
		});
	}
	
	public void showDeleteButton() {
		mDeleteView.setVisibility(View.VISIBLE);
	}
	
	public void hideDeleteButton() {
		mDeleteView.setVisibility(View.GONE);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		this.mDetector.onTouchEvent(event);
		
		return super.onTouchEvent(event);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
        Log.d("Gestures","onFling: " + e1.toString()+ e2.toString());

		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

}
