package com.hzsun.mpos.Activity;

import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.hzsun.mpos.Adapter.WifiListviewAdapter;
import com.hzsun.mpos.Public.FileUtils;
import com.hzsun.mpos.Public.SelectPopWindow;
import com.hzsun.mpos.Public.ToastUtils;
import com.hzsun.mpos.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hzsun.mpos.Global.Global.ZYTKPath;
import static com.hzsun.mpos.Global.Global.g_WifiParaInfo;
import static com.hzsun.mpos.MyApplication.gWifiManager;
import static com.hzsun.mpos.Public.Publicfun.RunShellCmd;
import static com.hzsun.mpos.Public.ToastUtils.BOTTOM;
import static com.hzsun.mpos.Public.ToastUtils.CENTER;
import static com.hzsun.mpos.Public.ToastUtils.WARN;
import static com.hzsun.mpos.Public.WifiUtil.isExsits;

public class WifiActivity extends BaseActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    private String TAG = getClass().getSimpleName();
    private ListView wifiListview;

    private WifiListviewAdapter wifiListviewAdapter;
    private List<String> stringList = new ArrayList<>();
    private int positionId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        initViews();
    }


    private void initViews() {
        wifiListview = ((ListView) findViewById(R.id.wifi_listview));
        setTitle("WIFI选项");
        stringList.add(getResources().getString(R.string.wifi_1));
        stringList.add(getResources().getString(R.string.wifi_2));
        stringList.add(getResources().getString(R.string.wifi_3));
        wifiListviewAdapter = new WifiListviewAdapter(this, stringList);
        wifiListview.setAdapter(wifiListviewAdapter);
        wifiListview.setOnItemSelectedListener(this);
        wifiListview.setOnItemClickListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        wifiListviewAdapter.setCurrentItem(position);
        wifiListviewAdapter.notifyDataSetChanged();
        positionId = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                WIFICardSet();
                break;
            case 1:
                WIFIList();
                break;
            case 2:
                DelAndCloseWIFI();
                break;
        }
    }

    //wifi卡设置
    private void WIFICardSet() {
        startActivity(new Intent(WifiActivity.this, WifiCardSetActivity.class));
    }

    //wifi列表
    private void WIFIList() {
        startActivity(new Intent(WifiActivity.this, WifiListActivity.class));
    }

    //删除关闭wifi
    private void DelAndCloseWIFI() {
        //判断是否存在wifi文件和信息
        Map<String, String> InfoMap = new HashMap<>();
        //读取MAP数据
        String fileName = ZYTKPath + "DeviceWifiPara.ini";
        InfoMap = FileUtils.ReadMapFileData(fileName);

        if (InfoMap != null) {
            List<Object> viewList = SelectPopWindow.showSelectPopWindow(this, wifiListview, "删除关闭wifi？");
            final PopupWindow popupWindow = (PopupWindow) viewList.get(0);
            Button bt_confirm = (Button) viewList.get(1);
            bt_confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                    delWifi();
                }
            });
        } else {
            ToastUtils.showText(WifiActivity.this, "无WIFI信息！", WARN, BOTTOM, Toast.LENGTH_LONG);
        }
    }

    private void delWifi() {
        Log.d(TAG, "删除原有的wifi：" + g_WifiParaInfo.strUserName);

        if (gWifiManager.isWifiEnabled()) {
            Log.d(TAG, "WIFI已经打开");
        }
        WifiConfiguration tempConfig = isExsits(g_WifiParaInfo.strUserName, gWifiManager);
        if (tempConfig != null) {
            gWifiManager.removeNetwork(tempConfig.networkId);
            gWifiManager.saveConfiguration();
            //RemoveWifiBySsid(gWifiManager, g_WifiParaInfo.strUserName);
        }
        String strCmd = "rm -r " + ZYTKPath + "DeviceWifiPara.ini";
        Log.i(TAG, strCmd);
        RunShellCmd(strCmd);
        ResetDevice(300);
    }

    //重启设备
    private void ResetDevice(int iPosition) {
        List<Object> viewList = SelectPopWindow.showSelectPopWindow(this, wifiListview, "是否重启设备？");
        final PopupWindow popupWindow = (PopupWindow) viewList.get(0);
        Button bt_confirm = (Button) viewList.get(1);
        bt_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                RunShellCmd("reboot");
            }
        });

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        int KeyValue;
        KeyValue = event.getKeyCode();
        if ((KeyValue == KeyEvent.KEYCODE_ENTER) && (event.getAction() == 0)) {
            if (positionId == 0) {
                WIFICardSet();
            } else if (positionId == 1) {
                WIFIList();
            } else if (positionId == 2) {
                DelAndCloseWIFI();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int KeyValue;
        KeyValue = event.getKeyCode();
        if (KeyValue == KeyEvent.KEYCODE_PERIOD ||
                KeyValue == KeyEvent.KEYCODE_NUMPAD_MULTIPLY ||
                KeyValue == KeyEvent.KEYCODE_NUMPAD_ADD ||
                KeyValue == KeyEvent.KEYCODE_0) {
            return true;
        }
        switch (KeyValue) {
            case KeyEvent.KEYCODE_1:
                WIFICardSet();
                break;
            case KeyEvent.KEYCODE_2:
                WIFIList();
                break;
            case KeyEvent.KEYCODE_3:
                DelAndCloseWIFI();
                break;
        }
        return super.onKeyDown(keyCode, event);
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

            case 2:  //删除关闭wifi
                delWifi();
                break;
            case 300:  //重启设备
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                if (iRet == 1) {
                    Log.i(TAG, "重启设备");
                    ToastUtils.showText(this, "重启设备", WARN, CENTER, Toast.LENGTH_LONG);
                    RunShellCmd("reboot");
                }
                break;
        }
    }
}
