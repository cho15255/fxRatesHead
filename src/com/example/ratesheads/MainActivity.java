package com.example.ratesheads;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import android.R.color;
import android.R.integer;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GestureDetectorCompat;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.oanda.fxtrade.sdk.FxClient;
import com.oanda.fxtrade.sdk.Instrument;
import com.oanda.fxtrade.sdk.Price;
import com.oanda.fxtrade.sdk.User;
import com.oanda.fxtrade.sdk.network.InstrumentListener;
import com.oanda.fxtrade.sdk.network.LoginListener;
import com.oanda.fxtrade.sdk.network.PriceListener;

public class MainActivity extends Activity implements
		GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

	private GestureDetectorCompat mDetector;
	private WindowManager mWindowManager;
	private static View mDeleteView;

	public static Button settingButton;
	public static Button tradeButton;
	
	private WindowManager.LayoutParams settingParam;
	private WindowManager.LayoutParams tradeParam;
	
	private ArrayAdapter<String> mRatesAdapter;
	private ProgressDialog mDialog;
	private static Handler handler;

	private static final int POLL_INTERVAL = 1000; // 3 seconds
	private static final String USERNAME = "mobileusa";
	private static final String PASSWORD = "password1";
	private static final String API_KEY = "0325ee6232373738";

	private List<String> instrumentsList;
	private static String currentHead;
	private FxClient mFxSession;
	private Dialog stripesDialog;
	private Dialog triangleDialog;


	public static WindowManager.LayoutParams deleteViewParam;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int screenHeight = displaymetrics.heightPixels;
		int screenWidth = displaymetrics.widthPixels;

		Button stripeOptionsButton = (Button) findViewById(R.id.stripesButton);
		stripeOptionsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				addHead();
			}
		});

		mWindowManager = (WindowManager) getApplicationContext()
				.getSystemService(Context.WINDOW_SERVICE);

		mDetector = new GestureDetectorCompat(this, this);
		mDetector.setOnDoubleTapListener(this);

		mDeleteView = new View(this);
		mDeleteView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				100));
		mDeleteView.setBackgroundColor(Color.RED);

		deleteViewParam = new WindowManager.LayoutParams();
		deleteViewParam.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		deleteViewParam.format = PixelFormat.RGBA_8888;
		deleteViewParam.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		deleteViewParam.width = mDeleteView.getLayoutParams().width;
		deleteViewParam.height = mDeleteView.getLayoutParams().height;
		deleteViewParam.y = screenHeight - mDeleteView.getLayoutParams().height;
		mWindowManager.addView(mDeleteView, deleteViewParam);
		mDeleteView.setVisibility(View.GONE);
		
		settingParam = new WindowManager.LayoutParams();
		settingParam.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		settingButton = new Button(this);
		settingButton.setLayoutParams(new LayoutParams(screenWidth / 2, 130));
		settingButton.setBackgroundColor(color.transparent);
		settingButton.setText("Settings");
		settingButton.setTextColor(getResources().getColor(R.color.oanda_green));
		settingButton.setGravity(Gravity.CENTER);
		settingButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
				startActivity(mainIntent);
				MainActivity.settingButton.setVisibility(View.GONE);
				MainActivity.tradeButton.setVisibility(View.GONE);
			}
		});
		
		settingParam.format = PixelFormat.RGBA_8888;
		settingParam.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		settingParam.gravity = Gravity.TOP | Gravity.LEFT;
		settingParam.width = settingButton.getLayoutParams().width;
		settingParam.height = settingButton.getLayoutParams().height;
		mWindowManager.addView(settingButton, settingParam);
		settingButton.setVisibility(View.GONE);

		tradeParam = new WindowManager.LayoutParams();
		tradeParam.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		tradeButton = new Button(this);
		tradeButton.setLayoutParams(new LayoutParams(screenWidth / 2, 130));
		tradeButton.setBackgroundColor(color.transparent);
		tradeButton.setText("Launch fxTrade");
		tradeButton.setTextColor(getResources().getColor(R.color.oanda_green));
		tradeButton.setGravity(Gravity.CENTER);
		tradeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		
		tradeParam.format = PixelFormat.RGBA_8888;
		tradeParam.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		tradeParam.gravity = Gravity.TOP | Gravity.RIGHT;
		tradeParam.width = tradeButton.getLayoutParams().width;
		tradeParam.height = tradeButton.getLayoutParams().height;
		mWindowManager.addView(tradeButton, tradeParam);
		tradeButton.setVisibility(View.GONE);

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
		final InstrumentList instrumentList = new InstrumentList();
		mFxSession.getInstruments(new InstrumentListener() {
			
			@Override
			public void onSuccess(List<Instrument> arg0) {
				List<String> instrumentListString = new ArrayList<String>(arg0.size()); 
				for (Instrument instrument : arg0){
					instrumentListString.add(instrument.instrument());
				}
				instrumentsList = instrumentListString;
				instrumentList.setInstruments(instrumentListString);
			}
			
			@Override
			public void onError(Exception arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().add(R.id.listFragment, instrumentList, "LIST").commit();
	}
		
		private void addHead() {
			
			final RateHeadView headView = new RateHeadView(this, mWindowManager);
			headView.setRun(mFxSession, currentHead);
		}

	public static void showDeleteButton() {
		mDeleteView.setVisibility(View.VISIBLE);
	}

	public static void hideDeleteButton() {
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
		Log.d("Gestures", "onFling: " + e1.toString() + e2.toString());

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
	
	public static void setHeadInstrument(String instrument){
		currentHead = instrument;
	}
	
	public static void postHandle(Runnable prices){
		handler.post(prices);
	}
	public static void postHandleDelay(Runnable runnable){
		handler.postDelayed(runnable, POLL_INTERVAL);
	}

}
