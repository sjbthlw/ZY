package com.hzsun.mpos.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.hzsun.mpos.Adapter.MyEditText;
import com.hzsun.mpos.R;

import java.util.ArrayList;

/**
    * @author  zytk wcp
        * @time  2019/12/12
        * @description 输入密码界面类
**/
public class PWDActivity extends BaseActivity
        implements View.OnKeyListener {

    private String TAG = getClass().getSimpleName();
    private TextView tvNotice;
    private MyEditText etInput;

    private int iType;  //类型 1:
    private int iPosition; //选中的选项
    private String strTitle;    //标题
    private ArrayList<String> OptionItemList = new ArrayList<String>(); //选项内容
    private int image;
    private String strInpwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pwd);
        isShowPerScreen(true);
        initData();
        initViews();
        //hideInputManager();
    }

    //Activity返回参数
    private void PutResult(int iType, int iRet) {
        Intent intent = new Intent();
        Log.i(TAG, "iType:" + iType + " result:" + iRet);
        intent.putExtra("result", iRet);
        intent.putExtra("Position", iPosition);
        setResult(iType, intent);
        finish();//此处一定要调用finish()方法
    }

    private void initData() {

        iType = getIntent().getIntExtra("Type", -1);
        iPosition = getIntent().getIntExtra("Position", -1);
        strTitle = getIntent().getStringExtra("strTitle");
        OptionItemList = getIntent().getStringArrayListExtra("OptionItemList");
        image = getIntent().getIntExtra("Image", -1);
    }

    private void initViews() {
        tvNotice = ((TextView) findViewById(R.id.tv_notice));
        etInput = ((MyEditText) findViewById(R.id.et_input));
        etInput.setOnKeyListener(this);
        //etInput.setInputType(InputType.TYPE_NULL);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etInput.getWindowToken(), 0);

        //0.密钥 1.维护密钥 2.设置密钥 3.高级密钥
        tvNotice.setText(strTitle);
        //输入的密码
        strInpwd = OptionItemList.get(0);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        int KeyValue;
        KeyValue = event.getKeyCode();
        if (KeyValue == KeyEvent.KEYCODE_FUNCTION ||
                KeyValue == KeyEvent.KEYCODE_NUMPAD_DOT ||
                KeyValue == KeyEvent.KEYCODE_NUMPAD_ADD) {
            return true;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN) {

            switch (KeyValue) {
                case KeyEvent.KEYCODE_ENTER:
                    String strPwd = etInput.getText().toString();
                    CheckPassword(strPwd);
                    break;
                case KeyEvent.KEYCODE_BACK:
                    PutResult(iType, 2);
                    break;
                case KeyEvent.KEYCODE_NUMPAD_MULTIPLY:
                    etInput.setText("");
                    return true;
            }
        }
        return false;
    }

    //校验密码的是否正确
    private void CheckPassword(String strPwd) {
        if (strInpwd.equals(strPwd)) {   //密码正确
            PutResult(iType, 0);
        } else {  //密码错误
            PutResult(iType, 1);
        }
    }

    /**
     * 隐藏输入软键盘
     *
     * @param context
     * @param view
     */
    public static void hideInputManager(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (view != null && imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);  //强制隐藏
        }
    }

}
