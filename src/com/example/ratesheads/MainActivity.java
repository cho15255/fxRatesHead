package com.example.ratesheads;

import java.util.ArrayList;
import java.util.List;

import android.R.color;
import android.R.integer;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.oanda.fxtrade.sdk.FxClient;
import com.oanda.fxtrade.sdk.Price;
import com.oanda.fxtrade.sdk.User;
import com.oanda.fxtrade.sdk.network.LoginListener;
import com.oanda.fxtrade.sdk.network.PriceListener;

public class MainActivity extends Activity implements
		GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

	private GestureDetectorCompat mDetector;
	private WindowManager mWindowManager;
	private static View mDeleteView;

	private ArrayAdapter<String> mRatesAdapter;
	private ProgressDialog mDialog;
	private Handler handler;

	private static final int POLL_INTERVAL = 1000; // 3 seconds
	private static final String USERNAME = "mobileusa";
	private static final String PASSWORD = "password1";
	private static final String API_KEY = "0325ee6232373738";


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

		stripesDialog = new Dialog(this);
		stripesDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		triangleDialog = new Dialog(this);
		triangleDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		Button stripeOptionsButton = (Button) findViewById(R.id.stripesButton);
		stripeOptionsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				stripesDialog.setContentView(R.layout.dialog);
				stripesDialog.setCancelable(false);
				stripesDialog.show();

				Button stripeOptionsDialogButton = (Button) stripesDialog
						.findViewById(R.id.dialog_button);
				stripeOptionsDialogButton.setText("Add Stripe");
				stripeOptionsDialogButton
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								stripesDialog.dismiss();
								addHead();
							}
						});
			}
		});

		Button triangleOptionsButton = (Button) findViewById(R.id.triangleButton);
		triangleOptionsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				triangleDialog.setContentView(R.layout.dialog);
				triangleDialog.setCancelable(false);
				triangleDialog.show();

				Button triangleOptionsDialogButton = (Button) triangleDialog
						.findViewById(R.id.dialog_button);
				triangleOptionsDialogButton.setText("Add Triangle");
				triangleOptionsDialogButton
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								triangleDialog.dismiss();
								addHead();
							}
						});
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
	}

	private void addHead() {
		
		final RateHeadView headView = new RateHeadView(this, mWindowManager);

		Runnable prices = new Runnable() {
			@Override
			public void run() {
				fetchPrices(headView);
				handler.postDelayed(this, POLL_INTERVAL);
			}
		};
		handler.post(prices);
	}

	public void fetchPrices(final RateHeadView headView) {
		mFxSession.getPrices(new PriceListener() {
			@Override
			public void onSuccess(List<Price> prices) {
				for (Price price : prices) {
					headView.setText(price.toString());
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

}
