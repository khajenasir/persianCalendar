package com.knst.calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;


@SuppressLint("AppCompatCustomView")
public class PersianTextView extends TextView

{

    public PersianTextView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);

        init();

    }

    public PersianTextView(Context context, AttributeSet attrs) {

        super(context, attrs);

        init();

    }

    public PersianTextView(Context context) {

        super(context);

        init();

    }

    public void init() {

        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/lcd.ttf");

        setTypeface(tf ,Typeface.BOLD);

    }

}
