package com.hzsun.mpos.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

import com.hzsun.mpos.R;

import java.util.ArrayList;

public class ConfirmActivity extends BaseActivity {

    private static final String TAG = "ConfirmActivity";
    private ImageView ivSetImage;
    private TextView textTitle;
    private TextView tvConfirm;

    private int iType;  //类型
    private int iPosition; //选中的选项
    private String strTitle;    //标题
    private ArrayList<String> OptionItemList = new ArrayList<String>(); //选项内容
    private int image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        initData();
        initViews();
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
        textTitle = ((TextView) findViewById(R.id.title_tv));
        tvConfirm = ((TextView) findViewById(R.id.tv_confirm));

        textTitle.setText(strTitle);
        tvConfirm.setText(OptionItemList.get(0));

        if (image != 0)
            ivSetImage.setImageResource(image);
        else
            ivSetImage.setImageResource(R.mipmap.s_local_check);

        if (iType == 100)
            new DismissTimer(2000, 1000).start();
    }

    private class DismissTimer extends CountDownTimer {
        private DismissTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            finish();
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Log.i(TAG, "keyCode:" + keyCode);

        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_ENTER:
                Log.i(TAG, "按了确定键");
                PutResult(iType, 1);
                break;

            case KeyEvent.KEYCODE_BACK:
                Log.i(TAG, "按了取消键");
                PutResult(1000, 0);
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
