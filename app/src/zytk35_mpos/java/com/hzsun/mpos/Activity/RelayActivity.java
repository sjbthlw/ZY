package com.hzsun.mpos.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.R;
import com.hzsun.mpos.views.SwitchButton;
import com.hzsun.mpos.data.LocalInfoRW;

import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Public.Publicfun.RelayControlDeal;


/**
 * 继电器设置界面
 */
public class RelayActivity extends BaseActivity implements  RadioButton.OnCheckedChangeListener, SwitchButton.OnCheckedChangeListener {

    private RadioButton radio_on;
    private RadioButton radio_off;
    private EditText et_duration;
    private EditText et_count;
    private TextView tv_tip;

    private int iRelayMode;  // 继电器模式 1:常开 0:常闭
    private int iRelayOperTime;  // 继电器动作时长(ms)
    private int iRelayOperCnt;  // 动作脉冲数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relay);
        isShowPerScreen(true);
        initData();
        initView();
    }

    private void initData() {
        iRelayMode = g_LocalInfo.iRelayMode;  // 继电器模式 1:常开 0:常闭
        iRelayOperTime = g_LocalInfo.iRelayOperTime;  // 继电器动作时长(ms)
        iRelayOperCnt = g_LocalInfo.iRelayOperCnt;  // 动作脉冲数
    }

    private void initView() {
        radio_on = (RadioButton) findViewById(R.id.radio_on);
        radio_off = (RadioButton) findViewById(R.id.radio_off);
        et_duration = (EditText) findViewById(R.id.et_duration);
        et_count = (EditText) findViewById(R.id.et_count);
        tv_tip = (TextView) findViewById(R.id.tv_tip);
        radio_on.setOnCheckedChangeListener(this);
        radio_off.setOnCheckedChangeListener(this);
        if (iRelayMode == 1) {
            radio_on.setChecked(true);
            radio_off.setChecked(false);
        } else {
            radio_on.setChecked(false);
            radio_off.setChecked(true);
        }
        et_duration.setText(String.valueOf(iRelayOperTime));
        et_count.setText(String.valueOf(iRelayOperCnt));
        et_count.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_MULTIPLY) {
                        et_count.setText("");
                        return true;
                    }
                }
                return false;
            }
        });
        et_duration.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_MULTIPLY) {
                        et_duration.setText("");
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void displayTips() {
        int visibility = (et_duration.isFocused() || et_count.isFocused()) ? View.GONE : View.VISIBLE;
        tv_tip.setVisibility(visibility);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.radio_on:
                if (isChecked)
                    iRelayMode = 1;
                break;
            case R.id.radio_off:
                if (isChecked)
                    iRelayMode = 0;
                break;
        }
    }

    /**
     * @param buttonView
     * @param isChecked  true 开,false 关
     */
    @Override
    public void onCheckedChanged(SwitchButton buttonView, boolean isChecked) {
        if (!isChecked) {
            radio_on.setEnabled(false);
            radio_off.setEnabled(false);
            et_duration.setEnabled(false);
            et_count.setEnabled(false);
        } else {
            radio_on.setEnabled(true);
            radio_off.setEnabled(true);
            et_duration.setEnabled(true);
            et_count.setEnabled(true);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_ENTER:
                    saveRelayParams();
                    finish();
                    break;
                case KeyEvent.KEYCODE_1:
                    if (!et_count.isFocused() && !et_duration.isFocused()) {
                        radio_on.setChecked(true);
                    }
                    break;
                case KeyEvent.KEYCODE_2:
                    if (!et_count.isFocused() && !et_duration.isFocused()) {
                        radio_off.setChecked(true);
                    }
                    break;
                case KeyEvent.KEYCODE_NUMPAD_ADD:
                    RelayTest();//继电器测试
                    break;
            }
        }
        displayTips();
        return super.dispatchKeyEvent(event);
    }

    //测试继电器
    private  void RelayTest() {

        int iRelayState=1;
        String strRelayOperTime = et_duration.getText().toString(); // 继电器动作时长(ms)
        if (TextUtils.isEmpty(strRelayOperTime)) {
            iRelayOperTime = 0;
        } else {
            iRelayOperTime = Integer.parseInt(strRelayOperTime);
        }

        String strRelayOperCnt = et_count.getText().toString();   // 动作脉冲数
        if (TextUtils.isEmpty(strRelayOperCnt)) {
            iRelayOperCnt = 0;
        } else {
            iRelayOperCnt = Integer.parseInt(strRelayOperCnt);
        }

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                RelayControlDeal(iRelayState, iRelayMode, iRelayOperTime, iRelayOperCnt);
            }
        });
        t.start();
    }

    /**
     * 保存继电器参数
     */
    private void saveRelayParams() {
        g_LocalInfo.iRelayMode = iRelayMode;  // 继电器模式 1:常开 0:常闭

        String strRelayOperTime = et_duration.getText().toString(); // 继电器动作时长(ms)
        if (TextUtils.isEmpty(strRelayOperTime)) {
            g_LocalInfo.iRelayOperTime = 0;
        } else {
            g_LocalInfo.iRelayOperTime = Integer.parseInt(strRelayOperTime);
        }

        String strRelayOperCnt = et_count.getText().toString();   // 动作脉冲数
        if (TextUtils.isEmpty(strRelayOperCnt)) {
            g_LocalInfo.iRelayOperCnt = 0;
        } else {
            g_LocalInfo.iRelayOperCnt = Integer.parseInt(strRelayOperCnt);
        }
        //初始化继电器
        if (g_LocalInfo.iRelayState == 1) {
            if (g_LocalInfo.iRelayMode == 0)// 继电器模式 0:常闭 1:常开   rk3399_all:/sys/class/gpio/gpio2 # echo 1 > value
                Publicfun.RunShellCmd("echo 0 > /sys/class/gpio/gpio2/value");
            else
                Publicfun.RunShellCmd("echo 1 > /sys/class/gpio/gpio2/value");
        }
        LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
    }
}
