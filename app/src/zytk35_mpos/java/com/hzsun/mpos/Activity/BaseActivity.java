package com.hzsun.mpos.Activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.hzsun.mpos.R;

import static com.hzsun.mpos.Global.Global.g_ExtDisplay;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;

public class BaseActivity extends AppCompatActivity {

    protected MainPreActivity mainPreActivity;
    private String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        requestWindowFeature(Window.FEATURE_NO_TITLE); //隐藏标题栏
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN; //定义全屏参数
        window.setFlags(flag, flag);  //设置当前窗体显示为全屏
        hideBottomUIMenu();
        setContentView(R.layout.activity_base);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void isShowPerScreen(boolean bool) {
        if (bool) {
            //显示副屏
            if (g_ExtDisplay != null) {
                mainPreActivity = new MainPreActivity(this, g_ExtDisplay);
                mainPreActivity.show();
                hideBottomUIMenu();
            }
        }
    }

    /**
     * 隐藏导航栏
     */
    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int KeyValue;
        KeyValue = event.getKeyCode();
        g_WorkInfo.lngPowerSaveCnt=System.currentTimeMillis();
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        hideBottomUIMenu();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mainPreActivity != null && mainPreActivity.isShowing()) {
            mainPreActivity.dismiss();
        }
    }
}
