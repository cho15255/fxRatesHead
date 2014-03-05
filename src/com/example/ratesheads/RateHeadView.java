package com.example.ratesheads;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class RateHeadView extends View {
	private TextView mTextView;
	private WindowManager mWindowManager;

	private Button settingButton;
	private Button tradeButton;
	
	private WindowManager.LayoutParams settingParam;
	private WindowManager.LayoutParams tradeParam;

	private static Boolean isRateVisible;

	
	public RateHeadView(Context context, WindowManager windowManager) {
		super (context);
		mTextView = new TextView(context);
		
		isRateVisible = false;
		
		final WindowManager.LayoutParams headParam;
		headParam = new WindowManager.LayoutParams();

		headParam.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		mTextView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		headParam.format = PixelFormat.RGBA_8888;
		headParam.gravity = Gravity.TOP;
		headParam.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		headParam.width = LayoutParams.WRAP_CONTENT;
		headParam.height = LayoutParams.WRAP_CONTENT;
		
		mWindowManager = windowManager;
		mWindowManager.addView(mTextView, headParam);
		
		settingParam = new WindowManager.LayoutParams();
		settingParam.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		settingButton = new Button(context);
		settingButton.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		settingParam.format = PixelFormat.RGBA_8888;
		settingParam.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		settingParam.gravity = Gravity.TOP | Gravity.LEFT;
		settingParam.width = LayoutParams.WRAP_CONTENT;
		settingParam.height = LayoutParams.WRAP_CONTENT;

		tradeParam = new WindowManager.LayoutParams();
		tradeParam.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		tradeButton = new Button(context);
		tradeButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		tradeParam.format = PixelFormat.RGBA_8888;
		tradeParam.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		tradeParam.gravity = Gravity.TOP | Gravity.RIGHT;
		tradeParam.width = LayoutParams.WRAP_CONTENT;
		tradeParam.height = LayoutParams.WRAP_CONTENT;
		
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
					return true;
				case MotionEvent.ACTION_UP:
					MainActivity.hideDeleteButton();

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
					headParam.x = initialX
							+ (int) (event.getRawX() - initialTouchX);
					headParam.y = initialY
							+ (int) (event.getRawY() - initialTouchY);

					MainActivity.showDeleteButton();

					Log.d("View", headParam.y + " " + MainActivity.deleteViewParam.y + " "
							+ headParam.height);

					if (headParam.y + headParam.height > MainActivity.deleteViewParam.y) {
						if (isRateVisible) {
							mWindowManager.removeView(settingButton);
							mWindowManager.removeView(tradeButton);
						}
						mWindowManager.removeView(mTextView);
						isRateVisible = false;
						MainActivity.hideDeleteButton();
					}
					mWindowManager.updateViewLayout(mTextView, headParam);
					return true;
				}
				return false;
			}
		});

	}

	public void setText(String text) {
		mTextView.setBackgroundColor(Color.RED);
		mTextView.setText(text);
	}
	
	@Override
	public void setOnTouchListener(OnTouchListener l) {
		// TODO Auto-generated method stub
		super.setOnTouchListener(l);
	}
	
}
