package com.zero.support;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public abstract class OnSwipeTouchListener implements OnTouchListener
{

	private final GestureDetector gestureDetector;

	public OnSwipeTouchListener(Context ctx)
	{
		gestureDetector = new GestureDetector(ctx, new GestureListener());
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		return gestureDetector.onTouchEvent(event);
	}

	private final class GestureListener extends SimpleOnGestureListener 
	{

		private boolean getSwipeVerticalTop = true;
		private boolean getSwipeVerticalBottom = true;
		private boolean getSwipeHorizontalRight = true;
		private boolean getSwipeHorizontalLeft = true;

		@Override
		public boolean onDown(MotionEvent e)
		{
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
		{
			if (Math.abs(distanceX) > Math.abs(distanceY))
			{
				if (distanceX < 0)
				{
					getSwipeHorizontalRight = !onScrollRight(-distanceX);
				}
				else
				{
					getSwipeHorizontalLeft = !onScrollLeft(distanceX);
				}
			}
			else
			{
				if (distanceY < 0)
				{
					getSwipeVerticalBottom = !onScrollBottom(-distanceY);
				}
				else
				{
					getSwipeVerticalTop = !onScrollTop(distanceY);
				}
			}
			return !(getSwipeHorizontalLeft && getSwipeVerticalBottom && getSwipeVerticalTop && getSwipeHorizontalRight);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
		{
			boolean result = false;
			try
			{
				float diffY = e2.getY() - e1.getY();
				float diffX = e2.getX() - e1.getX();
				int SWIPE_THRESHOLD = 100;
				int SWIPE_VELOCITY_THRESHOLD = 100;
				if (Math.abs(diffX) > Math.abs(diffY))
				{
					if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD)
					{
						if (diffX > 0 && getSwipeHorizontalRight)
						{
							onSwipeRight();
						}
						else if (getSwipeHorizontalLeft)
						{
							onSwipeLeft();
						}
						result = true;
					}
				}
				else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD)
				{
					if (diffY > 0 && getSwipeVerticalBottom)
					{
						onSwipeBottom();
					}
					else if (getSwipeVerticalTop)
					{
						onSwipeTop();
					}
					result = true;
				}
			}
			catch (Exception ignored)
			{}
			return result;
		}

		@Override
		public boolean onDoubleTap(MotionEvent event)
		{
			onDoubleTaped();
			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e)
		{
			onSingleTaped();
			return true;
		}

	}

	abstract boolean onScrollRight(float X);
	abstract boolean onScrollLeft(float X);
	abstract boolean onScrollTop(float Y);
	abstract boolean onScrollBottom(float Y);
	abstract void onSwipeRight();
	abstract void onSwipeLeft();
	abstract void onSwipeTop();
	abstract void onSwipeBottom();
	abstract void onDoubleTaped();
	abstract void onSingleTaped();

}
