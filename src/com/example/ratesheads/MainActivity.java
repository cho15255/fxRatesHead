package com.example.ratesheads;

import com.oanda.fxtrade.sdk.FxClient;
import com.oanda.fxtrade.sdk.Price;
import com.oanda.fxtrade.sdk.User;
import com.oanda.fxtrade.sdk.network.*;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private ArrayAdapter<String> mRatesAdapter;
	private ProgressDialog mDialog;
	private Handler handler;

	private static final int POLL_INTERVAL = 1000; // 3 seconds
	private TextView view;
	private static final String USERNAME = "mobileusa";
	private static final String PASSWORD = "password1";
	private static final String API_KEY = "0325ee6232373738";
	private FxClient mFxSession;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final WindowManager.LayoutParams param = new WindowManager.LayoutParams();
		param.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		view = new TextView(this);
		view.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		final ViewGroup parent = (ViewGroup) view.getParent();
		if (parent != null)
			parent.removeView(view);
		param.format = PixelFormat.RGBA_8888;
		param.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		param.gravity = Gravity.CENTER;
		param.width = parent != null ? LayoutParams.WRAP_CONTENT : view
				.getLayoutParams().width;
		param.height = parent != null ? LayoutParams.WRAP_CONTENT : view
				.getLayoutParams().height;
		final WindowManager wmgr = (WindowManager) getApplicationContext()
				.getSystemService(Context.WINDOW_SERVICE);
		wmgr.addView(view, param);

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
					param.x = initialX
							+ (int) (event.getRawX() - initialTouchX);
					param.y = initialY
							+ (int) (event.getRawY() - initialTouchY);
					wmgr.updateViewLayout(view, param);
					return true;
				}
				return false;
			}
		});
	}

	public void fetchPrices() {
		mFxSession.getPrices(new PriceListener() {
			@Override
			public void onSuccess(List<Price> prices) {
				for (Price price : prices) {
					view.setText(price.toString());
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
