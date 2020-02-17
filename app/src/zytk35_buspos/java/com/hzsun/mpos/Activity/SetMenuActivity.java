package com.hzsun.mpos.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.hzsun.mpos.Adapter.SetMenuListviewAdapter;
import com.hzsun.mpos.R;

import java.util.ArrayList;


/**
 * 选择设置下的菜单
 */
public class SetMenuActivity extends BaseActivity
        implements AdapterView.OnItemClickListener {

    private String TAG = getClass().getSimpleName();
    private ListView lvSetMenu;
    private ImageView ImageBack;
    private SetMenuListviewAdapter setMenuListviewAdapter;

    private int iType;  //类型
    private int iPosition; //选中的选项
    private String strTitle;    //标题
    private ArrayList<String> OptionItemList = new ArrayList<String>(); //选项内容
    private int positionId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_menu);
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
    }

    private void initViews() {

        positionId = iPosition;
        lvSetMenu = ((ListView) findViewById(R.id.lv_set_menu));
        ImageBack = ((ImageView) findViewById(R.id.iv_back));
        setTitle(strTitle);

        setMenuListviewAdapter = new SetMenuListviewAdapter(this, OptionItemList);
        lvSetMenu.setAdapter(setMenuListviewAdapter);
        lvSetMenu.setSelection(iPosition);//选择选中的对象
        setMenuListviewAdapter.setCurrentItem(iPosition);//系统级需要自己选择

        ImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PutResult(iType, positionId);
            }
        });
    }

    private void initListener() {
        lvSetMenu.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        setMenuListviewAdapter.setCurrentItem(position);
        setMenuListviewAdapter.notifyDataSetChanged();
        positionId = position;

    }


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
