package com.hzsun.mpos.Activity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hzsun.mpos.Adapter.MenuListviewAdapter;
import com.hzsun.mpos.Public.FileUtils;
import com.hzsun.mpos.Public.ToastUtils;
import com.hzsun.mpos.R;
import com.hzsun.mpos.data.LocalInfoRW;
import com.hzsun.mpos.thread.LogWriteThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.hzsun.mpos.Global.Global.DATAPath;
import static com.hzsun.mpos.Global.Global.LogPath;
import static com.hzsun.mpos.Global.Global.PicPath;
import static com.hzsun.mpos.Global.Global.SOFTWAREVER;
import static com.hzsun.mpos.Global.Global.ZYTK35Path;
import static com.hzsun.mpos.Global.Global.ZYTKPath;
import static com.hzsun.mpos.Global.Global.g_BasicInfo;
import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Global.Global.g_LocalNetStrInfo;
import static com.hzsun.mpos.Global.Global.g_StationInfo;
import static com.hzsun.mpos.Global.Global.g_SystemInfo;
import static com.hzsun.mpos.Public.Publicfun.ReadLocalNetStrCfg;
import static com.hzsun.mpos.Public.Publicfun.ReadSofeVerInfoFile;
import static com.hzsun.mpos.Public.Publicfun.RunShellCmd;
import static com.hzsun.mpos.Public.Publicfun.getProp;
import static com.hzsun.mpos.Public.ToastUtils.CENTER;
import static com.hzsun.mpos.Public.ToastUtils.WARN;

/**
 * 关于本机（设置）
 */
public class AboutPosActivity extends BaseActivity implements
        AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    private String TAG = getClass().getSimpleName();
    private ImageView ivCheckMenu;
    private TextView textTitle;
    private ListView aboutPosListview;
    private TextView tvCheckedNum;
    private int image = R.mipmap.s_local_check;
    private ArrayList<String> stringList;
    private MenuListviewAdapter menuListviewAdapter;
    private int positionId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_pos);
        isShowPerScreen(true);
        initViews();
    }

    private void initViews() {
        ivCheckMenu = ((ImageView) findViewById(R.id.iv_check_menu));
        textTitle = ((TextView) findViewById(R.id.text_title));
        aboutPosListview = ((ListView) findViewById(R.id.about_pos_listview));
        tvCheckedNum = ((TextView) findViewById(R.id.tv_checked_num));
        textTitle.setText("关于本机");
        ivCheckMenu.setImageResource(image);

        stringList = new ArrayList<>();
        stringList.add("网络参数");
        stringList.add("本机参数");
        stringList.add("系统参数");
        stringList.add("设置IP获取方式");
        stringList.add("设置日志管理");
        stringList.add("删除日志图片记录");
        stringList.add("重新下载应用app");
        stringList.add("设置设备类型");
        //stringList.add("设置对接机");
        menuListviewAdapter = new MenuListviewAdapter(this, stringList);
        aboutPosListview.setAdapter(menuListviewAdapter);
        aboutPosListview.setOnItemSelectedListener(this);
        aboutPosListview.setOnItemClickListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        tvCheckedNum.setText(position + 1 + "/" + stringList.size());
        menuListviewAdapter.setCurrentItem(position);
        menuListviewAdapter.notifyDataSetChanged();
        positionId = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    //设置网络参数
    private void SetNetParams(int position) {
        int iType = 100;
        int image = 0;
        ArrayList<String> OptionItemList = new ArrayList<String>();
        Map<String, String> DevTypeMap = new HashMap<>();
        String ServiceIp = "192.168.1.42";
        String ServicePort = "9050";
        String LocalIp = "192.168.1.144";
        String EthNetmask = "255.255.255.0";
        String EthGateway = "192.168.1.253";
        String Dns = "8.8.4.4";

        //读取MAP数据
        String fileName = ZYTKPath + "DeviceNetPara.ini";
        DevTypeMap = FileUtils.ReadMapFileData(fileName);
        if (DevTypeMap != null) {
            ServiceIp = DevTypeMap.get("ServeripAddr1");
            ServicePort = DevTypeMap.get("ServerPort1");
            LocalIp = DevTypeMap.get("ipAddr");
            EthNetmask = DevTypeMap.get("netMask");
            EthGateway = DevTypeMap.get("gateway");
            Dns = DevTypeMap.get("dns1");
        }
        String strTitle = "设置网络参数";
        OptionItemList.add(ServiceIp);
        OptionItemList.add(ServicePort);
        OptionItemList.add(LocalIp);
        OptionItemList.add(EthNetmask);
        OptionItemList.add(EthGateway);
        OptionItemList.add(Dns);

        ComponentName componetName = new ComponentName(
                //这个是另外一个应用程序的包名
                "com.hzsun.iap",
                //这个参数是要启动的Activity
                "com.hzsun.iap.SetNetParamsActivity");
        try {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.addCategory("android.intent.category.DEFAULT");
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("Type", iType);
            intent.putExtra("Position", 0);
            intent.putExtra("strTitle", strTitle);
            intent.putStringArrayListExtra("OptionItemList", OptionItemList);
            intent.putExtra("Image", image);
            intent.setComponent(componetName);
            startActivityForResult(intent, iType);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "可以在这里提示用户没有找到应用程序，或者是做其他的操作！", Toast.LENGTH_LONG).show();
            Log.v("go to apk error", "------>" + e.toString());
        }
    }

    //本机参数
    private void QueryLocalPare(int iPosition) {
        int cResult;
        int iType = iPosition;
        int image = R.mipmap.s_local_check;
        ArrayList<String> OptionItemList = new ArrayList<String>();
        String strTemp = "";
        String strTitle = "本机参数";

        //设备类型
        if (g_StationInfo.iStationClass == 301) {
            OptionItemList.add("设备类型：现金消费机");
        } else if (g_StationInfo.iStationClass == 302) {
            OptionItemList.add("设备类型：现金充值机");
        }

        //站点号
        strTemp = String.format("站点号：%d", g_StationInfo.iStationID);
        OptionItemList.add(strTemp);

        strTemp = String.format("商户号：%d", g_StationInfo.iShopUserID);
        OptionItemList.add(strTemp);

        //最大脱机天数
        strTemp = String.format("最大脱机天数：%d", g_StationInfo.cCanOffCount);
        OptionItemList.add(strTemp);

        startActivityForResult(new Intent(this, CheckMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iPosition)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);

        return;
    }

    //系统参数
    private void QuerySystemPare(int iPosition) {
        int cResult;
        int iType = iPosition;
        int image = R.mipmap.s_local_check;
        ArrayList<String> OptionItemList = new ArrayList<String>();
        String strTemp = "";
        String strTitle = "系统参数";

        //固件版本号
        OptionItemList.add("固件版本号：" + SOFTWAREVER.substring(0, 11));
        //终端软件版本号(APP)

        String strLocalAppVer = ReadSofeVerInfoFile().substring(0, 11);
        OptionItemList.add("软件版本号：" + strLocalAppVer);

        //终端序列号
        strTemp = String.format("终端序列号：%02x.%02x.%02x.%02x.%02x.%02x.",
                g_BasicInfo.cTerminalSerID[0], g_BasicInfo.cTerminalSerID[1], g_BasicInfo.cTerminalSerID[2],
                g_BasicInfo.cTerminalSerID[3], g_BasicInfo.cTerminalSerID[4], g_BasicInfo.cTerminalSerID[5]);
        OptionItemList.add(strTemp);

        //终端内码
        strTemp = String.format("终端内码：%02x.%02x.%02x.%02x.", g_BasicInfo.cTerInCode[0], g_BasicInfo.cTerInCode[1], g_BasicInfo.cTerInCode[2], g_BasicInfo.cTerInCode[3]);
        OptionItemList.add(strTemp);

        //平台客户号
        strTemp = String.format("代理客户号：%d-%d", g_SystemInfo.cAgentID, g_SystemInfo.iGuestID);
        OptionItemList.add(strTemp);

        //平台客户号
        strTemp = String.format("本机代理客户号：%d-%d", g_BasicInfo.cAgentID, g_BasicInfo.iGuestID);
        OptionItemList.add(strTemp);

        //android属性参数
        //[ro.product.zytkdevice]: [rk3399_yt223_v1]
        //[ro.product.zytkname]: [rk3399_zytk]
        String value  = getProp("ro.product.zytkdevice");
        OptionItemList.add("zytkdevice："+value);
        String value1  = getProp("ro.product.zytkname");
        OptionItemList.add("zytkname："+value1);

        startActivityForResult(new Intent(this, CheckMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iPosition)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);

        return;
    }

//    //设置音量大小
//    private void SetVolumeLevel(int iPosition) {
//        int cResult;
//        int iType=iPosition;
//        int image = R.mipmap.s_local_set;
//        ArrayList<String> OptionItemList = new ArrayList<String>();
//        String strTemp="";
//        String strTitle="音量设置";
//
//        OptionItemList.add(""+g_LocalInfo.cVolumeLevel);
//
//        startActivityForResult(new Intent(AboutPosActivity.this, ProgressbarActivity.class)
//                .putExtra("Type", iType)
//                .putExtra("Position", iPosition)
//                .putExtra("strTitle", strTitle)
//                .putStringArrayListExtra("OptionItemList", OptionItemList)
//                .putExtra("Image", image),iType);
//    }

    //设置本地IP获取方式
    private void SetGetLocalIPMode(int iPosition) {
        int iType = 200;
        int image = 0;
        int iSelectItem = 0;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "IP获取方式";
        OptionItemList.add("static");
        OptionItemList.add("dhcp");

        //获取本机IP方式 0 DHCP 1 STATIC
        if (g_LocalNetStrInfo.IPMode == 1)   //1 STATIC
            iSelectItem = 0;
        else
            iSelectItem = 1;

        ComponentName componetName = new ComponentName(
                //这个是另外一个应用程序的包名
                "com.hzsun.iap",
                //这个参数是要启动的Activity
                "com.hzsun.iap.SetMenuActivity");
        try {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.addCategory("android.intent.category.DEFAULT");
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("Type", iType);
            intent.putExtra("Position", iSelectItem);
            intent.putExtra("strTitle", strTitle);
            intent.putStringArrayListExtra("OptionItemList", OptionItemList);
            intent.putExtra("Image", image);

//			Bundle bundle = new Bundle();
//			bundle.putCharSequenceArray("val",new String[]{"111","222","333","444"});
//			intent.putExtras(bundle);//绑定bundle数据
            intent.setComponent(componetName);
            startActivityForResult(intent, iType);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "可以在这里提示用户没有找到应用程序，或者是做其他的操作！", Toast.LENGTH_LONG).show();
            Log.e("go to apk error", "------>" + e.toString());
        }
    }

    //设置客户设密卡
    private void SetUserSecretCard(int iPosition) {
        int cResult;
        int iType = 1;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();
        String strTemp = "";
        String strTitle = "设置客户设密卡";

        //请刷客户设密卡
        OptionItemList.add("请刷客户设密卡!");

        startActivityForResult(new Intent(this, ScanCardActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iPosition)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);

        return;
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

    //设置日志管理
    private void SetLogManagement(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "设置日志管理";

        // 0:关闭  1:打开
        OptionItemList.add("关闭日志");
        OptionItemList.add("打开日志");

        iSelectItem = g_LocalInfo.cLogFlag;// 0:关闭  1:打开

        startActivityForResult(new Intent(this, SetMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iSelectItem)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
    }

    //删除日志图片记录
    private void DelLogRecord(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "删除日志图片记录";

        //是否清零当日累计？
        OptionItemList.add("是否删除日志图片记录？");
        startActivityForResult(new Intent(this, ConfirmActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iPosition)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
    }

    //重新下载应用app
    private void ReDownAPPFile(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "下载应用app";

        //是否重新下载应用？
        OptionItemList.add("是否重新下载应用app？");
        startActivityForResult(new Intent(this, ConfirmActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iPosition)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);

    }

    //设置设备类型
    private void SetDeviceType(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "设置设备类型";

        // 0:新设备机型0x24   1:老设备机型 0x1C
        OptionItemList.add("新设备机型");
        OptionItemList.add("老设备机型");

        iSelectItem = g_LocalInfo.iDeviceType;

        startActivityForResult(new Intent(this, SetMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iSelectItem)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
        return;
    }

    //设置对接机
    private void SetEnableDockpos(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "是否启用对接机";

        // 0:不启用  1:启用
        OptionItemList.add("不启用");
        OptionItemList.add("启用");

        iSelectItem = g_LocalInfo.cDockposFlag;// 0:不启用  1:启用

        startActivityForResult(new Intent(this, SetMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iSelectItem)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
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

            case 4:  //设置日志管理
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                g_LocalInfo.cLogFlag = (short) iRet;
                LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
                if (g_LocalInfo.cLogFlag == 1) {
                    LogWriteThread ThreadLogWrite = new LogWriteThread("");
                    ThreadLogWrite.start();
                }
                break;

            case 5:  //删除日志图片记录
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                RunShellCmd("rm -f " + LogPath + "*");
                RunShellCmd("rm -rf " + PicPath + "*");
                break;

            case 6:  //重新下载应用app
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                RunShellCmd("rm -rf " + ZYTK35Path + "VersionInfo.ini");
                ResetDevice(300);
                break;

            case 7:  //设置设备类型
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                if (g_LocalInfo.iDeviceType != iRet) {
                    Log.i(TAG, "设备类型变更");
                    g_LocalInfo.iDeviceType = iRet;
                    LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
                    //清除相关参数
                    RunShellCmd("rm -rf " + DATAPath + "SytemInfo.dat");
                    RunShellCmd("rm -rf " + DATAPath + "PurseInfo.dat");
                    RunShellCmd("rm -rf " + DATAPath + "StationInfo.dat");
                    RunShellCmd("rm -rf " + DATAPath + "IdentityWallet.dat");
                    RunShellCmd("rm -rf " + DATAPath + "IdentityDiscount.dat");
                    RunShellCmd("rm -rf " + DATAPath + "TimeGroup.dat");
                    ResetDevice(300);
                }
                break;
            case 8: //设置对接机
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                g_LocalInfo.cDockposFlag = (short) iRet;
                LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
                ResetDevice(300);
                break;
            case 100: //网络参数
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                //读本机网络参数配置文件
                Log.i(TAG, "读本机网络配置文件");
                g_LocalNetStrInfo = ReadLocalNetStrCfg();
                if (iRet == 1)
                    ResetDevice(300);
                break;

            case 200: //设置本地IP获取方式
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                Log.i(TAG, "读本机网络配置文件");
                g_LocalNetStrInfo = ReadLocalNetStrCfg();
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        int KeyValue;
        KeyValue = event.getKeyCode();
        //Log.d(TAG, "dispatchKeyEvent KeyValue:" + KeyValue);

        if ((KeyValue == KeyEvent.KEYCODE_ENTER)) {
            switch (positionId) {
                case 0: //网络参数
                    if(event.getAction() == KeyEvent.ACTION_UP)
                        SetNetParams(positionId);
                    break;
                case 1: //本机参数
                    if(event.getAction() == KeyEvent.ACTION_DOWN)
                        QueryLocalPare(positionId);
                    break;
                case 2: //系统参数
                    if(event.getAction() == KeyEvent.ACTION_DOWN)
                        QuerySystemPare(positionId);
                    break;
                case 3: //设置本地IP获取方式
                    if(event.getAction() == KeyEvent.ACTION_UP)
                        SetGetLocalIPMode(positionId);
                    break;
                case 4: //设置日志管理
                    if(event.getAction() == KeyEvent.ACTION_DOWN)
                        SetLogManagement(positionId);
                    break;
                case 5: //删除日志图片记录
                    if(event.getAction() == KeyEvent.ACTION_DOWN)
                        DelLogRecord(positionId);
                    break;
                case 6: //重新下载应用app
                    if(event.getAction() == KeyEvent.ACTION_DOWN)
                         ReDownAPPFile(positionId);
                    break;
                case 7: //设置设备类型
                    if(event.getAction() == KeyEvent.ACTION_DOWN)
                        SetDeviceType(positionId);
                    break;
//				case 8: //设置对接机
//                    if(event.getAction() == KeyEvent.ACTION_DOWN)
//                        SetEnableDockpos(positionId);
//                    break;

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
                KeyValue == KeyEvent.KEYCODE_NUMPAD_ADD) {
            return true;
        }

        switch (KeyValue) {
            case KeyEvent.KEYCODE_1:  //网络参数
                if (event.getAction() == KeyEvent.ACTION_UP)
                    SetNetParams(KeyValue - KeyEvent.KEYCODE_1);
                break;
            case KeyEvent.KEYCODE_2:  //本机参数
                QueryLocalPare(KeyValue - KeyEvent.KEYCODE_1);
                break;
            case KeyEvent.KEYCODE_3:  //系统参数
                QuerySystemPare(KeyValue - KeyEvent.KEYCODE_1);
                break;
            case KeyEvent.KEYCODE_4:  //设置本地IP获取方式
                SetGetLocalIPMode(KeyValue - KeyEvent.KEYCODE_1);
                break;
            case KeyEvent.KEYCODE_5:  //设置日志管理
                SetLogManagement(KeyValue - KeyEvent.KEYCODE_1);
                break;
            case KeyEvent.KEYCODE_6:  //删除日志图片日志
                DelLogRecord(KeyValue - KeyEvent.KEYCODE_1);
                break;
            case KeyEvent.KEYCODE_7:  //重新下载应用app
                ReDownAPPFile(KeyValue - KeyEvent.KEYCODE_1);
                break;
            case KeyEvent.KEYCODE_8: //设置设备类型
                SetDeviceType(KeyValue - KeyEvent.KEYCODE_1);
                break;
//            case KeyEvent.KEYCODE_9: //设置对接机
//                SetEnableDockpos(KeyValue - KeyEvent.KEYCODE_1);
//                break;
        }
        return super.onKeyDown(keyCode, event);
    }

}
