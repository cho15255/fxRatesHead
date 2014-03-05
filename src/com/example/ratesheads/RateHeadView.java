package com.example.ratesheads;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.oanda.fxtrade.sdk.FxClient;
import com.oanda.fxtrade.sdk.Price;
import com.oanda.fxtrade.sdk.network.PriceListener;

import android.R.color;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class RateHeadView extends View {
	private TextView mTextView;
	private WindowManager mWindowManager;

	private Button settingButton;
	private Button tradeButton;
	
	private WindowManager.LayoutParams settingParam;
	private WindowManager.LayoutParams tradeParam;

	private static Boolean isRateVisible;
	private WindowManager.LayoutParams headParam;
	
	private boolean headExist;
	
	private BigDecimal currentBid;
	private BigDecimal currentAsk;
	private BigDecimal newBid;
	private BigDecimal newAsk;
	private static Boolean isHeadMoved;

	
	public RateHeadView(final Context context, WindowManager windowManager) {
		super (context);
		DisplayMetrics displaymetrics = new DisplayMetrics();
		windowManager.getDefaultDisplay().getMetrics(displaymetrics);
		final int screenHeight = displaymetrics.heightPixels;
		final int screenWidth = displaymetrics.widthPixels;
		
		mTextView = new TextView(context);
		
		isRateVisible = false;
		isHeadMoved = false;
		
		
		headParam = new WindowManager.LayoutParams();

		headParam.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		mTextView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		headParam.format = PixelFormat.RGBA_8888;
		headParam.gravity = Gravity.CENTER|Gravity.RIGHT;
		headParam.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		headParam.width = LayoutParams.WRAP_CONTENT;
		headParam.height = LayoutParams.WRAP_CONTENT;
		mWindowManager = windowManager;
		mWindowManager.addView(mTextView, headParam);
		headExist = true;
		
		settingParam = new WindowManager.LayoutParams();
		settingParam.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		settingButton = new Button(context);
		settingButton.setLayoutParams(new LayoutParams(screenWidth / 2, 130));
		settingButton.setBackgroundColor(color.transparent);
		settingButton.setText("Settings");
		settingButton.setTextColor(getResources().getColor(R.color.oanda_green));
		settingButton.setGravity(Gravity.CENTER);
		settingButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent mainIntent = new Intent(context, MainActivity.class);
				context.startActivity(mainIntent);
			}
		});
		
		settingParam.format = PixelFormat.RGBA_8888;
		settingParam.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		settingParam.gravity = Gravity.TOP | Gravity.LEFT;
		settingParam.width = settingButton.getLayoutParams().width;
		settingParam.height = settingButton.getLayoutParams().height;

		tradeParam = new WindowManager.LayoutParams();
		tradeParam.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		tradeButton = new Button(context);
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
		
		mTextView.setOnTouchListener(new View.OnTouchListener() {
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
					
					isHeadMoved = false;
					return true;
				case MotionEvent.ACTION_UP:
					headParam.x = initialX;
					mWindowManager.updateViewLayout(mTextView, headParam);
					MainActivity.hideDeleteButton();
					if (!isHeadMoved) {
						if (!isRateVisible) {
							addButtons();
							isRateVisible = true;
						} else {
							removeButtons();
							isRateVisible = false;
						}
					}

					return true;
				case MotionEvent.ACTION_MOVE:
					headParam.x = initialX
							- (int) (event.getRawX() - initialTouchX);
					headParam.y = initialY
							+ (int) (event.getRawY() - initialTouchY);

					MainActivity.showDeleteButton();

					//Log.d("View", headParam.y + " " + MainActivity.deleteViewParam.y + " "
							//+ headParam.height);

					if (headParam.y + headParam.height + (screenHeight/2) > MainActivity.deleteViewParam.y) {
						if (isRateVisible) {
							removeButtons();
						}
						headExist = false;
						mWindowManager.removeView(mTextView);
						MainActivity.hideDeleteButton();
					}
					mWindowManager.updateViewLayout(mTextView, headParam);
					
					isHeadMoved = true;
					return true;
				}
				return false;
			}
		});

	}

	public void setText(String text) {
		mTextView.setTextColor(Color.WHITE);
		mTextView.setGravity(Gravity.CENTER);
		mTextView.setPadding(5, 5, 5, 5);
		mTextView.setText(text);
	}
	
	public void updatePrice(Price price){
		if (headExist){
			if (newBid == null || newAsk == null){
				newBid = price.bid();
				newAsk = price.ask();
				currentBid = newBid;
				currentAsk = newAsk;
			} else {
				currentBid = newBid;
				currentAsk = newAsk;
				newBid = price.bid();
				newAsk = price.ask();
			}
			BigDecimal difference = newBid.subtract(currentBid);
			BigDecimal precision = price.instrument().precision();
			difference = difference.movePointRight(precision.intValue()+1);
			Log.i("DIFFERENCE", difference.intValue() + "");
			headParam.y = headParam.y - (difference.intValue()*2);
			final int sign = difference.signum();
			switch (sign) {
			case 1: 
				mTextView.setBackgroundResource(R.drawable.rounded_rectangle_green);
				break;
			case 0: 
				mTextView.setBackgroundResource(R.drawable.rounded_rectangle);
				break;
			case -1:
				mTextView.setBackgroundResource(R.drawable.rounded_rectangle_red);
				break;
			default:
				break;
			}
			mWindowManager.updateViewLayout(mTextView, headParam);
		}
	}
	
	@Override
	public void setOnTouchListener(OnTouchListener l) {
		// TODO Auto-generated method stub
		super.setOnTouchListener(l);
	}
	
	public void addButtons() {
		mWindowManager.addView(settingButton, settingParam);
		mWindowManager.addView(tradeButton, tradeParam);
	}

	public void removeButtons() {
		mWindowManager.removeView(settingButton);
		mWindowManager.removeView(tradeButton);
	}
	
	public void fetchPrices(final FxClient fxSession, final String currentHead) {
		fxSession.getPrices(new PriceListener() {
			@Override
			public void onSuccess(List<Price> prices) {
				for (Price price : prices) {
					updatePrice(price);
					String instrument=getResources().getString(R.string.instrument, price.instrument().displayName());
					String text = instrument + "\n"
							+ "Bid: " + price.bid() + "\n"
							+ "Ask: " + price.ask();
					setText(text);
				}
			}

			@Override
			public void onError(Exception e) {
			}
		}, new ArrayList<String>() {
			{
				add(currentHead);
			}
		});
	}
	public void setRun(final FxClient fxSession,final String currentHead){
		Runnable prices = new Runnable() {
			@Override
			public void run() {
				fetchPrices(fxSession, currentHead);
				MainActivity.postHandleDelay(this);
			}
		};
		MainActivity.postHandle(prices);
	}
}
