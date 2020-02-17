package com.hzsun.mpos.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hzsun.mpos.Public.LongClickUtils;
import com.hzsun.mpos.R;
import com.hzsun.mpos.data.BasicInfoRW;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.hzsun.mpos.Global.Global.g_BasicInfo;
import static com.hzsun.mpos.Global.Global.g_Nlib;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;

public class ScanCardActivity extends AppCompatActivity {


    private static final String TAG = "ScanCardActivity";
    private ImageView ivSetImage;
    private TextView textTitle;
    private TextView tvConfirm;
    private LinearLayout ll_setcard;

    private int iType;  //类型
    private int iPosition; //选中的选项
    private String strTitle;    //标题
    private ArrayList<String> OptionItemList = new ArrayList<String>(); //选项内容
    private int image;

    private volatile boolean isScanCard = false;
    private ScanCardThread ThreadScanCard;
    private int sAgentID, sGuestID;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    int iRet = (int) msg.obj;
                    if (iRet == 0) {
                        ThreadScanCard.stopScan();
                        String strTemp = String.format("代理号:%d -- 客户号:%d", sAgentID, sGuestID);
                        Log.i(TAG, strTemp);
                        tvConfirm.setTextColor(getResources().getColor(R.color.colorGreen));
                        tvConfirm.setText(strTemp);
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                PutResult(iType, 1);
                            }
                        }, 2000);

                    } else if (iRet == 1) {
                        tvConfirm.setTextColor(Color.GRAY);
                        tvConfirm.setText("请刷客户设密卡!");
                    } else if (iRet == 2) {
                        tvConfirm.setTextColor(Color.RED);
                        tvConfirm.setText("错误，请刷客户设置卡!");
                    }
                    break;

                case 2:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scancard);
        initData();
        initViews();
        initListener();
        ThreadScanCard = new ScanCardThread();
        ThreadScanCard.start();
    }

    //Activity返回参数
    private void PutResult(int iType, int iRet) {
        Intent intent = new Intent();
        Log.i(TAG, "iType:" + iType + " result:" + iRet);
        intent.putExtra("result", iRet);
        if ((iType == 1) && (iRet == 1)) {
            //写入设密卡数据
            g_BasicInfo.cSystemState = 90;
            g_BasicInfo.cAgentID = (short) sAgentID;
            g_BasicInfo.iGuestID = sGuestID;
            BasicInfoRW.WriteAllBasicInfo(g_BasicInfo);
            //计算密钥(32扇区)
            Log.i(TAG, "计算密钥");
            g_Nlib.UCardDecryptA(g_WorkInfo.bUCardAuthKeyA, g_BasicInfo.iGuestID, g_BasicInfo.cAgentID);
            g_Nlib.UCardDecryptB(g_WorkInfo.bUCardAuthKeyB, g_BasicInfo.iGuestID, g_BasicInfo.cAgentID);
            //设置卡片密钥
            g_Nlib.SetCardKey(g_WorkInfo.bUCardAuthKeyA, g_WorkInfo.bUCardAuthKeyB);

            intent.putExtra("sAgentID", sAgentID);
            intent.putExtra("sGuestID", sGuestID);
        }
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
        textTitle = ((TextView) findViewById(R.id.title_tv));
        tvConfirm = ((TextView) findViewById(R.id.tv_confirm));
        ll_setcard = ((LinearLayout) findViewById(R.id.ll_setcard));

        textTitle.setText(strTitle);
        tvConfirm.setText(OptionItemList.get(0));


        if (iType == 100)
            new DismissTimer(2000, 1000).start();
    }

    //卡片扫描检测线程
    private class ScanCardThread extends Thread {

        public ScanCardThread() {
        }

        public void stopScan() {
            isScanCard = false;
            try {
                this.join();
            } catch (InterruptedException e) {
            }
        }

        @Override
        public void run() {
            isScanCard = true;

            while (isScanCard) {
                try {
                    TimeUnit.MILLISECONDS.sleep(300);
                } catch (Exception e) {
                }

                if (iType == 1)
                    ScanUserSecretCard();
//                else (iType==2)
//                    ScanWifiCard();
            }
        }
    }

    //客户设密码
    private int ScanUserSecretCard() {
        int cResult;

        byte[] bCardUID = new byte[8];
        byte[] bSCardSecret = new byte[8];
        byte[] bCardContext = new byte[32];

        cResult = g_Nlib.ReaderCardUID(bCardUID);
        if (cResult == 0) {
            g_Nlib.SCardSecret(bCardUID, bSCardSecret);
            //下载0扇区密钥
            Log.i(TAG, "读1块数据 客户号和代理号");
            cResult = g_Nlib.ReadPSCard(bSCardSecret, bCardUID, 1, bCardContext);
            if (cResult == 0) {
                //代理号
                sAgentID = (bCardContext[0] & 0xff);
                //客户号
                sGuestID = (bCardContext[1] & 0xff) + ((bCardContext[2] & 0xff) * 256);
                //检查类型
                if ((bCardContext[10] & 0xff) != 0xF0) {
                    Log.i(TAG, "检查类型失败");
                    //return;
                }
                isScanCard = false;
            } else {
                cResult = 2;
            }
        } else {
            cResult = 1;
        }
        Message message = Message.obtain();
        message.obj = cResult;
        message.what = 1;
        handler.sendMessage(message);
        return 0;
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

    private void GotoFnMenu() {
        startActivity(new Intent(this, MenuActivity.class));
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        int KeyValue;
        KeyValue = event.getKeyCode();
        //Log.d(TAG,"KeyValue:"+KeyValue);
        if (event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER) {
        }
        switch (event.getKeyCode()) {

            case KeyEvent.KEYCODE_FUNCTION:
                Log.i(TAG, "按了确定键");
                GotoFnMenu();//进入功能界面FN
                break;

            case KeyEvent.KEYCODE_NUMPAD_ENTER:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    //记录用户首次点击返回键的时间
    private long firstTime = 0;

    public void onBackPressed() {
        //不执行回退功能
//        long secondTime = System.currentTimeMillis();
//        if (secondTime - firstTime > 2000) {
//            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
//            firstTime = secondTime;
//        } else {
//            super.onBackPressed();
//        }
    }

    @Override
    protected void onDestroy() {
        if (ThreadScanCard != null) {
            ThreadScanCard.stopScan();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {

        Log.i(TAG, "注册");
        super.onResume();
    }

    @Override
    protected void onPause() {

        Log.i(TAG, "注销");
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
    }

    private void initListener() {
        LongClickUtils.setLongClick(new Handler(), ll_setcard, 5 * 1000, new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                GotoFnMenu();
                return true;
            }
        });
    }
}
