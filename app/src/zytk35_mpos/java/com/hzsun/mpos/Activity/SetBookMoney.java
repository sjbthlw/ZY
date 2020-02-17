package com.hzsun.mpos.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hzsun.mpos.R;

import java.util.ArrayList;

public class SetBookMoney extends BaseActivity {

    private String TAG = getClass().getSimpleName();
    private ImageView ivCheckMenu;
    private TextView textTitle;

    private EditText etBookmoney1;
    private RelativeLayout rlBookmoney1;

    private EditText etBookmoney2;
    private RelativeLayout rlBookmoney2;

    private EditText etBookmoney3;
    private RelativeLayout rlBookmoney3;

    private EditText etBookmoney4;
    private RelativeLayout rlBookmoney4;

    private EditText etBookmoney5;
    private RelativeLayout rlBookmoney5;

    private EditText etBookmoney6;
    private RelativeLayout rlBookmoney6;


    private int iType;  //类型
    private int iPosition; //选中的选项
    private String strTitle;    //标题
    private ArrayList<String> OptionItemList = new ArrayList<String>(); //选项内容
    private ArrayList<String> BookMoneyList = new ArrayList<String>(); //选项内容
    private int[] sInputFlag = new int[6];  //类型
    private int image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setbookmoney);
        isShowPerScreen(true);
        initData();
        initViews();
        initListener();
    }

    private void initData() {
        iType = getIntent().getIntExtra("Type", -1);
        iPosition = getIntent().getIntExtra("Position", -1);
        strTitle = getIntent().getStringExtra("strTitle");
        OptionItemList = getIntent().getStringArrayListExtra("OptionItemList");
        image = getIntent().getIntExtra("Image", -1);
    }

    private void initViews() {

        ivCheckMenu = (ImageView) findViewById(R.id.iv_check_menu);
        textTitle = (TextView) findViewById(R.id.text_title);

        etBookmoney1 = (EditText) findViewById(R.id.et_bookmoney1);
        rlBookmoney1 = (RelativeLayout) findViewById(R.id.bookmoney1);
        etBookmoney2 = (EditText) findViewById(R.id.et_bookmoney2);
        rlBookmoney2 = (RelativeLayout) findViewById(R.id.bookmoney2);
        etBookmoney3 = (EditText) findViewById(R.id.et_bookmoney3);
        rlBookmoney3 = (RelativeLayout) findViewById(R.id.bookmoney3);
        etBookmoney4 = (EditText) findViewById(R.id.et_bookmoney4);
        rlBookmoney4 = (RelativeLayout) findViewById(R.id.bookmoney4);
        etBookmoney5 = (EditText) findViewById(R.id.et_bookmoney5);
        rlBookmoney5 = (RelativeLayout) findViewById(R.id.bookmoney5);
        etBookmoney6 = (EditText) findViewById(R.id.et_bookmoney6);
        rlBookmoney6 = (RelativeLayout) findViewById(R.id.bookmoney6);

        textTitle.setText(strTitle);
        ivCheckMenu.setImageResource(image);

        etBookmoney1.setText(OptionItemList.get(0));
        etBookmoney2.setText(OptionItemList.get(1));
        etBookmoney3.setText(OptionItemList.get(2));
        etBookmoney4.setText(OptionItemList.get(3));
        etBookmoney5.setText(OptionItemList.get(4));
        etBookmoney6.setText(OptionItemList.get(5));
    }

    //Activity返回参数
    private void PutResult(int iType, int iRet) {
        Intent intent = new Intent();
        intent.putExtra("result", iRet);
        intent.putStringArrayListExtra("BookMoneyList", BookMoneyList);
        setResult(iType, intent);
        finish();
    }

    private void initListener() {

        etBookmoney1.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int KeyValue;
                KeyValue = event.getKeyCode();

                if ((KeyValue >= KeyEvent.KEYCODE_0) && (KeyValue <= KeyEvent.KEYCODE_9)) {
                    if (sInputFlag[0] == 0) {
                        sInputFlag[0] = 1;
                        etBookmoney1.setText("");
                    }
                }

                if (KeyValue == KeyEvent.KEYCODE_ENTER) {
                    judgeBookmoneyPara();
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_NUMPAD_MULTIPLY) {
                    etBookmoney1.setText("");
                    return true;
                }
                return false;
            }
        });

        etBookmoney2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int KeyValue;
                KeyValue = event.getKeyCode();
                if ((KeyValue >= KeyEvent.KEYCODE_0) && (KeyValue <= KeyEvent.KEYCODE_9)) {
                    if (sInputFlag[1] == 0) {
                        sInputFlag[1] = 1;
                        etBookmoney2.setText("");
                    }
                }

                if (KeyValue == KeyEvent.KEYCODE_ENTER) {
                    judgeBookmoneyPara();
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_NUMPAD_MULTIPLY) {
                    etBookmoney2.setText("");
                    return true;
                }
                return false;
            }
        });

        etBookmoney3.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int KeyValue;
                KeyValue = event.getKeyCode();
                if ((KeyValue >= KeyEvent.KEYCODE_0) && (KeyValue <= KeyEvent.KEYCODE_9)) {
                    if (sInputFlag[2] == 0) {
                        sInputFlag[2] = 1;
                        etBookmoney3.setText("");
                    }
                }

                if (KeyValue == KeyEvent.KEYCODE_ENTER) {
                    judgeBookmoneyPara();
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_NUMPAD_MULTIPLY) {
                    etBookmoney3.setText("");
                    return true;
                }
                return false;
            }
        });

        etBookmoney4.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int KeyValue;
                KeyValue = event.getKeyCode();
                if ((KeyValue >= KeyEvent.KEYCODE_0) && (KeyValue <= KeyEvent.KEYCODE_9)) {
                    if (sInputFlag[3] == 0) {
                        sInputFlag[3] = 1;
                        etBookmoney4.setText("");
                    }
                }

                if (KeyValue == KeyEvent.KEYCODE_ENTER) {
                    judgeBookmoneyPara();
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_NUMPAD_MULTIPLY) {
                    etBookmoney4.setText("");
                    return true;
                }
                return false;
            }
        });

        etBookmoney5.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int KeyValue;
                KeyValue = event.getKeyCode();
                if ((KeyValue >= KeyEvent.KEYCODE_0) && (KeyValue <= KeyEvent.KEYCODE_9)) {
                    if (sInputFlag[4] == 0) {
                        sInputFlag[4] = 1;
                        etBookmoney5.setText("");
                    }
                }

                if (KeyValue == KeyEvent.KEYCODE_ENTER) {
                    judgeBookmoneyPara();
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_NUMPAD_MULTIPLY) {
                    etBookmoney5.setText("");
                    return true;
                }
                return false;
            }
        });

        etBookmoney6.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int KeyValue;
                KeyValue = event.getKeyCode();
                if ((KeyValue >= KeyEvent.KEYCODE_0) && (KeyValue <= KeyEvent.KEYCODE_9)) {
                    if (sInputFlag[5] == 0) {
                        sInputFlag[5] = 1;
                        etBookmoney6.setText("");
                    }
                }

                if (KeyValue == KeyEvent.KEYCODE_ENTER) {
                    judgeBookmoneyPara();
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_NUMPAD_MULTIPLY) {
                    etBookmoney6.setText("");
                    return true;
                }
                return false;
            }
        });
    }

    //判断网络参数
    private void judgeBookmoneyPara() {

        BookMoneyList.add(etBookmoney1.getText().toString());
        BookMoneyList.add(etBookmoney2.getText().toString());
        BookMoneyList.add(etBookmoney3.getText().toString());
        BookMoneyList.add(etBookmoney4.getText().toString());
        BookMoneyList.add(etBookmoney5.getText().toString());
        BookMoneyList.add(etBookmoney6.getText().toString());
        PutResult(iType, 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        int KeyValue;
        KeyValue = event.getKeyCode();
        //Log.i(TAG, "KeyValue:" + KeyValue);
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                PutResult(100, 0);//后退不处理返回值
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

//    //记录用户首次点击返回键的时间
//    private long firstTime = 0;
//    public void onBackPressed()
//    {
//        //不执行回退功能
////        long secondTime = System.currentTimeMillis();
////        if (secondTime - firstTime > 2000) {
////            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
////            firstTime = secondTime;
////        } else {
////            super.onBackPressed();
////        }
//    }
}
