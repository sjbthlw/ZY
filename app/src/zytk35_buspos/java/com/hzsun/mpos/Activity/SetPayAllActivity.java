package com.hzsun.mpos.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hzsun.mpos.Adapter.MenuListviewAdapter;
import com.hzsun.mpos.R;

import java.util.ArrayList;

public class SetPayAllActivity extends
        BaseActivity implements AdapterView.OnItemClickListener {

    private String TAG = getClass().getSimpleName();
    private ListView lv_SetAll;
    private ArrayList<String> stringList;
    private MenuListviewAdapter menuListviewAdapter;

    private String title = "设置";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pay_all);
        initViews();
        initListView();
    }

    private void initViews() {
        lv_SetAll = ((ListView) findViewById(R.id.set_all));
        setTitle(title);
    }

    private void initListView() {
        stringList = new ArrayList<>();
        stringList.add("本机设置");
        stringList.add("高级设置");
        stringList.add("U盘选项");
        stringList.add("WIFI选项");
        stringList.add("人脸参数设置");
        stringList.add("身份语音设置");


        menuListviewAdapter = new MenuListviewAdapter(this, stringList);
        lv_SetAll.setAdapter(menuListviewAdapter);
        lv_SetAll.setOnItemClickListener(this);

    }

    //关于本机参数
    private void GotoAboutThisMac(int position) {
        startActivity(new Intent(SetPayAllActivity.this, AboutPosActivity.class));
    }

    //高级参数设置
    private void GotoAdvanced(int position) {
        startActivity(new Intent(SetPayAllActivity.this, AdvancedActivity.class));
    }

    //U盘功能
    private void UDiskFun(int iPosition) {
        int iType = 24;
        int iSelectItem;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "U盘功能";
        OptionItemList.add("人脸特征码导入");
        OptionItemList.add("应用程序升级");
        OptionItemList.add("引导程序升级");
        OptionItemList.add("POS机数据导出");
        OptionItemList.add("身份语音文件导入");

        iSelectItem = 0;
        startActivity(new Intent(this, SetUdiskActivity.class)
                .putExtra("Type", iType)
                .putExtra("Position", iSelectItem)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList));
    }

    //WIFI选项
    private void WIFIOptions(int position) {
        startActivity(new Intent(SetPayAllActivity.this, WifiActivity.class)
                .putExtra("Image", R.mipmap.s_local_senior)
                .putExtra("TwoMenuTitle", title)
                .putExtra("SetMenuTitle", stringList.get(position))
                .putExtra("ThreeType", position));
    }

    //人脸参数设置
    private void FaceOptions(int position) {
        startActivity(new Intent(SetPayAllActivity.this, FaceInfoActivity.class));
    }

    //身份语音设置
    private void IdSoundOptions(int position) {
        startActivity(new Intent(SetPayAllActivity.this, IdSoundActivity.class));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:  //本机设置
                GotoAboutThisMac(position);
                break;
            case 1:  //高级设置
                GotoAdvanced(position);
                break;
            case 2:  //U盘设置
                UDiskFun(position);
                break;
            case 3:  //WIFI选项
                WIFIOptions(position);
                break;
            case 4:  //人脸参数设置
                FaceOptions(position);
                break;
            case 5:  //身份语音设置
                IdSoundOptions(position);
                break;
        }
    }

}
