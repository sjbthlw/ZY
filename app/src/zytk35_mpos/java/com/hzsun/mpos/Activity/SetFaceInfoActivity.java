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

public class SetFaceInfoActivity extends BaseActivity {

    private String TAG = getClass().getSimpleName();
    private ImageView ivCheckMenu;
    private TextView textTitle;

    private EditText et1;
    private RelativeLayout rl1;

    private EditText et2;
    private RelativeLayout rl2;

    private EditText et3;
    private RelativeLayout rl3;

    private int iType;  //类型
    private int iPosition; //选中的选项
    private String strTitle;    //标题
    private ArrayList<String> OptionItemList = new ArrayList<String>(); //选项内容
    private ArrayList<String> strList = new ArrayList<String>(); //选项内容
    private int[] sInputFlag = new int[6];  //类型
    private int image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_face_info);
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

        et1 = (EditText) findViewById(R.id.et_1);
        rl1 = (RelativeLayout) findViewById(R.id.rl_1);
        et2 = (EditText) findViewById(R.id.et_2);
        rl2 = (RelativeLayout) findViewById(R.id.rl_2);
        et3 = (EditText) findViewById(R.id.et_3);
        rl3 = (RelativeLayout) findViewById(R.id.rl_3);

        textTitle.setText(strTitle);
        ivCheckMenu.setImageResource(image);

        et1.setText(OptionItemList.get(0));
        et2.setText(OptionItemList.get(1));
        et3.setText(OptionItemList.get(2));
    }

    //Activity返回参数
    private void PutResult(int iType, int iRet) {
        Intent intent = new Intent();
        intent.putExtra("result", iRet);
        intent.putStringArrayListExtra("strList", strList);
        setResult(iType, intent);
        finish();
    }

    private void initListener() {

        et1.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int KeyValue;
                KeyValue = event.getKeyCode();

                if ((KeyValue >= KeyEvent.KEYCODE_0) && (KeyValue <= KeyEvent.KEYCODE_9)) {
                    if (sInputFlag[0] == 0) {
                        sInputFlag[0] = 1;
                        et1.setText("");
                    }
                }

                if (KeyValue == KeyEvent.KEYCODE_ENTER) {
                    judgeBookmoneyPara();
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_NUMPAD_MULTIPLY) {
                    et1.setText("");
                    return true;
                }
                return false;
            }
        });

        et2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int KeyValue;
                KeyValue = event.getKeyCode();
                if ((KeyValue >= KeyEvent.KEYCODE_0) && (KeyValue <= KeyEvent.KEYCODE_9)) {
                    if (sInputFlag[1] == 0) {
                        sInputFlag[1] = 1;
                        et2.setText("");
                    }
                }

                if (KeyValue == KeyEvent.KEYCODE_ENTER) {
                    judgeBookmoneyPara();
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_NUMPAD_MULTIPLY) {
                    et2.setText("");
                    return true;
                }
                return false;
            }
        });

        et3.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int KeyValue;
                KeyValue = event.getKeyCode();
                if ((KeyValue >= KeyEvent.KEYCODE_0) && (KeyValue <= KeyEvent.KEYCODE_9)) {
                    if (sInputFlag[2] == 0) {
                        sInputFlag[2] = 1;
                        et3.setText("");
                    }
                }

                if (KeyValue == KeyEvent.KEYCODE_ENTER) {
                    judgeBookmoneyPara();
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_NUMPAD_MULTIPLY) {
                    et3.setText("");
                    return true;
                }
                return false;
            }
        });
    }

    //判断网络参数
    private void judgeBookmoneyPara() {

        strList.add(et1.getText().toString());
        strList.add(et2.getText().toString());
        strList.add(et3.getText().toString());
        PutResult(iType, 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        int KeyValue;
        KeyValue = event.getKeyCode();
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                PutResult(100, 0);//后退不处理返回值
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

}