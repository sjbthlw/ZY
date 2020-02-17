package com.hzsun.mpos.Activity;

import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hzsun.mpos.Adapter.WifiListviewAdapter;
import com.hzsun.mpos.Public.FileUtils;
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
    private ImageView ivCheckMenu;
    private TextView textTitle;
    private TextView tvCheckedNum;
    private ListView wifiListview;
    private String twoMenuTitle;
    private String checkMenuTitle;
    private int image;

    private WifiManager wifiManager;
    private WifiListviewAdapter wifiListviewAdapter;
    private List<String> stringList = new ArrayList<>();
    private int positionId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        isShowPerScreen(true);
        initData();
        initViews();
    }

    private void initData() {
        twoMenuTitle = getIntent().getStringExtra("TwoMenuTitle");
        checkMenuTitle = getIntent().getStringExtra("SetMenuTitle");
        image = getIntent().getIntExtra("Image", -1);
        //threeType = getIntent().getIntExtra("ThreeType", -1);
    }

    private void initViews() {
        ivCheckMenu = ((ImageView) findViewById(R.id.iv_check_menu));
        textTitle = ((TextView) findViewById(R.id.text_title));
        tvCheckedNum = ((TextView) findViewById(R.id.tv_checked_num));
        wifiListview = ((ListView) findViewById(R.id.wifi_listview));
        textTitle.setText(twoMenuTitle + " > " + checkMenuTitle);
        ivCheckMenu.setImageResource(image);
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
        tvCheckedNum.setText(position + 1 + "/" + stringList.size());
        wifiListviewAdapter.setCurrentItem(position);
        wifiListviewAdapter.notifyDataSetChanged();
        positionId = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        if (position == 0) {
//            WIFICardSet(position);
//        } else if (position == 1) {
//            WIFIList(position);
//        }
    }

    //wifi卡设置
    private void WIFICardSet(int position) {
        startActivity(new Intent(WifiActivity.this, WifiCardSetActivity.class)
                .putExtra("proTitle", textTitle.getText().toString())
                .putExtra("Title", stringList.get(position))
                .putExtra("Image", image));
    }

    //wifi列表
    private void WIFIList(int position) {
        startActivity(new Intent(WifiActivity.this, WifiListActivity.class)
                .putExtra("proTitle", textTitle.getText().toString())
                .putExtra("Title", stringList.get(position))
                .putExtra("Image", image));
    }

    //删除关闭wifi
    private void DelAndCloseWIFI(int position) {

        int iType = position;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "删除关闭wifi";

        //判断是否存在wifi文件和信息
        Map<String, String> InfoMap = new HashMap<>();
        //读取MAP数据
        String fileName = ZYTKPath + "DeviceWifiPara.ini";
        InfoMap = FileUtils.ReadMapFileData(fileName);
        if (InfoMap != null) {
            //删除关闭wifi
            OptionItemList.add("删除关闭wifi？");
            startActivityForResult(new Intent(this, ConfirmActivity.class)

                    .putExtra("Type", iType)
                    .putExtra("Position", position)
                    .putExtra("strTitle", strTitle)
                    .putStringArrayListExtra("OptionItemList", OptionItemList)
                    .putExtra("Image", image), iType);
        } else {
            ToastUtils.showText(WifiActivity.this, "无WIFI信息！", WARN, BOTTOM, Toast.LENGTH_LONG);
        }
    }

    //重启设备
    private void ResetDevice(int iPosition) {

        int iType = iPosition;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "重启设备";

        //是否恢复出厂设置？
        OptionItemList.add("是否重启设备？");
        startActivityForResult(new Intent(this, ConfirmActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iPosition)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        int KeyValue;
        KeyValue = event.getKeyCode();
        if ((KeyValue == KeyEvent.KEYCODE_ENTER) && (event.getAction() == 0)) {
            if (positionId == 0) {
                WIFICardSet(positionId);
            } else if (positionId == 1) {
                WIFIList(positionId);
            } else if (positionId == 2) {
                DelAndCloseWIFI(positionId);
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
                WIFICardSet(KeyValue - KeyEvent.KEYCODE_1);
                break;
            case KeyEvent.KEYCODE_2:
                WIFIList(KeyValue - KeyEvent.KEYCODE_1);
                break;
            case KeyEvent.KEYCODE_3:
                DelAndCloseWIFI(KeyValue - KeyEvent.KEYCODE_1);
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
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
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
