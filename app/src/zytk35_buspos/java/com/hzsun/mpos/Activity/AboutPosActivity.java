package com.hzsun.mpos.Activity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.hzsun.mpos.Adapter.MenuListviewAdapter;
import com.hzsun.mpos.Public.FileUtils;
import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.Public.SelectPopWindow;
import com.hzsun.mpos.Public.ToastUtils;
import com.hzsun.mpos.R;
import com.hzsun.mpos.data.LocalInfoRW;
import com.hzsun.mpos.data.RecordInfoRW;
import com.hzsun.mpos.thread.LogWriteThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.widget.Toast.LENGTH_LONG;
import static com.hzsun.mpos.Global.Global.LogPath;
import static com.hzsun.mpos.Global.Global.PicPath;
import static com.hzsun.mpos.Global.Global.SOFTWAREVER;
import static com.hzsun.mpos.Global.Global.ZYTK35Path;
import static com.hzsun.mpos.Global.Global.ZYTKPath;
import static com.hzsun.mpos.Global.Global.g_BasicInfo;
import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Global.Global.g_LocalNetStrInfo;
import static com.hzsun.mpos.Global.Global.g_RecordInfo;
import static com.hzsun.mpos.Global.Global.g_StationInfo;
import static com.hzsun.mpos.Global.Global.g_SystemInfo;
import static com.hzsun.mpos.Public.Publicfun.ReadLocalNetStrCfg;
import static com.hzsun.mpos.Public.Publicfun.ReadSofeVerInfoFile;
import static com.hzsun.mpos.Public.Publicfun.RunShellCmd;
import static com.hzsun.mpos.Public.ToastUtils.CENTER;
import static com.hzsun.mpos.Public.ToastUtils.WARN;

/**
 * 关于本机（设置）
 */
public class AboutPosActivity extends
        BaseActivity implements AdapterView.OnItemClickListener {

    private String TAG = getClass().getSimpleName();
    private ListView lv_AboutPos;
    private ArrayList<String> stringList;
    private MenuListviewAdapter aboutPosListviewAdapter;
    private List<Object> viewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_pos);
        initViews();
        initListView();
    }

    private void initViews() {
        lv_AboutPos = ((ListView) findViewById(R.id.lv_aboutpos));
        setTitle("本机设置");
    }


    private void initListView() {

        stringList = new ArrayList<>();
        stringList.add("网络参数设置");
        stringList.add("设置IP获取方式");
        stringList.add("本机参数");
        stringList.add("系统参数");
        stringList.add("音量设置");
        stringList.add("设置日志管理");
        stringList.add("删除日志图片记录");
        stringList.add("重新下载应用app");
        stringList.add("日餐累清零");
        stringList.add("设置播放金额语音");
        stringList.add("设置账户姓名显示");
        stringList.add("设置是否启用代扣");
        stringList.add("设置继电器");
        stringList.add("设置切换主题");
        stringList.add("设置是否启用人脸唤醒");
        stringList.add("设置省电时长");
        stringList.add("设置同账号时长");
        stringList.add("设置支付成功显示时长");
        stringList.add("设置对接机");

        aboutPosListviewAdapter = new MenuListviewAdapter(this, stringList);
        lv_AboutPos.setAdapter(aboutPosListviewAdapter);
        lv_AboutPos.setOnItemClickListener(this);
    }

//    //设置网络参数
//    private void SetNetParams(int position) {
//        int iType=100;
//        int image = 0;
//        ArrayList<String> OptionItemList = new ArrayList<String>();
//        Map<String, String> DevTypeMap =  new HashMap<>();
//        String ServiceIp="192.168.1.42";
//        String ServicePort="9050";
//        String LocalIp="192.168.1.144";
//        String EthNetmask="255.255.255.0";
//        String  EthGateway="192.168.1.253";
//        String  Dns="8.8.4.4";
//
//        //读取MAP数据
//        String fileName=ZYTKPath+"DeviceNetPara.ini";
//        DevTypeMap= FileUtils.ReadMapFileData(fileName);
//        if(DevTypeMap!=null)
//        {
//            ServiceIp = DevTypeMap.get("ServeripAddr1");
//            ServicePort = DevTypeMap.get("ServerPort1");
//            LocalIp = DevTypeMap.get("ipAddr");
//            EthNetmask = DevTypeMap.get("netMask");
//            EthGateway = DevTypeMap.get("gateway");
//            Dns = DevTypeMap.get("dns1");
//        }
//        String strTitle="设置网络参数";
//        OptionItemList.add(ServiceIp);
//        OptionItemList.add(ServicePort);
//        OptionItemList.add(LocalIp);
//        OptionItemList.add(EthNetmask);
//        OptionItemList.add(EthGateway);
//        OptionItemList.add(Dns);
//
//        ComponentName componetName = new ComponentName(
//                //这个是另外一个应用程序的包名
//                "com.hzsun.iap",
//                //这个参数是要启动的Activity
//                "com.hzsun.iap.SetNetParamsActivity");
//        try {
//            Intent intent = new Intent();
//            intent.setAction("android.intent.action.VIEW");
//            intent.addCategory("android.intent.category.DEFAULT");
//            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.putExtra("Type", iType);
//            intent.putExtra("Position", 0);
//            intent.putExtra("strTitle", strTitle);
//            intent.putStringArrayListExtra("OptionItemList", OptionItemList);
//            intent.putExtra("Image", image);
//            intent.setComponent(componetName);
//            startActivityForResult(intent,iType);
//        } catch (Exception e) {
//            Toast.makeText(getApplicationContext(), "用户没有找到应用程序！", Toast.LENGTH_LONG).show();
//            Log.v("go to apk error","------>"+e.toString());
//        }
//    }

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
                "com.hzsun.iap.Activity.SetNetParamsActivity");
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
                "com.hzsun.iap.Activity.SetMenuActivity");
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
            Toast.makeText(getApplicationContext(), "用户没有找到应用程序！", Toast.LENGTH_LONG).show();
            Log.e("go to apk error", "------>" + e.toString());
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


        //内核参数
        //Publicfun.ReadKernelVersion(cVerTemp);
        startActivityForResult(new Intent(this, CheckMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iPosition)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);

        return;
    }

    //设置音量大小
    private void SetVolumeLevel(int iPosition) {
        startActivity(new Intent(AboutPosActivity.this, VolumeSetActivity.class));
    }

    //设置日志管理
    private void SetLogManagement(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
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
                .putStringArrayListExtra("OptionItemList", OptionItemList), iType);
    }

    //删除日志图片记录
    private void DelLogRecord(int iPosition) {
        viewList = SelectPopWindow.showSelectPopWindow(this, lv_AboutPos, "删除日志图片记录");
        final PopupWindow popupWindow = (PopupWindow) viewList.get(0);
        Button bt_confirm = (Button) viewList.get(1);
        bt_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                ToastUtils.showText(AboutPosActivity.this, "删除日志图片记录", WARN, CENTER, LENGTH_LONG);
                RunShellCmd("rm -f " + LogPath + "*");
                RunShellCmd("rm -rf " + PicPath + "*");
            }
        });
    }

    //重新下载应用app
    private void ReDownAPPFile(int iPosition) {
        viewList = SelectPopWindow.showSelectPopWindow(this, lv_AboutPos, "重新下载应用app");
        final PopupWindow popupWindow = (PopupWindow) viewList.get(0);
        Button bt_confirm = (Button) viewList.get(1);
        bt_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                ToastUtils.showText(AboutPosActivity.this, "重新下载应用app", WARN, CENTER, LENGTH_LONG);
                RunShellCmd("rm -rf " + ZYTK35Path + "VersionInfo.ini");
                ResetDevice();
            }
        });
    }

    //日餐累清零
    private void ClearDayAmountTotal(int iPosition) {
        //是否允许查汇总
        if (g_StationInfo.cCanPermitStat == 1) {
            viewList = SelectPopWindow.showSelectPopWindow(this, lv_AboutPos, "日餐累计清零");
            final PopupWindow popupWindow = (PopupWindow) viewList.get(0);
            Button bt_confirm = (Button) viewList.get(1);
            bt_confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                    ToastUtils.showText(AboutPosActivity.this, "日餐累计清零", WARN, CENTER, LENGTH_LONG);
                    Log.i(TAG, "日餐累计清零");
                    //记录当日交易笔数、金额
                    g_RecordInfo.wTodayPaymentSum = 0;
                    g_RecordInfo.lngTodayPaymentMoney = 0;
                    //记录当餐交易笔数、金额
                    g_RecordInfo.wTotalBusinessSum = 0;
                    g_RecordInfo.lngTotalBusinessMoney = 0;
                    //记录交易完成后日累，餐累，末笔营业，累计总金额参数
                    RecordInfoRW.WriteAllRecordInfo(g_RecordInfo);
                }
            });
        } else {
            ToastUtils.showText(AboutPosActivity.this, "不允许操作此类", WARN, CENTER, LENGTH_LONG);
        }
    }

    //设置播放具体金额语音
    private void SetVoiceMoneyMode(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "设置播放金额语音";

        //是否播放具体金额 0: 不启用 1:启用
        OptionItemList.add("普通模式");
        OptionItemList.add("数字金额模式");

        iSelectItem = g_LocalInfo.cPlayVoiceMoneyFlag;//设置小票打印  0:不打印 1:直接打印

        startActivityForResult(new Intent(this, SetMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iSelectItem)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList), iType);
    }

    //显示姓名设置
    private void CardNameSet(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "设置账户显示姓名";

        // 0:* 号模式  1:正常模式
        OptionItemList.add("* 号模式");
        OptionItemList.add("正常模式");

        iSelectItem = g_LocalInfo.cAccNameShowMode;// 0:* 号模式  1:正常模式

        startActivityForResult(new Intent(this, SetMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iSelectItem)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList), iType);

    }

    //设置是否代扣
    private void WithholdSet(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "设置是否启用代扣";

        // 0:不启用  1:启用
        OptionItemList.add("不启用");
        OptionItemList.add("启用");

        iSelectItem = g_LocalInfo.iWithholdState;// 0:不启用  1:启用

        startActivityForResult(new Intent(this, SetMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iSelectItem)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList), iType);
    }

    //设置是否启用继电器
    private void SetRelayStatus(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "设置继电器";

        startActivityForResult(new Intent(this, RelayActivity.class)
                        .putExtra("Type", iType)
                        .putExtra("Position", 0)
                        .putExtra("strTitle", strTitle),
                iType);

    }

    /**
     * 切换主题
     *
     * @param iPosition
     */
    private void changeStyle(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
        ArrayList<String> OptionItemList = new ArrayList<String>();
        String strTitle = "切换主题";
        OptionItemList.add("样式一(读卡外挂)");
        OptionItemList.add("样式二(屏下刷卡)");
        iSelectItem = g_LocalInfo.iStyle; // 0-外置刷卡样式,1-屏下刷卡样式
        startActivityForResult(new Intent(this, SetMenuActivity.class)
                .putExtra("Type", iType)
                .putExtra("Position", iSelectItem)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList), iType);
    }

    //设置是否启用人脸唤醒
    private void SetFaceWakeup(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
        ArrayList<String> OptionItemList = new ArrayList<String>();
        String strTitle = "设置是否启用人脸唤醒";
        OptionItemList.add("不启用");
        OptionItemList.add("启用");
        iSelectItem = g_LocalInfo.iFaceWakeupFlag; // 0-不启用,1-启用
        startActivityForResult(new Intent(this, SetMenuActivity.class)
                .putExtra("Type", iType)
                .putExtra("Position", iSelectItem)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList), iType);
    }

    //设置省电时长
    private void SetPowerSaveTime(int iType) {
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "设置省电时长";
        int iPosition = g_LocalInfo.cPowerSaveTimeA;
        OptionItemList.add("省电时长(1-30)分钟");

        startActivityForResult(new Intent(this, EditTextActivity.class)
                .putExtra("Type", iType)
                .putExtra("Position", iPosition)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
    }

    //设置同账号时长
    private void SetSameAccTime(int iType) {
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "设置同账号时长";
        int iPosition = g_LocalInfo.iAccSameTime;
        OptionItemList.add("同账号时长(0-3600)秒");

        startActivityForResult(new Intent(this, EditTextActivity.class)
                .putExtra("Type", iType)
                .putExtra("Position", iPosition)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
    }

    //设置支付成功显示时长
    private void SetPayShowTime(int iPosition) {
        int iType = iPosition;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "支付成功显示时长";
        iPosition = g_LocalInfo.iPayShowTime;
        OptionItemList.add("显示时长(1-10)秒");

        startActivityForResult(new Intent(this, EditTextActivity.class)
                .putExtra("Type", iType)
                .putExtra("Position", iPosition)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
    }


    //设置对接机
    private void setEnableDockpos(int iPosition) {
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


    //重启设备
    private void ResetDevice() {
        viewList = SelectPopWindow.showSelectPopWindow(this, lv_AboutPos, "设备重启");
        final PopupWindow popupWindow = (PopupWindow) viewList.get(0);
        Button bt_confirm = (Button) viewList.get(1);
        bt_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                ToastUtils.showText(AboutPosActivity.this, "重启设备", WARN, CENTER, LENGTH_LONG);
                Publicfun.RunShellCmd("reboot");
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0: //设置网络参数
                SetNetParams(position);
                break;
            case 1: //设置本地IP获取方式
                SetGetLocalIPMode(position);
                break;
            case 2: //本机参数
                QueryLocalPare(position);
                break;
            case 3: //系统参数
                QuerySystemPare(position);
                break;
            case 4: //音量设置
                SetVolumeLevel(position);
                break;
            case 5: //设置日志管理
                SetLogManagement(position);
                break;
            case 6: //删除日志图片记录
                DelLogRecord(position);
                break;
            case 7: //重新下载应用app
                ReDownAPPFile(position);
                break;
            case 8:  //日累清零
                ClearDayAmountTotal(position);
                break;
            case 9:  //设置播放金额语音
                SetVoiceMoneyMode(position);
                break;
            case 10:  //显示姓名设置
                CardNameSet(position);
                break;
            case 11:  //设置是否代扣
                WithholdSet(position);
                break;
            case 12:    //设置是否启用继电器
                SetRelayStatus(position);
                break;
            case 13:
                changeStyle(position);
                break;
            case 14:    //设置是否启用人脸唤醒
                SetFaceWakeup(position);
                break;
            case 15:    //设置省电时长
                SetPowerSaveTime(position);
                break;
            case 16:    //设置同账号时长
                SetSameAccTime(position);
                break;
            case 17:    //设置支付成功显示时长
                SetPayShowTime(position);
                break;
            case 18:   //设置对接机
                setEnableDockpos(position);
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
            case 5:  //设置日志管理
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                g_LocalInfo.cLogFlag = (short) iRet;
                LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
                if (g_LocalInfo.cLogFlag == 1) {
                    LogWriteThread ThreadLogWrite = new LogWriteThread("");
                    ThreadLogWrite.start();
                }
                break;

            case 9:  //设置播放金额语音
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                g_LocalInfo.cPlayVoiceMoneyFlag = (short) iRet;
                LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
                break;

            case 10:  //账户显示姓名模式
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                g_LocalInfo.cAccNameShowMode = (short) iRet;
                LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
                break;

            case 11:  //是否启用代扣
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                g_LocalInfo.iWithholdState = (short) iRet;
                LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
                break;

            case 12:  //设置是否启用继电器
                break;
            case 13: //设置UI主题切换
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                g_LocalInfo.iStyle = (short) iRet;
                LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
                break;
            case 14:  //设置是否启用人脸唤醒
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                g_LocalInfo.iFaceWakeupFlag = (short) iRet;
                LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
                break;
            case 18:  //设置对接机
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                g_LocalInfo.cDockposFlag = (short) iRet;
                LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
                ResetDevice();
                break;
            case 100: //网络参数
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                //读本机网络参数配置文件
                Log.i(TAG, "读本机网络配置文件");
                g_LocalNetStrInfo = ReadLocalNetStrCfg();
                if (iRet == 1)
                    ResetDevice();
                break;

            case 200: //设置本地IP获取方式
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                Log.i(TAG, "读本机网络配置文件");
                g_LocalNetStrInfo = ReadLocalNetStrCfg();
                ResetDevice();
                break;
        }
    }


}
