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
import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.Public.ToastUtils;
import com.hzsun.mpos.R;
import com.hzsun.mpos.data.LocalInfoRW;
import com.hzsun.mpos.data.SystemInfoRW;

import java.util.ArrayList;

import static com.hzsun.mpos.Global.Global.MAXBOOKSCOUNT;
import static com.hzsun.mpos.Global.Global.g_FaceIdentInfo;
import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Global.Global.g_SystemInfo;
import static com.hzsun.mpos.Global.Global.g_WasteBookInfo;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;
import static com.hzsun.mpos.Public.ToastUtils.CENTER;
import static com.hzsun.mpos.Public.ToastUtils.WARN;

/**
 * 高级参数（设置）
 */
public class SeniorParamsActivity extends BaseActivity implements
        AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    private String TAG = getClass().getSimpleName();
    private ImageView ivCheckMenu;
    private TextView textTitle;
    private TextView tvCheckedNum;
    private ListView ListviewInfo;
    private int image = R.mipmap.s_local_senior;
    private ArrayList<String> stringList;
    private MenuListviewAdapter ListviewInfoAdapter;
    private String strTitle;
    private int positionId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_senior_params);
        isShowPerScreen(true);
        initViews();
    }

    private void initViews() {
        ivCheckMenu = ((ImageView) findViewById(R.id.iv_check_menu));
        textTitle = ((TextView) findViewById(R.id.text_title));
        tvCheckedNum = ((TextView) findViewById(R.id.tv_checked_num));
        ListviewInfo = ((ListView) findViewById(R.id.about_pos_listview));
        textTitle.setText("高级参数");
        ivCheckMenu.setImageResource(image);
        strTitle = textTitle.getText().toString();
        stringList = new ArrayList<>();

        stringList.add("恢复出厂设置");
        stringList.add("初始化设备");
        stringList.add("重启设备");
        stringList.add("重新上传记录");
        stringList.add("U盘功能");
        stringList.add("测试模式");
        stringList.add("进入Android桌面");
        stringList.add("人脸活体设置");
        stringList.add("人脸启用设置");
        stringList.add("人脸参数设置");

        ListviewInfoAdapter = new MenuListviewAdapter(this, stringList);
        ListviewInfo.setAdapter(ListviewInfoAdapter);
        ListviewInfo.setOnItemSelectedListener(this);
        ListviewInfo.setOnItemClickListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        tvCheckedNum.setText(position + 1 + "/" + stringList.size());
        ListviewInfoAdapter.setCurrentItem(position);
        ListviewInfoAdapter.notifyDataSetChanged();
        positionId = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    //恢复出厂设置
    private void RestoreFactorySet(int iPosition) {

        int iType = iPosition;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "恢复出厂设置";

        //是否恢复出厂设置？
        OptionItemList.add("是否恢复出厂设置？");
        startActivityForResult(new Intent(this, ConfirmActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iPosition)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
    }

    //初始化设备
    private void InitDevice(int iPosition) {

        int iType = iPosition;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "初始化设备";

        //是否恢复出厂设置？
        OptionItemList.add("是否初始化设备？");
        startActivityForResult(new Intent(this, ConfirmActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iPosition)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
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

    //重新上传记录
    private void ReUpRecord(int iPosition) {

        int iType = iPosition;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "重新上传记录";

        //是否恢复出厂设置？
        OptionItemList.add("是否重新上传记录？");
        startActivityForResult(new Intent(this, ConfirmActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iPosition)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
    }

    //U盘功能
    private void UDiskFun(int iPosition) {
        int iType = 24;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "U盘功能";

        OptionItemList.add("人脸特征码导入");
        OptionItemList.add("应用程序升级");
        OptionItemList.add("引导程序升级");
        OptionItemList.add("POS机数据导出");

        iSelectItem = 0;
        startActivity(new Intent(this, SetUdiskActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iSelectItem)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image));
    }


    //进入测试模式
    private void EnterTestMode(int iPosition) {

        int iType = iPosition;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "测试模式";

        //是否恢复出厂设置？
        if (g_WorkInfo.cTestState == 0)
            OptionItemList.add("是否进入测试模式？");
        else
            OptionItemList.add("是否退出测试模式？");
        startActivityForResult(new Intent(this, ConfirmActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iPosition)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
    }

    //进入android桌面
    private void EnterLAUNCHER3(int iPosition) {

        int iType = iPosition;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "进入android桌面";

        OptionItemList.add("是否进入桌面模式？");
        startActivityForResult(new Intent(this, ConfirmActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iPosition)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
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
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        int KeyValue;
        KeyValue = event.getKeyCode();
        //Log.d(TAG, "dispatchKeyEvent KeyValue:" + KeyValue);
        //Log.d("按键",event.getAction()+"");

        if ((KeyValue == KeyEvent.KEYCODE_ENTER) && (event.getAction() == 0)) {
            switch (positionId) {
                case 0:  //恢复出厂设置
                    RestoreFactorySet(positionId);
                    break;
                case 1:  //初始化设备
                    InitDevice(positionId);
                    break;
                case 2:  //重启设备
                    ResetDevice(positionId);
                    break;
                case 3:  //重新上传记录
                    ReUpRecord(positionId);
                    break;
                case 4:  //U盘功能
                    UDiskFun(positionId);
                    break;
                case 5:  //测试模式
                    EnterTestMode(positionId);
                    break;
                case 6:  //进入Android桌面
                    EnterLAUNCHER3(positionId);
                    break;
                case 7:  //人脸活体设置
                    SetFaceLive(positionId);
                    break;
                case 8:  //人脸识别启用设置
                    SetFaceEnable(positionId);
                    break;
                case 9:  //设置人脸参数
                    SetFaceInfo(positionId);
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
        //LogUtil.e(TAG, "KeyValue:" + KeyValue);
        if (KeyValue == KeyEvent.KEYCODE_PERIOD ||
                KeyValue == KeyEvent.KEYCODE_NUMPAD_MULTIPLY ||
                KeyValue == KeyEvent.KEYCODE_NUMPAD_ADD ||
                KeyValue == KeyEvent.KEYCODE_0) {
            return true;
        }
        switch (KeyValue) {
            case KeyEvent.KEYCODE_1:  //恢复出厂设置
                RestoreFactorySet(KeyValue - KeyEvent.KEYCODE_1);
                break;
            case KeyEvent.KEYCODE_2:  //初始化设备
                InitDevice(KeyValue - KeyEvent.KEYCODE_1);
                break;
            case KeyEvent.KEYCODE_3:  //重启设备
                ResetDevice(KeyValue - KeyEvent.KEYCODE_1);
                break;
            case KeyEvent.KEYCODE_4:  //重新上传记录
                ReUpRecord(KeyValue - KeyEvent.KEYCODE_1);
                break;
            case KeyEvent.KEYCODE_5:  //导入人脸特征码
                UDiskFun(KeyValue - KeyEvent.KEYCODE_1);
                break;
            case KeyEvent.KEYCODE_6:  //测试模式
                EnterTestMode(KeyValue - KeyEvent.KEYCODE_1);
                break;
            case KeyEvent.KEYCODE_7:  //进入android桌面
                EnterLAUNCHER3(KeyValue - KeyEvent.KEYCODE_1);
                break;
            case KeyEvent.KEYCODE_8:  //人脸活体设置
                SetFaceLive(KeyValue - KeyEvent.KEYCODE_1);
                break;
            case KeyEvent.KEYCODE_9:  //人脸启用设置
                SetFaceEnable(KeyValue - KeyEvent.KEYCODE_1);
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
            case 0:  //恢复出厂设置
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                if (iRet == 1) {
                    ToastUtils.showText(this, "恢复出厂设置", WARN, CENTER, Toast.LENGTH_LONG);
                    Publicfun.FactoryReset();
                }
                break;
            case 1:  //初始化设备
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                if (iRet == 1) {
                    Log.i(TAG, "设备初始化");
                    ToastUtils.showText(this, "设备初始化", WARN, CENTER, Toast.LENGTH_LONG);
                    Publicfun.InitDevice();
                }
                break;
            case 2:  //重启设备
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                if (iRet == 1) {
                    Log.i(TAG, "重启设备");
                    ToastUtils.showText(this, "重启设备", WARN, CENTER, Toast.LENGTH_LONG);
                    Publicfun.RunShellCmd("reboot");
                }
                break;
            case 3:  //重新上传记录
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                if (iRet == 1) {
                    ToastUtils.showText(this, "重新上传交易记录", WARN, CENTER, Toast.LENGTH_LONG);
                    Log.i(TAG, "重置已上传流水记录号:" + g_WasteBookInfo.TransferIndex);
                    long TransferIndex = (g_WasteBookInfo.TransferIndex % MAXBOOKSCOUNT);
                    g_WasteBookInfo.TransferIndex = g_WasteBookInfo.TransferIndex - TransferIndex;

                    Log.i(TAG, "重置已上传流水记录号:" + g_WasteBookInfo.TransferIndex);
                }
                break;
            case 4:  //导入人脸特征
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                if (iRet == 0) {
                } else if (iRet == 1) {
                    //U盘导入
                }
                break;

            case 5:  //进入测试模式
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                if (iRet == 1) {
                    if (g_WorkInfo.cTestState == 0) {
                        Log.i(TAG, "进入测试模式");
                        ToastUtils.showText(this, "进入测试模式", WARN, CENTER, Toast.LENGTH_LONG);
                        g_WorkInfo.cTestState = 1;
                        g_LocalInfo.cInputMode = 3;
                        g_LocalInfo.cBookSureMode = 0;
                    } else {
                        Log.i(TAG, "退出测试模式");
                        ToastUtils.showText(this, "退出测试模式", WARN, CENTER, Toast.LENGTH_LONG);
                        g_WorkInfo.cTestState = 0;
                        g_LocalInfo = LocalInfoRW.ReadLocalInfo();
                    }
                }
                break;

            case 6:  //进入android桌面
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                if (iRet == 1) {
                    //am start -n com.android.launcher3/com.android.launcher3.Launcher
                    Log.i(TAG, "进入android桌面");
                    ToastUtils.showText(this, "进入android桌面", WARN, CENTER, Toast.LENGTH_LONG);
                    GotoLauncher3();
                }
                break;

            case 7:  //人脸活体设置
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                g_LocalInfo.cFaceLiveFlag = (short) iRet;
                LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
                break;

            case 8:  //是否支持人脸识别
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                g_SystemInfo.cFaceDetectFlag = (byte) iRet;
                SystemInfoRW.WriteAllSystemInfo(g_SystemInfo);
                break;

            case 9:  //设置人脸参数
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                ArrayList<String> strList = new ArrayList<String>(); //选项内容
                strList = data.getStringArrayListExtra("strList");

                String strtemp = strList.get(0);
                try {
                    g_LocalInfo.iPupilDistance = Integer.parseInt(strtemp);// 瞳距
                    g_FaceIdentInfo.iPupilDistance = g_LocalInfo.iPupilDistance;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                String strtemp1 = String.format("0.%s", strList.get(1));
                g_LocalInfo.fLiveThrehold = Float.parseFloat(strtemp1);// 人脸活体检测率
                g_FaceIdentInfo.fLiveThrehold = g_LocalInfo.fLiveThrehold;

                String strtemp2 = String.format("0.%s", strList.get(2));
                g_LocalInfo.fFraction = Float.parseFloat(strtemp2);// 人脸相识率
                g_FaceIdentInfo.fFraction = g_LocalInfo.fFraction;

                LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
                break;
        }
    }

}
