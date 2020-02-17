package com.hzsun.mpos.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hzsun.mpos.R;
import com.hzsun.mpos.data.LocalInfoRW;

import java.util.ArrayList;

import static com.hzsun.mpos.Global.Global.g_LocalInfo;


public class EditTextActivity extends BaseActivity implements View.OnClickListener {

    private String TAG = getClass().getSimpleName();
    private TextView tvTitle;
    private TextView tvNotice;
    private EditText etInput;
    private Button bt_cancle;
    private Button bt_confirm;

    private int iType;  //类型
    private int iPosition; //选中的选项
    private String strTitle;    //标题
    private ArrayList<String> OptionItemList = new ArrayList<String>(); //选项内容
    private int sInputFlag;  //输入标记
    private int iRet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_text);
        initData();
        initViews();
    }

    //Activity返回参数
    private void PutResult(int iType, int iRet) {
        Intent intent = new Intent();
        Log.i(TAG, "iType:" + iType + " result:" + iRet);
        intent.putExtra("result", iRet);
        setResult(iType, intent);

        Log.i(TAG, "返回的结果:" + iRet);
        String strTemp = etInput.getText().toString();
        int iTemp = Integer.parseInt(strTemp);

        if (iType == 15)//设置省电时长
        {
            if (iTemp > 30)
                iTemp = 30;
            if (iTemp < 1)
                iTemp = 1;
            g_LocalInfo.cPowerSaveTimeA = iTemp;
        } else if (iType == 16)//设置同账号时长
        {
            if (iTemp > 3600)
                iTemp = 360;
            g_LocalInfo.iAccSameTime = iTemp;
        } else if (iType == 17)//支付成功显示时长
        {
            if ((iTemp > 10) || (iTemp == 0))
                iTemp = 5;
            g_LocalInfo.iPayShowTime = iTemp;
        }
        LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
        finish();//此处一定要调用finish()方法
    }

    private void initData() {

        iType = getIntent().getIntExtra("Type", -1);
        iPosition = getIntent().getIntExtra("Position", -1);
        strTitle = getIntent().getStringExtra("strTitle");
        OptionItemList = getIntent().getStringArrayListExtra("OptionItemList");
    }

    private void initViews() {
        setTitle(strTitle);

        tvNotice = ((TextView) findViewById(R.id.tv_notice));
        etInput = ((EditText) findViewById(R.id.et_input));
        bt_cancle = (Button) findViewById(R.id.bt_cancle);
        bt_confirm = (Button) findViewById(R.id.bt_confirm);

        bt_cancle.setOnClickListener(this);
        bt_confirm.setOnClickListener(this);

        etInput.setText(iPosition + "");
        tvNotice.setText(OptionItemList.get(0));
        sInputFlag = 0;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_cancle:
                finish();
                break;
            case R.id.bt_confirm:
                PutResult(iType, iRet);
                break;
        }
    }
}
