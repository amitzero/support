package com.zero.support;


import android.view.MotionEvent;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

public class DragView implements View.OnTouchListener
		{
			private float initialX;
			private float initialY;
			private float initialTouchX;
			private float initialTouchY;
			private boolean clickable = true;

			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				switch (event.getAction()) 
				{
					case MotionEvent.ACTION_DOWN:
						initialX = v.getX();
						initialY = v.getY();
						initialTouchX = event.getRawX();
						initialTouchY = event.getRawY();
						return true;
					case MotionEvent.ACTION_UP:
						int Xdiff = (int) (event.getRawX() - initialTouchX);
						int Ydiff = (int) (event.getRawY() - initialTouchY);
						if (Xdiff < 10 && Ydiff < 10) 
						{
							if(clickable)
							{
								v.callOnClick();
							}
							return false;
						}
						return true;
					case MotionEvent.ACTION_MOVE:
						float x = initialX + (int) (event.getRawX() - initialTouchX);
						float y = initialY + (int) (event.getRawY() - initialTouchY);
						v.setX(x);
						v.setY(y);
						clickable = false;
						setClickable();
						return true;
				}
				return false;
			}
			
			private void setClickable()
			{
				Timer t = new Timer();
				TimerTask tt = new TimerTask()
				{
					@Override
					public void run()
					{
						clickable = true;
					}
				};
				t.schedule(tt, 200);
			}
		};
