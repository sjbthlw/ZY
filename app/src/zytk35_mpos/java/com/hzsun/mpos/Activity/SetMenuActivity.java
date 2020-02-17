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

import com.hzsun.mpos.Adapter.SetMenuListviewAdapter;
import com.hzsun.mpos.R;
import com.hzsun.mpos.progressutils.ProgressManage;

import java.util.ArrayList;


/**
 * 选择设置下的菜单
 */
public class SetMenuActivity extends BaseActivity {

    private String TAG = getClass().getSimpleName();
    private ImageView ivCheckMenu;
    private TextView textTitle;
    private ListView lvSetMenu;
    private String twoMenuTitle, checkMenuTitle;
    private SetMenuListviewAdapter setMenuListviewAdapter;

    private int iType;  //类型
    private int iPosition; //选中的选项
    private String strTitle;    //标题
    private ArrayList<String> OptionItemList = new ArrayList<String>(); //选项内容
    private int image;
    private int positionId = 0;

    private ProgressManage progressManage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_menu);
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
        lvSetMenu = ((ListView) findViewById(R.id.lv_set_menu));

        textTitle.setText(strTitle);

        if (image != 0)
            ivCheckMenu.setImageResource(image);
        else
            ivCheckMenu.setImageResource(R.mipmap.s_local_senior);

        setMenuListviewAdapter = new SetMenuListviewAdapter(this, OptionItemList);
        lvSetMenu.setAdapter(setMenuListviewAdapter);
        lvSetMenu.setSelection(iPosition);//选择选中的对象
        setMenuListviewAdapter.setCurrentItem(iPosition);//系统级需要自己选择
        lvSetMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setMenuListviewAdapter.setCurrentItem(position);
                setMenuListviewAdapter.notifyDataSetChanged();
                positionId = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        lvSetMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //确定键
                Log.i(TAG, "===Position:" + position);
                //PutResult(iType,position);
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
            PutResult(iType, positionId);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        int KeyValue;
        KeyValue = event.getKeyCode();
        //Log.i(TAG, "KeyValue:" + KeyValue);
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                PutResult(1000, 0);//后退不处理返回值
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

    //在onResume()方法注册
    @Override
    protected void onResume() {
        Log.i(TAG, "注册");
        super.onResume();
    }

    //onPause()方法注销
    @Override
    protected void onPause() {
        Log.i(TAG, "注销");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "销毁");
        super.onDestroy();
    }
}
