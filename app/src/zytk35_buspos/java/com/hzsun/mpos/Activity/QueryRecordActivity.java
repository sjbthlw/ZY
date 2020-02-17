package com.hzsun.mpos.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hzsun.mpos.Adapter.ListBusinessCountAdapter;
import com.hzsun.mpos.R;

import java.util.ArrayList;

/**
 * 交易流水查询页
 */
public class QueryRecordActivity extends BaseActivity {

    private String TAG = getClass().getSimpleName();
    private TextView tvCheckedNum;
    private TextView textTitle;
    private LinearLayout linearTop;
    private ListView listBusinessCount;

    private int iType;  //类型
    private int iPosition; //选中的选项
    private String strTitle;    //标题
    private ArrayList<String> OptionItemList = new ArrayList<String>(); //选项内容
    private int image;

    private ListBusinessCountAdapter listBusinessCountAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_info);
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

    }

    private void initViews() {
        tvCheckedNum = ((TextView) findViewById(R.id.tv_checked_num));
        textTitle = ((TextView) findViewById(R.id.text_title));
        linearTop = ((LinearLayout) findViewById(R.id.linear_top));
        listBusinessCount = ((ListView) findViewById(R.id.list_business_count));

        setTitle(strTitle);
        tvCheckedNum.setText(1 + "/" + OptionItemList.size());

        listBusinessCountAdapter = new ListBusinessCountAdapter(this, OptionItemList);
        listBusinessCount.setAdapter(listBusinessCountAdapter);
    }

}
