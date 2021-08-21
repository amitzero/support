package com.zero.support.lock;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.zero.support.R;

public class PinView extends LinearLayout
{
	private final Context context;
	private CallBack callback;
	private final LinearLayout buttonsBackground;
	private final View pin1, pin2, pin3, pin4, pin5, pin6;
	private String code = "";

    public PinView(Context context)
	{
        this(context, null);
    }

    public PinView(Context context, AttributeSet attrs)
	{
        super(context, attrs);

		this.context = context;

		  TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PinView, 0, 0);
		// String titleText = a.getString(R.styleable.PinView_titleText);
		 @SuppressWarnings("ResourceAsColor")
		int color = a.getColor(R.styleable.PinView_buttonsBackground, android.R.color.black);
        a.recycle();

        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(com.zero.support.R.layout.support_buttons, this, true);

		buttonsBackground = findViewById(com.zero.support.R.id.buttonsLinearLayoutBackground);
		buttonsBackground.setBackgroundColor(color);


		Button bt0, bt1, bt2, bt3, bt4, bt5, bt6, bt7, bt8, bt9, btOk;

		bt0 = findViewById(com.zero.support.R.id.button0);
		bt1 = findViewById(com.zero.support.R.id.button1);
		bt2 = findViewById(com.zero.support.R.id.button2);
		bt3 = findViewById(com.zero.support.R.id.button3);
		bt4 = findViewById(com.zero.support.R.id.button4);
		bt5 = findViewById(com.zero.support.R.id.button5);
		bt6 = findViewById(com.zero.support.R.id.button6);
		bt7 = findViewById(com.zero.support.R.id.button7);
		bt8 = findViewById(com.zero.support.R.id.button8);
		bt9 = findViewById(com.zero.support.R.id.button9);

		btOk = findViewById(com.zero.support.R.id.buttonClear);
		btOk.setOnClickListener(p1 -> setPinEmpty());

		OnClickListener buttonListener = v -> setPinView(Integer.parseInt(((Button) v).getText().toString()));

		bt0.setOnClickListener(buttonListener);
		bt1.setOnClickListener(buttonListener);
		bt2.setOnClickListener(buttonListener);
		bt3.setOnClickListener(buttonListener);
		bt4.setOnClickListener(buttonListener);
		bt5.setOnClickListener(buttonListener);
		bt6.setOnClickListener(buttonListener);
		bt7.setOnClickListener(buttonListener);
		bt8.setOnClickListener(buttonListener);
		bt9.setOnClickListener(buttonListener);

		pin1 = findViewById(com.zero.support.R.id.pin1);
		pin2 = findViewById(com.zero.support.R.id.pin2);
		pin3 = findViewById(com.zero.support.R.id.pin3);
		pin4 = findViewById(com.zero.support.R.id.pin4);
		pin5 = findViewById(com.zero.support.R.id.pin5);
		pin6 = findViewById(com.zero.support.R.id.pin6);
		
	}

	private void setPinView(int i)
	{
		code = code + i;
		int length = code.length();
		switch (length)
		{
			case 1:
				pin1.setBackgroundResource(com.zero.support.R.drawable.pin_bg_active);
				break;
			case 2:
				pin2.setBackgroundResource(com.zero.support.R.drawable.pin_bg_active);
				break;
			case 3:
				pin3.setBackgroundResource(com.zero.support.R.drawable.pin_bg_active);
				break;
			case 4:
				pin4.setBackgroundResource(com.zero.support.R.drawable.pin_bg_active);
				break;
			case 5:
				pin5.setBackgroundResource(com.zero.support.R.drawable.pin_bg_active);
				break;
			case 6:
				pin6.setBackgroundResource(com.zero.support.R.drawable.pin_bg_active);
				ObjectAnimator oa = new ObjectAnimator();
				oa.setTarget(new View(context));
				oa.setPropertyName("alpha");
				oa.setFloatValues(1, 1);
				oa.setDuration(100);
				oa.addListener(new ObjectAnimator.AnimatorListener(){

						@Override
						public void onAnimationStart(Animator p1) {}
						@Override
						public void onAnimationEnd(Animator p1)
						{
							if (callback != null)
								callback.onPin(code);
						}
						@Override
						public void onAnimationCancel(Animator p1) {}
						@Override
						public void onAnimationRepeat(Animator p1) {}
					});
					oa.start();
				break;
			default:
				code = "" + i;
				pin2.setBackgroundResource(com.zero.support.R.drawable.pin_bg);
				pin3.setBackgroundResource(com.zero.support.R.drawable.pin_bg);
				pin4.setBackgroundResource(com.zero.support.R.drawable.pin_bg);
				pin5.setBackgroundResource(com.zero.support.R.drawable.pin_bg);
				pin6.setBackgroundResource(com.zero.support.R.drawable.pin_bg);
				break;
		}
	}

	public void setCallBack(CallBack callback)
	{
		this.callback = callback;
	}
	
	public void setButtonsBackground(int color)
	{
		buttonsBackground.setBackgroundColor(color);
	}

	public void setPinEmpty()
	{
		code = "";
		pin1.setBackgroundResource(com.zero.support.R.drawable.pin_bg);
		pin2.setBackgroundResource(com.zero.support.R.drawable.pin_bg);
		pin3.setBackgroundResource(com.zero.support.R.drawable.pin_bg);
		pin4.setBackgroundResource(com.zero.support.R.drawable.pin_bg);
		pin5.setBackgroundResource(com.zero.support.R.drawable.pin_bg);
		pin6.setBackgroundResource(com.zero.support.R.drawable.pin_bg);
	}

	public interface CallBack
	{
		public void onPin(String pin);
	}
}
