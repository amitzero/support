package com.zero.support;


import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.Timer;
import java.util.TimerTask;

public class FloatingViewService extends Service
{
	private WindowManager mWindowManager;
	private final View mFloatingView;

	public FloatingViewService(View view) 
	{
		mFloatingView = view;
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@SuppressLint("RtlHardcoded")
	@Override
	public void onCreate() 
	{
		super.onCreate();

		//Add the view to the window.
		final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
			WindowManager.LayoutParams.WRAP_CONTENT,
			WindowManager.LayoutParams.WRAP_CONTENT,
			WindowManager.LayoutParams.TYPE_PHONE,
			WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
			PixelFormat.TRANSLUCENT);

		//Specify the view position
		params.gravity = Gravity.TOP | Gravity.LEFT;        //Initially view will be added to top-left corner
		params.x = 0;
		params.y = 100;

		//Add the view to the window
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		mWindowManager.addView(mFloatingView, params);
		viewsAlpha();

		

		//Drag and move floating view using user's touch action.
		mFloatingView.setOnTouchListener(new View.OnTouchListener()
			{
				private int initialX;
				private int initialY;
				private float initialTouchX;
				private float initialTouchY;


				@SuppressLint("ClickableViewAccessibility")
				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					viewsAlpha();
					switch (event.getAction()) 
					{
						case MotionEvent.ACTION_DOWN:

							//remember the initial position.
							initialX = params.x;
							initialY = params.y;

							//get the touch location
							initialTouchX = event.getRawX();
							initialTouchY = event.getRawY();
							return true;
						case MotionEvent.ACTION_UP:
//							int Xdiff = (int) (event.getRawX() - initialTouchX);
//							int Ydiff = (int) (event.getRawY() - initialTouchY);


//							//The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
//							//So that is click event.
//							if (Xdiff < 10 && Ydiff < 10)
//							{
//								if (isViewCollapsed())
//								{
////									When user clicks on the image view of the collapsed layout,
////									visibility of the collapsed layout will be changed to "View.GONE"
////									and expanded view will become visible.
//									collapsedView.setVisibility(View.GONE);
//									expandedView.setVisibility(View.VISIBLE);
//								}
//							}
							return true;
						case MotionEvent.ACTION_MOVE:
							//Calculate the X and Y coordinates of the view.
							params.x = initialX + (int) (event.getRawX() - initialTouchX);
							params.y = initialY + (int) (event.getRawY() - initialTouchY);


							//Update the layout with new X & Y coordinate
							mWindowManager.updateViewLayout(mFloatingView, params);
							return true;
					}
					return false;
				}
			});
	}
	
	
	public void close()
	{
		stopSelf();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
	}
	
	private void viewsAlpha()
	{
		mFloatingView.setAlpha(1);
		Timer t = new Timer();
		TimerTask tt = new TimerTask()
		{
			@Override
			public void run()
			{
				mFloatingView.setAlpha(0.2f);
			}	
		};
		t.schedule(tt, 5000);
	}
}
