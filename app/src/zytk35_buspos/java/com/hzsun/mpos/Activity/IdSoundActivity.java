package com.hzsun.mpos.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


import com.hzsun.mpos.Adapter.MenuListviewAdapter;
import com.hzsun.mpos.R;
import com.hzsun.mpos.data.IdentitySoundRW;
import com.hzsun.mpos.data.LocalInfoRW;

import java.util.ArrayList;

import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Global.Global.g_MapIDSound;


public class IdSoundActivity extends
        BaseActivity implements AdapterView.OnItemClickListener {

    private String TAG = getClass().getSimpleName();
    private ListView lv_IdSound;
    private ArrayList<String> stringList;
    private MenuListviewAdapter menuListviewAdapter;

    private String title = "身份语音设置";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id_sound);
        initViews();
        initListView();
    }

    private void initViews() {
        lv_IdSound = ((ListView) findViewById(R.id.IdSound));
        setTitle(title);
    }

    private void initListView() {
        stringList = new ArrayList<>();
        stringList.add("身份语音开关设置");
        stringList.add("身份语音文件设置");

        menuListviewAdapter = new MenuListviewAdapter(this, stringList);
        lv_IdSound.setAdapter(menuListviewAdapter);
        lv_IdSound.setOnItemClickListener(this);

    }

    //身份语音开关设置
    private void SetIDSound(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "设置身份语音开关";

        // 0:关闭  1:打开
        OptionItemList.add("关闭");
        OptionItemList.add("打开");

        iSelectItem = g_LocalInfo.cIDSoundFlag;// 0:关闭  1:打开

        startActivityForResult(new Intent(this, SetMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iSelectItem)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList), iType);
    }


    //身份语音文件设置
    private void IdSoundOptions(int position) {
        startActivity(new Intent(this, IdentitySoundEditActivity.class));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:  //身份语音开关设置
                SetIDSound(position);
                break;

            case 1:  //身份语音文件设置
                IdSoundOptions(position);
                break;
        }
    }

    /**
     * 为了得到传回的数据，必须在前面的Activity中（指MainActivity类）重写onActivityResult方法
     * <p>
     * requestCode 请求码，即调用startActivityForResult()传递过去的值
     * resultCode 结果码，结果码用于标识返回数据来自哪个新Activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        int iRet;
        switch (resultCode) {
            case 0:  //身份语音开关设置
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                if(iRet!=0){
                    Log.i(TAG, "读取身份语音信息");
                    g_MapIDSound = IdentitySoundRW.ReadIdentitySoundFile();
                }
                g_LocalInfo.cIDSoundFlag = (byte) iRet;
                LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
                break;

        }
    }
}