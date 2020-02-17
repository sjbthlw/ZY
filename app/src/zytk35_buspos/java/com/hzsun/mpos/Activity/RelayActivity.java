package com.hzsun.mpos.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;

import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.R;
import com.hzsun.mpos.data.LocalInfoRW;
import com.hzsun.mpos.views.SwitchButton;

import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Public.Publicfun.RelayControlDeal;

/**
 * 继电器设置界面
 */
public class RelayActivity extends BaseActivity implements View.OnClickListener, RadioButton.OnCheckedChangeListener, SwitchButton.OnCheckedChangeListener {

    private int iType;
    private int iPosition;
    private String strTitle;

    private SwitchButton sb_relay;
    private RadioButton radio_on;
    private RadioButton radio_off;
    private EditText et_duration;
    private EditText et_count;
    private Button bt_test;
    private Button bt_cancle;
    private Button bt_confirm;
    private int iRet;

    private int iRelayState;  // 继电器 0:关 1:开
    private int iRelayMode;  // 继电器模式 1:常开 0:常闭
    private int iRelayOperTime;  // 继电器动作时长(ms)
    private int iRelayOperCnt;  // 动作脉冲数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relay);
        initData();
        initView();
    }

    private void initData() {
        iType = getIntent().getIntExtra("Type", -1);
        iPosition = getIntent().getIntExtra("Position", -1);
        strTitle = getIntent().getStringExtra("strTitle");

        iRelayState=g_LocalInfo.iRelayState;  // 继电器 0:关 1:开
        iRelayMode=g_LocalInfo.iRelayMode;  // 继电器模式 1:常开 0:常闭
        iRelayOperTime=g_LocalInfo.iRelayOperTime;  // 继电器动作时长(ms)
        iRelayOperCnt=g_LocalInfo.iRelayOperCnt;  // 动作脉冲数
    }

    private void initView() {
        setTitle(strTitle);
        sb_relay = (SwitchButton) findViewById(R.id.sb_relay);
        radio_on = (RadioButton) findViewById(R.id.radio_on);
        radio_off = (RadioButton) findViewById(R.id.radio_off);
        et_duration = (EditText) findViewById(R.id.et_duration);
        et_count = (EditText) findViewById(R.id.et_count);
        bt_test = (Button) findViewById(R.id.bt_test);
        bt_cancle = (Button) findViewById(R.id.bt_cancle);
        bt_confirm = (Button) findViewById(R.id.bt_confirm);

        sb_relay.setOnCheckedChangeListener(this);
        radio_on.setOnCheckedChangeListener(this);
        radio_off.setOnCheckedChangeListener(this);
        bt_test.setOnClickListener(this);
        bt_cancle.setOnClickListener(this);
        bt_confirm.setOnClickListener(this);

        if(iRelayState==0)
            sb_relay.setChecked(false);
        else
            sb_relay.setChecked(true);

        if(iRelayMode==1){
            radio_on.setChecked(true);
            radio_off.setChecked(false);
        }else{
            radio_on.setChecked(false);
            radio_off.setChecked(true);
        }
        et_duration.setText(""+iRelayOperTime);
        et_count.setText(""+iRelayOperCnt);
    }

    //Activity返回参数
    private void PutResult(int iType, int iRet) {
        Intent intent = new Intent();
        intent.putExtra("result", iRet);
        setResult(iType, intent);

        g_LocalInfo.iRelayState=iRelayState;  // 继电器 0:关 1:开
        g_LocalInfo.iRelayMode=iRelayMode;  // 继电器模式 1:常开 0:常闭

        String strRelayOperTime = et_duration.getText().toString();
        g_LocalInfo.iRelayOperTime=Integer.parseInt(strRelayOperTime);  // 继电器动作时长(ms)

        String strRelayOperCnt = et_count.getText().toString();
        g_LocalInfo.iRelayOperCnt=Integer.parseInt(strRelayOperCnt);  // 动作脉冲数

        //初始化继电器
        if(g_LocalInfo.iRelayState==1){
            if(g_LocalInfo.iRelayMode==0)// 继电器模式 0:常闭 1:常开
                Publicfun.RunShellCmd("echo 0 > /sys/class/relay/control");
            else
                Publicfun.RunShellCmd("echo 1 > /sys/class/relay/control");
        }
        LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
        finish();//此处一定要调用finish()方法
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_test:
                String strRelayOperTime = et_duration.getText().toString();
                iRelayOperTime=Integer.parseInt(strRelayOperTime);  // 继电器动作时长(ms)

                String strRelayOperCnt = et_count.getText().toString();
                iRelayOperCnt=Integer.parseInt(strRelayOperCnt);  // 动作脉冲数
                RelayControlDeal( iRelayState, iRelayMode, iRelayOperTime, iRelayOperCnt);
                break;
            case R.id.bt_cancle:
                finish();
                break;
            case R.id.bt_confirm:
                PutResult(iType, iRet);
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.radio_on:
                if(isChecked)
                    iRelayMode=1;
                break;
            case R.id.radio_off:
                if(isChecked)
                    iRelayMode=0;
                break;
        }
    }

    /**
     * @param buttonView
     * @param isChecked  true 开,false 关
     */
    @Override
    public void onCheckedChanged(SwitchButton buttonView, boolean isChecked) {
        iRelayState = isChecked ? 1 : 0;

        if (!isChecked){
            radio_on.setEnabled(false);
            radio_off.setEnabled(false);
            et_duration.setEnabled(false);
            et_count.setEnabled(false);
        }else{
            radio_on.setEnabled(true);
            radio_off.setEnabled(true);
            et_duration.setEnabled(true);
            et_count.setEnabled(true);
        }
    }
}
