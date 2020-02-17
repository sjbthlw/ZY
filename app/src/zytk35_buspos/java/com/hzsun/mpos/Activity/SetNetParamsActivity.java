package com.hzsun.mpos.Activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hzsun.mpos.Logger.LogUtil;
import com.hzsun.mpos.R;

public class SetNetParamsActivity extends BaseActivity {

    private String TAG = getClass().getSimpleName();
    private ImageView ivCheckMenu;
    private TextView textTitle;
    private EditText etServiceIp;
    private RelativeLayout serviceIp;
    private EditText etServicePort;
    private RelativeLayout servicePort;
    private EditText etLocalIp;
    private RelativeLayout localIp;
    private EditText etZwym;
    private RelativeLayout zwym;
    private EditText etMrwg;
    private RelativeLayout mrwg;
    private EditText etDns;
    private RelativeLayout dns;
    private String twoMenuTitle;
    private String checkMenuTitle;
    private int image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_net_params);
        initData();
        initViews();
        initListener();
    }

    private void initData() {
        twoMenuTitle = getIntent().getStringExtra("TwoMenuTitle");
        checkMenuTitle = getIntent().getStringExtra("SeniorMenuTitle");
        image = getIntent().getIntExtra("Image", -1);
    }

    private void initViews() {
        ivCheckMenu = (ImageView) findViewById(R.id.iv_check_menu);
        textTitle = (TextView) findViewById(R.id.text_title);
        etServiceIp = (EditText) findViewById(R.id.et_service_ip);
        serviceIp = (RelativeLayout) findViewById(R.id.service_ip);
        etServicePort = (EditText) findViewById(R.id.et_service_port);
        servicePort = (RelativeLayout) findViewById(R.id.service_port);
        etLocalIp = (EditText) findViewById(R.id.et_local_ip);
        localIp = (RelativeLayout) findViewById(R.id.local_ip);
        etZwym = (EditText) findViewById(R.id.et_zwym);
        zwym = (RelativeLayout) findViewById(R.id.zwym);
        etMrwg = (EditText) findViewById(R.id.et_mrwg);
        mrwg = (RelativeLayout) findViewById(R.id.mrwg);
        etDns = (EditText) findViewById(R.id.et_dns);
        dns = (RelativeLayout) findViewById(R.id.dns);
        textTitle.setText(twoMenuTitle + " > " + checkMenuTitle);
        ivCheckMenu.setImageResource(image);
    }


    private String etServiceIpText, etServicePortText, etLocalIpText, etZwymText, etMrwgText, etDnsText;

    private void initListener() {
        etServiceIp.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int KeyValue;
                KeyValue = event.getKeyCode();
                //LogUtil.e(TAG, "KeyValue:" + KeyValue);
                if (KeyValue == KeyEvent.KEYCODE_ENTER) {
                    judgeText();
                    return true;
                }
                String str = etServiceIp.getText().toString().replace(".", ",");
                String[] strArr = str.split(",");
                LogUtil.e("长度", strArr[strArr.length - 1].length() + "");
                if (keyCode == KeyEvent.KEYCODE_NUMPAD_MULTIPLY) {
                    etServiceIp.setText("");
                    return true;
                }
                if ((TextUtils.isEmpty(str) || str.endsWith(",")) && keyCode == KeyEvent.KEYCODE_PERIOD) {
                    return true;
                }
                if ((3 == strArr[strArr.length - 1].length()) && ((keyCode >= KeyEvent.KEYCODE_0)
                        && (keyCode <= KeyEvent.KEYCODE_9)) && (!str.endsWith(","))) {
                    return true;
                }
                return false;
            }
        });

        etServicePort.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int KeyValue;
                KeyValue = event.getKeyCode();
                //LogUtil.e(TAG, "KeyValue:" + KeyValue);
                if (KeyValue == KeyEvent.KEYCODE_ENTER) {
                    judgeText();
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_NUMPAD_MULTIPLY) {
                    etServicePort.setText("");
                    return true;
                }
                return false;
            }
        });


        etLocalIp.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int KeyValue;
                KeyValue = event.getKeyCode();
                //LogUtil.e(TAG, "KeyValue:" + KeyValue);
                if (KeyValue == KeyEvent.KEYCODE_ENTER) {
                    judgeText();
                    return true;
                }
                String str = etLocalIp.getText().toString().replace(".", ",");
                String[] strArr = str.split(",");
                LogUtil.e("长度", strArr[strArr.length - 1].length() + "");
                if (keyCode == KeyEvent.KEYCODE_NUMPAD_MULTIPLY) {
                    etLocalIp.setText("");
                    return true;
                }
                if ((TextUtils.isEmpty(str) || str.endsWith(",")) && keyCode == KeyEvent.KEYCODE_PERIOD) {
                    return true;
                }
                if ((3 == strArr[strArr.length - 1].length()) && ((keyCode >= KeyEvent.KEYCODE_0)
                        && (keyCode <= KeyEvent.KEYCODE_9)) && (!str.endsWith(","))) {
                    return true;
                }
                return false;
            }
        });

        etZwym.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int KeyValue;
                KeyValue = event.getKeyCode();
                //LogUtil.e(TAG, "KeyValue:" + KeyValue);
                if (KeyValue == KeyEvent.KEYCODE_ENTER) {
                    judgeText();
                    return true;
                }
                String str = etZwym.getText().toString().replace(".", ",");
                String[] strArr = str.split(",");
                LogUtil.e("长度", strArr[strArr.length - 1].length() + "");
                if (keyCode == KeyEvent.KEYCODE_NUMPAD_MULTIPLY) {
                    etZwym.setText("");
                    return true;
                }
                if ((TextUtils.isEmpty(str) || str.endsWith(",")) && keyCode == KeyEvent.KEYCODE_PERIOD) {
                    return true;
                }
                if ((3 == strArr[strArr.length - 1].length()) && ((keyCode >= KeyEvent.KEYCODE_0)
                        && (keyCode <= KeyEvent.KEYCODE_9)) && (!str.endsWith(","))) {
                    return true;
                }
                return false;
            }
        });

        etMrwg.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int KeyValue;
                KeyValue = event.getKeyCode();
                //LogUtil.e(TAG, "KeyValue:" + KeyValue);
                if (KeyValue == KeyEvent.KEYCODE_ENTER) {
                    judgeText();
                    return true;
                }
                String str = etMrwg.getText().toString().replace(".", ",");
                String[] strArr = str.split(",");
                LogUtil.e("长度", strArr[strArr.length - 1].length() + "");
                if (keyCode == KeyEvent.KEYCODE_NUMPAD_MULTIPLY) {
                    etMrwg.setText("");
                    return true;
                }
                if ((TextUtils.isEmpty(str) || str.endsWith(",")) && keyCode == KeyEvent.KEYCODE_PERIOD) {
                    return true;
                }
                if ((3 == strArr[strArr.length - 1].length()) && ((keyCode >= KeyEvent.KEYCODE_0)
                        && (keyCode <= KeyEvent.KEYCODE_9)) && (!str.endsWith(","))) {
                    return true;
                }
                return false;
            }
        });

        etDns.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int KeyValue;
                KeyValue = event.getKeyCode();
                //LogUtil.e(TAG, "KeyValue:" + KeyValue);
                if (KeyValue == KeyEvent.KEYCODE_ENTER) {
                    judgeText();
                    return true;
                }
                String str = etDns.getText().toString().replace(".", ",");
                String[] strArr = str.split(",");
                LogUtil.e("长度", strArr[strArr.length - 1].length() + "");
                if (keyCode == KeyEvent.KEYCODE_NUMPAD_MULTIPLY) {
                    etDns.setText("");
                    return true;
                }
                if ((TextUtils.isEmpty(str) || str.endsWith(",")) && keyCode == KeyEvent.KEYCODE_PERIOD) {
                    return true;
                }
                if ((3 == strArr[strArr.length - 1].length()) && ((keyCode >= KeyEvent.KEYCODE_0)
                        && (keyCode <= KeyEvent.KEYCODE_9)) && (!str.endsWith(","))) {
                    return true;
                }
                return false;
            }
        });
    }

    private void judgeText() {
        etServiceIpText = etServiceIp.getText().toString();
        etServicePortText = etServicePort.getText().toString();
        etLocalIpText = etLocalIp.getText().toString();
        etZwymText = etZwym.getText().toString();
        etMrwgText = etMrwg.getText().toString();
        etDnsText = etDns.getText().toString();
    }

}
