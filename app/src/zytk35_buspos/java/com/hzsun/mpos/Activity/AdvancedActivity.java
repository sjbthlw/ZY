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
import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.Public.SelectPopWindow;
import com.hzsun.mpos.Public.ToastUtils;
import com.hzsun.mpos.R;
import com.hzsun.mpos.data.LocalInfoRW;

import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.LENGTH_LONG;
import static com.hzsun.mpos.Global.Global.MAXBOOKSCOUNT;
import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Global.Global.g_SystemInfo;
import static com.hzsun.mpos.Global.Global.g_WasteBookInfo;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;
import static com.hzsun.mpos.Public.ToastUtils.CENTER;
import static com.hzsun.mpos.Public.ToastUtils.WARN;

/**
 * 高级参数（设置）
 */
public class AdvancedActivity extends
        BaseActivity implements AdapterView.OnItemClickListener {

    private String TAG = getClass().getSimpleName();
    private ListView lv_Advanced;
    private ArrayList<String> stringList;
    private MenuListviewAdapter menuListviewAdapter;
    private List<Object> viewList;
    private int positionId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced);
        initViews();
        initListView();
    }

    private void initViews() {
        lv_Advanced = ((ListView) findViewById(R.id.lv_advanced_all));
        setTitle("高级参数");
    }

    private void initListView() {
        stringList = new ArrayList<>();
        stringList.add("恢复出厂设置");
        stringList.add("初始化设备");
        stringList.add("重启设备");
        stringList.add("重新上传记录");
        stringList.add("测试模式");
        stringList.add("进入Android桌面");

        menuListviewAdapter = new MenuListviewAdapter(this, stringList);
        lv_Advanced.setAdapter(menuListviewAdapter);
        lv_Advanced.setOnItemClickListener(this);
    }

    //恢复出厂设置
    private void RestoreFactorySet(int iPosition) {

        viewList = SelectPopWindow.showSelectPopWindow(this, lv_Advanced, "是否恢复出厂设置?");
        final PopupWindow popupWindow = (PopupWindow) viewList.get(0);
        Button bt_confirm = (Button) viewList.get(1);
        bt_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                ToastUtils.showText(AdvancedActivity.this, "恢复出厂设置", WARN, CENTER, LENGTH_LONG);
                Publicfun.FactoryReset();
            }
        });
    }

    //初始化设备
    private void InitDevice(int iPosition) {

        viewList = SelectPopWindow.showSelectPopWindow(this, lv_Advanced, "是否初始化设备?");
        final PopupWindow popupWindow = (PopupWindow) viewList.get(0);
        Button bt_confirm = (Button) viewList.get(1);
        bt_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                ToastUtils.showText(AdvancedActivity.this, "初始化设备", WARN, CENTER, LENGTH_LONG);
                Publicfun.InitDevice();
            }
        });
    }

    //重启设备
    private void ResetDevice(int iPosition) {
        viewList = SelectPopWindow.showSelectPopWindow(this, lv_Advanced, "设备重启");
        final PopupWindow popupWindow = (PopupWindow) viewList.get(0);
        Button bt_confirm = (Button) viewList.get(1);
        bt_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                ToastUtils.showText(AdvancedActivity.this, "重启设备", WARN, CENTER, LENGTH_LONG);
                Publicfun.RunShellCmd("reboot");
            }
        });
    }

    //重新上传记录
    private void ReUpRecord(int iPosition) {
        viewList = SelectPopWindow.showSelectPopWindow(this, lv_Advanced, "是否重新上传记录?");
        final PopupWindow popupWindow = (PopupWindow) viewList.get(0);
        Button bt_confirm = (Button) viewList.get(1);
        bt_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                ToastUtils.showText(AdvancedActivity.this, "重新上传记录", WARN, CENTER, LENGTH_LONG);
                Log.i(TAG, "重置已上传流水记录号:" + g_WasteBookInfo.TransferIndex);
                long TransferIndex = (g_WasteBookInfo.TransferIndex % MAXBOOKSCOUNT);
                g_WasteBookInfo.TransferIndex = g_WasteBookInfo.TransferIndex - TransferIndex;
                Log.i(TAG, "重置已上传流水记录号:" + g_WasteBookInfo.TransferIndex);
            }
        });
    }

    //进入测试模式
    private void EnterTestMode(int iPosition) {

        viewList = SelectPopWindow.showSelectPopWindow(this, lv_Advanced, "是否进入测试模式?");
        final PopupWindow popupWindow = (PopupWindow) viewList.get(0);
        Button bt_confirm = (Button) viewList.get(1);
        bt_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                ToastUtils.showText(AdvancedActivity.this, "测试模式", WARN, CENTER, LENGTH_LONG);
                if (g_WorkInfo.cTestState == 0) {
                    Log.i(TAG, "进入测试模式");
                    ToastUtils.showText(AdvancedActivity.this, "进入测试模式", WARN, CENTER, Toast.LENGTH_LONG);
                    g_WorkInfo.cTestState = 1;
                    g_LocalInfo.cInputMode = 3;
                    g_LocalInfo.cBookSureMode = 0;
                } else {
                    Log.i(TAG, "退出测试模式");
                    ToastUtils.showText(AdvancedActivity.this, "退出测试模式", WARN, CENTER, Toast.LENGTH_LONG);
                    g_WorkInfo.cTestState = 0;
                    g_LocalInfo = LocalInfoRW.ReadLocalInfo();
                }
            }
        });
    }

    //进入android桌面
    private void EnterLAUNCHER3(int iPosition) {
        viewList = SelectPopWindow.showSelectPopWindow(this, lv_Advanced, "是否进入桌面模式?");
        final PopupWindow popupWindow = (PopupWindow) viewList.get(0);
        Button bt_confirm = (Button) viewList.get(1);
        bt_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                ToastUtils.showText(AdvancedActivity.this, "进入android桌面", WARN, CENTER, LENGTH_LONG);
                //am start -n com.android.launcher3/com.android.launcher3.Launcher
                Log.i(TAG, "进入android桌面");
                ToastUtils.showText(AdvancedActivity.this, "进入android桌面", WARN, CENTER, Toast.LENGTH_LONG);
                GotoLauncher3();
            }
        });
    }

    //进入Android桌面
    public void GotoLauncher3() {

        //com.android.launcher3.SettingsActivity
        ComponentName componetName = new ComponentName(
                //这个是另外一个应用程序的包名
                "com.android.launcher3",
                //这个参数是要启动的Activity
                "com.android.launcher3.Launcher");
        //"com.android.settings.bluetooth.BluetoothSettings");
        //"com.android.settings.LanguageSettings");
        //"com.android.settings.UserDictionarySettings");
        //"com.android.settings.WallpaperSuggestionActivity");
        //"com.android.settings.DisplaySettings");
        //"com.android.settings.DeviceAdminSettings");
        //"com.android.settings.SoundSettings");
        //"com.android.settings.wifi.WifiDialogActivity");
        //"android.settings.SetupChooseLockPattern");
        try {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			Bundle bundle = new Bundle();
//			bundle.putCharSequenceArray("val",new String[]{"111","222","333","444"});
//			intent.putExtras(bundle);//绑定bundle数据
            intent.setComponent(componetName);
            startActivity(intent);

            int processId = android.os.Process.myPid();
            android.os.Process.killProcess(processId);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "可以在这里提示用户没有找到应用程序，或者是做其他的操作！", Toast.LENGTH_LONG).show();
            Log.v("go to apk error", "------>" + e.toString());
        }
    }


    //8.人脸活体设置
    private void SetFaceLive(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "人脸活体设置";

        //人脸活体设置   0:打开活体 1:关闭活体
        OptionItemList.add("打开活体");
        OptionItemList.add("关闭活体");

        iSelectItem = g_LocalInfo.cFaceLiveFlag;//人脸活体设置   0:打开活体 1:关闭活体

        startActivityForResult(new Intent(this, SetMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iSelectItem)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
    }

    //9.人脸识别启用设置
    private void SetFaceEnable(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "人脸识别启用设置";

        //人脸启用设置
        OptionItemList.add("关闭");
        OptionItemList.add("启用");

        iSelectItem = g_SystemInfo.cFaceDetectFlag;//是否支持人脸识别

        startActivityForResult(new Intent(this, SetMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iSelectItem)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
    }

    //设置人脸参数值
    private void SetFaceInfo(int iPosition) {
        int iType = iPosition;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "设置人脸参数值";

        //Float.toString(g_LocalInfo.fLiveThrehold*100)
        OptionItemList.add(String.format("%d", g_LocalInfo.iPupilDistance));// 瞳距

        OptionItemList.add(Float.toString(g_LocalInfo.fLiveThrehold * 100).split("\\.")[0]);//人脸活体检测率

        OptionItemList.add(Float.toString(g_LocalInfo.fFraction * 100).split("\\.")[0]);//人脸相识率

        startActivityForResult(new Intent(this, SetFaceInfoActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", 0)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
        return;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:  //恢复出厂设置
                RestoreFactorySet(position);
                break;
            case 1:  //初始化设备
                InitDevice(position);
                break;
            case 2:  //重启设备
                ResetDevice(position);
                break;
            case 3:  //重新上传记录
                ReUpRecord(position);
                break;
            case 4:  //测试模式
                EnterTestMode(position);
                break;
            case 5:  //进入Android桌面
                EnterLAUNCHER3(position);
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

//            case 7:  //人脸活体设置
//                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
//                Log.i(TAG,"返回的结果:"+iRet);
//                g_LocalInfo.cFaceLiveFlag=(short) iRet;
//                LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
//                break;
//
//            case 8:  //是否支持人脸识别
//                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
//                Log.i(TAG,"返回的结果:"+iRet);
//                g_SystemInfo.cFaceDetectFlag= (byte) iRet;
//                SystemInfoRW.WriteAllSystemInfo(g_SystemInfo);
//                break;
//
//            case 9:  //设置人脸参数
//                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
//                ArrayList<String> strList = new ArrayList<String>(); //选项内容
//                strList=data.getStringArrayListExtra("strList");
//
//                String strtemp =strList.get(0);
//                try {
//                    g_LocalInfo.iPupilDistance = Integer.parseInt(strtemp);// 瞳距
//                    g_FaceIdentInfo.iPupilDistance=g_LocalInfo.iPupilDistance;
//                } catch (NumberFormatException e) {
//                    e.printStackTrace();
//                }
//                String strtemp1 =String.format("0.%s",strList.get(1));
//                g_LocalInfo.fLiveThrehold = Float.parseFloat(strtemp1);// 人脸活体检测率
//                g_FaceIdentInfo.fLiveThrehold=g_LocalInfo.fLiveThrehold;
//
//                String strtemp2 =String.format("0.%s",strList.get(2));
//                g_LocalInfo.fFraction = Float.parseFloat(strtemp2);// 人脸相识率
//                g_FaceIdentInfo.fFraction=g_LocalInfo.fFraction;
//
//                LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
//                break;
        }
    }

}
