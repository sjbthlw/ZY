package com.hzsun.mpos.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hzsun.mpos.Adapter.CheckMenuAdapter;
import com.hzsun.mpos.R;

import java.util.ArrayList;

/**
 * 查询界面
 */
public class CheckMenuActivity extends BaseActivity {

    private static final String TAG = "CheckMenuActivity";
    private ListView listCheckMenu;
    private CheckMenuAdapter checkMenuAdapter;
    private int iType;  //类型
    private int iPosition; //选中的选项
    private String strTitle;    //标题
    private ArrayList<String> OptionItemList = new ArrayList<String>(); //选项内容
    private int image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_menu);
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
        listCheckMenu = ((ListView) findViewById(R.id.list_check_menu));
        checkMenuAdapter = new CheckMenuAdapter(this, OptionItemList);
        listCheckMenu.setAdapter(checkMenuAdapter);
        setTitle(strTitle);

        listCheckMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //确定键
                Log.i(TAG, "===Position:" + position);
                PutResult(iType, position);
            }
        });
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
