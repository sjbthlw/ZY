package com.hzsun.mpos.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hzsun.mpos.Adapter.MyEditText;
import com.hzsun.mpos.Public.ToastUtils;
import com.hzsun.mpos.R;

import java.util.ArrayList;

import static com.hzsun.mpos.Public.ToastUtils.BOTTOM;
import static com.hzsun.mpos.Public.ToastUtils.WARN;

public class EditTextActivity extends BaseActivity {

    private String TAG = getClass().getSimpleName();
    private ImageView ivSetImage;
    private TextView tvTitle;
    private TextView tvNotice;
    private MyEditText etInput;

    private int iType;  //类型
    private int iPosition; //选中的选项
    private String strTitle;    //标题
    private ArrayList<String> OptionItemList = new ArrayList<String>(); //选项内容
    private int image;
    private int sInputFlag;  //输入标记

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_text);
        isShowPerScreen(true);
        initData();
        initViews();
        initListener();
    }

    //Activity返回参数
    private void PutResult(int iType, int iRet) {
        Intent intent = new Intent();
        Log.i(TAG, "iType:" + iType + " result:" + iRet);
        intent.putExtra("result", iRet);
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
        ivSetImage = ((ImageView) findViewById(R.id.local_business_set_image));
        tvTitle = ((TextView) findViewById(R.id.title_tv));
        tvNotice = ((TextView) findViewById(R.id.tv_notice));
        etInput = ((MyEditText) findViewById(R.id.et_input));

        ivSetImage.setImageResource(image);
        tvTitle.setText(strTitle);
        etInput.setText(iPosition + "");
        tvNotice.setText(OptionItemList.get(0));
        sInputFlag = 0;
    }

    private void initListener() {
        etInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int KeyValue;
                KeyValue = event.getKeyCode();
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
//                    if (sInputFlag == 0) {
//                        sInputFlag = 1;
//                        etInput.setText("");
//                    }
                    String etStr = etInput.getText().toString();
                    if (TextUtils.isEmpty(etStr) && KeyValue == KeyEvent.KEYCODE_NUMPAD_MULTIPLY) {
                        return true;
                    }
                    if (KeyValue == KeyEvent.KEYCODE_NUMPAD_ADD || KeyValue == KeyEvent.KEYCODE_FUNCTION) {
                        return true;
                    }

                    switch (KeyValue) {
                        case KeyEvent.KEYCODE_ENTER:
                            Log.i(TAG, "按了确定键");
                            if (etStr.equals("")) {
                                ToastUtils.showText(EditTextActivity.this, "非法值，请重输！", WARN, BOTTOM, Toast.LENGTH_LONG);
                                break;
                            }
                            if (iType == 13) {
                                int iRet = Integer.parseInt(etStr);
                                if ((iRet < 1) || (iRet > 10))
                                    ToastUtils.showText(EditTextActivity.this, "超出范围，请重输！", WARN, BOTTOM, Toast.LENGTH_LONG);
                                else
                                    PutResult(iType, Integer.parseInt(etStr));
                            } else
                                PutResult(iType, Integer.parseInt(etStr));
                            break;

                        case KeyEvent.KEYCODE_BACK:
                            Log.i(TAG, "按了后退键");
                            PutResult(100, 0);
                            break;

                        case KeyEvent.KEYCODE_NUMPAD_MULTIPLY://清除键
                            etInput.setText("");
                            return true;
                    }
                }
                return false;
            }
        });
    }

}
