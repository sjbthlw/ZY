package com.hzsun.mpos.Activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.hzsun.mpos.R;

import static com.hzsun.mpos.Global.Global.g_WorkInfo;

public class BaseActivity extends AppCompatActivity {

    private FrameLayout parentLinearLayout;//把父类activity和子类activity的view都add到这里
    private ImageView ivBack;
    private TextView tv_Title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initContentView(R.layout.activity_base);
        initViews();
    }

    private void initViews() {
        tv_Title = ((TextView) findViewById(R.id.tv_Title));
    }


    public void setTitle(String title) {
        if (!TextUtils.isEmpty(title)) {
            tv_Title.setText(title);
        }
    }


    /**
     * 初始化contentview
     */
    private void initContentView(int layoutResID) {
        ViewGroup viewGroup = (ViewGroup) findViewById(android.R.id.content);
        viewGroup.removeAllViews();
        parentLinearLayout = new FrameLayout(this);
//        parentLinearLayout.setOrientation(LinearLayout.VERTICAL);
        viewGroup.addView(parentLinearLayout);
        LayoutInflater.from(this).inflate(layoutResID, parentLinearLayout, true);
        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void setContentView(int layoutResID) {
        LayoutInflater.from(this).inflate(layoutResID, parentLinearLayout, true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        g_WorkInfo.lngPowerSaveCnt=System.currentTimeMillis();
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        // TODO Auto-generated method stub
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
        return super.onTouchEvent(event);
    }

}
