package com.hzsun.mpos.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.hzsun.mpos.Adapter.HorizontalListViewAdapter;
import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.R;

import java.util.ArrayList;

import static com.hzsun.mpos.Global.Global.CANNOT_CANCEL_PAYMENT;
import static com.hzsun.mpos.Global.Global.g_StationInfo;
import static com.hzsun.mpos.Global.Global.g_WasteBookInfo;
import static com.hzsun.mpos.Global.Global.g_WasteQrBookInfo;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;

/**
 * 小屏主菜单界面{查询/设置/冲正/高级}
 */
public class MenuActivity extends BaseActivity
        implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    private String TAG = getClass().getSimpleName();
    private GridView horizontalListView;
    private HorizontalListViewAdapter horizontalListViewAdapter;
    private int positionId = 0;
    protected MainPreActivity mainPreActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        initViews();
        isShowPerScreen(true);
    }

    private void initViews() {

        horizontalListView = ((GridView) findViewById(R.id.horizontal_listview));
        horizontalListViewAdapter = new HorizontalListViewAdapter(this);
        horizontalListView.setAdapter(horizontalListViewAdapter);
        horizontalListView.setOnItemSelectedListener(this);
        horizontalListView.setOnItemClickListener(this);
        horizontalListView.setSelection(0);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_FUNCTION)
                || (keyCode == KeyEvent.KEYCODE_ENTER)) {
            Log.d(TAG, "onKeyUp-KeyValue:" + keyCode);
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        int KeyValue;
        KeyValue = event.getKeyCode();
        //Log.d(TAG, "dispatchKeyEvent KeyValue:" + KeyValue);
        Log.d("按键", event.getAction() + "");

        if ((KeyValue == KeyEvent.KEYCODE_ENTER) && (event.getAction() == 0)) {
            switch (positionId) {
                case 0:  //查询
                    GotoQueryPayAllMenu();
                    break;
                case 1:  //设置
                    GotoSetPayAllMenu();
                    break;
                case 2:  //冲正
                    GotoRecordDispelMenu();
                    break;
                case 3:  //高级（输入密码界面）
                    GotoSetSysAllMenu();
                    break;
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int KeyValue;
        KeyValue = event.getKeyCode();
        //Log.d(TAG, "KeyValue:" + KeyValue);
        if (KeyValue == KeyEvent.KEYCODE_FUNCTION ||
                KeyValue == KeyEvent.KEYCODE_PERIOD ||
                KeyValue == KeyEvent.KEYCODE_NUMPAD_MULTIPLY ||
                KeyValue == KeyEvent.KEYCODE_NUMPAD_ADD ||
                KeyValue == KeyEvent.KEYCODE_0 ||
                ((KeyValue >= KeyEvent.KEYCODE_5) && (KeyValue <= KeyEvent.KEYCODE_9))) {
            return true;
        }

        switch (KeyValue) {
            case KeyEvent.KEYCODE_DPAD_UP:
                //上键
                if (positionId > 0) {
                    positionId = positionId - 1;
                }
                horizontalListView.setSelection(positionId);
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                //下键
                if (positionId < 3) {
                    positionId = positionId + 1;
                }
                horizontalListView.setSelection(positionId);
                break;

            case KeyEvent.KEYCODE_1:  //查询
                horizontalListView.setSelection(0);
                GotoQueryPayAllMenu();
                break;
            case KeyEvent.KEYCODE_2:  //设置
                horizontalListView.setSelection(1);
                GotoSetPayAllMenu();
                break;
            case KeyEvent.KEYCODE_3:  //冲正
                horizontalListView.setSelection(2);
                GotoRecordDispelMenu();
                break;
            case KeyEvent.KEYCODE_4:  //高级
                horizontalListView.setSelection(3);
                GotoSetSysAllMenu();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        positionId = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        switch (position) {
//            case 0:  //查询
//                GotoQueryPayAllMenu();
//                break;
//            case 1:  //设置
//                GotoSetPayAllMenu();
//                break;
//            case 2:  //冲正
//                GotoRecordDispelMenu();
//                break;
//            case 3:  //高级（输入密码界面）
//                GotoSetSysAllMenu();
//                break;
//        }
    }

    //进入查询界面
    private void GotoQueryPayAllMenu() {
        Log.i(TAG, "进入查询界面");
        startActivity(new Intent(MenuActivity.this, QueryPayAllActivity.class));
    }

    //进入交易统计设置界面
    private void GotoSetPayAllMenu() {
        Log.i(TAG, "进入交易统计设置界面");
        startActivity(new Intent(MenuActivity.this, SetPayAllActivity.class));
    }

    //进入冲正界面F3
    private void GotoRecordDispelMenu() {
        int iRet;
        //判断是否允许冲正
        iRet = CheckRecordDispel();
        if (iRet != 0) {
            return;
        }
    }

    //进入系统设置界面F4
    private void GotoSetSysAllMenu() {
        int i;
        int iType = 3;
        int image = R.mipmap.s_local_senior;
        ArrayList<String> OptionItemList = new ArrayList<String>();
        String strTitle = "请输入高级密钥";
        String strTemp = "" + g_StationInfo.lngAdvancePsw;
        if(strTemp.length()!=6)//默认密码为333333
            strTemp="333333";
        Log.i(TAG, "密钥：" + strTemp);
        OptionItemList.add(strTemp);

        startActivityForResult(new Intent(this, PWDActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", 0)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
    }

    //判断是否允许冲正
    private int CheckRecordDispel() {
        int iRet = 0;
        //只有消费机和现金充值机可以销帐
        if ((g_StationInfo.iStationClass != 301) && (g_StationInfo.iStationClass != 302)) {
            Publicfun.ShowErrorStrDialog(getApplicationContext(), "不允许冲正!");
            return CANNOT_CANCEL_PAYMENT;
        }
        //判断是否允许销帐
        if ((g_StationInfo.cCanQuashPayment != 1) || (g_WorkInfo.cBusinessState != 0)) {
            Publicfun.ShowErrorStrDialog(getApplicationContext(), "不允许冲正!");
            return CANNOT_CANCEL_PAYMENT;
        }
        //跨餐次不允许冲正
        //判断是否有流水(交易流水最大流水号)
        if ((g_WasteBookInfo.MaxStationSID == 0) && (g_WasteQrBookInfo.MaxStationSID == 0)) {
            Publicfun.ShowErrorStrDialog(getApplicationContext(), "无冲正记录流水!");
            return CANNOT_CANCEL_PAYMENT;
        }
        //工作状态
        if ((g_WorkInfo.cRunState != 1) && (g_WorkInfo.cRunState != 2)) {
            Publicfun.ShowErrorStrDialog(getApplicationContext(), "不允许冲正!");
            return CANNOT_CANCEL_PAYMENT;
        }

        int iType = 4;
        int image = R.mipmap.s_local_senior;
        ArrayList<String> OptionItemList = new ArrayList<String>();
        String strTitle = "请输入操作密钥";
        String strTemp = "" + g_StationInfo.lngOptionPsw;
        OptionItemList.add(strTemp);

        startActivityForResult(new Intent(this, PWDActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", 0)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);

        return 0;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        int iRet;

        switch (resultCode) {

            case 4:  //冲正商户密码
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                if (iRet == 0) {
                    //进入冲正界面
                    Log.i(TAG, "进入冲正界面");
                    startActivity(new Intent(this, RecordDispelActivity.class));
                    finish();
                } else if (iRet == 1) {
                    ArrayList<String> OptionItemList = new ArrayList<String>();
                    String strTitle = "提示";
                    OptionItemList.add("输入密码错误！");
                    startActivity(new Intent(this, ConfirmActivity.class)

                            .putExtra("Type", 100)//类型100 为提示
                            .putExtra("Position", 0)
                            .putExtra("strTitle", strTitle)
                            .putStringArrayListExtra("OptionItemList", OptionItemList)
                            .putExtra("Image", 0));
                }
                break;

            case 3:  //高级密码
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                if (iRet == 0) {
                    startActivity(new Intent(this, SeniorSysAllActivity.class));
                } else if (iRet == 1) {
                    ArrayList<String> OptionItemList = new ArrayList<String>();

                    String strTitle = "提示";
                    OptionItemList.add("输入密码错误！");
                    startActivity(new Intent(this, ConfirmActivity.class)

                            .putExtra("Type", 100)//类型100 为提示
                            .putExtra("Position", 0)
                            .putExtra("strTitle", strTitle)
                            .putStringArrayListExtra("OptionItemList", OptionItemList)
                            .putExtra("Image", 0));
                }
                break;
        }
    }

    //在onResume()方法注册
    @Override
    protected void onResume() {

        Log.i(TAG, "MenuActivity 注册");
        g_WorkInfo.cCardEnableFlag = 0;
        super.onResume();
    }

    //onPause()方法注销
    @Override
    protected void onPause() {

        Log.i(TAG, "MenuActivity 注销");
        super.onPause();
    }
}
