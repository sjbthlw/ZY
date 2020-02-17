package com.hzsun.mpos.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hzsun.mpos.Adapter.CheckMenuAdapter;
import com.hzsun.mpos.R;

import java.util.ArrayList;

/**
 * 查询界面
 */
public class CheckMenuActivity extends BaseActivity {

    private static final String TAG = "CheckMenuActivity";
    private ImageView ivCheckMenu;
    private TextView textTitle;
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
        isShowPerScreen(true);
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
        ivCheckMenu = ((ImageView) findViewById(R.id.iv_check_menu));
        textTitle = ((TextView) findViewById(R.id.text_title));
        listCheckMenu = ((ListView) findViewById(R.id.list_check_menu));

        textTitle.setText(strTitle);

        if (image != 0)
            ivCheckMenu.setImageResource(image);
        else
            ivCheckMenu.setImageResource(R.mipmap.s_local_check);

        checkMenuAdapter = new CheckMenuAdapter(this, OptionItemList);
        listCheckMenu.setAdapter(checkMenuAdapter);

        listCheckMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                //确定键
//                Log.i(TAG,"===Position:"+position);
//                PutResult(iType,position);
            }
        });
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        int KeyValue;
        KeyValue = event.getKeyCode();
        //Log.d(TAG, "dispatchKeyEvent KeyValue:" + KeyValue);
        //Log.d("按键",event.getAction()+"");

        if ((KeyValue == KeyEvent.KEYCODE_ENTER) && (event.getAction() == 0)) {
            PutResult(iType, 0);
        }
        return super.dispatchKeyEvent(event);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Log.i(TAG, "keyCode:" + keyCode);
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
