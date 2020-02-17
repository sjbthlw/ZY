package com.hzsun.mpos.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

@SuppressLint("AppCompatCustomView")
public class MyEditText extends EditText implements View.OnClickListener,
        View.OnFocusChangeListener, TextView.OnEditorActionListener,
        View.OnTouchListener {

    private InputMethodManager mInputManager;

    public MyEditText(Context context) {
        this(context, null);
    }

    public MyEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public MyEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mInputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        setOnClickListener(this);
        setOnFocusChangeListener(this);
        setOnEditorActionListener(this);
        setOnTouchListener(this);
    }


    @Override
    public void onClick(View v) {
        hideSoftInput();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        hideSoftInput();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        hideSoftInput();
        return false;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        hideSoftInput();
        return false;
    }

    private void hideSoftInput() {
        if (mInputManager != null && mInputManager.isActive()) {
            mInputManager.hideSoftInputFromWindow(getApplicationWindowToken(), 0);
        }
    }
}
