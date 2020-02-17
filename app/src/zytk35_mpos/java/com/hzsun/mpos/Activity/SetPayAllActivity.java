package com.hzsun.mpos.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.hzsun.mpos.data.RecordInfoRW;
import com.hzsun.mpos.thread.LoadFeatureThread;

import java.util.ArrayList;

import static com.hzsun.mpos.Global.Global.CAMERA_NUM;
import static com.hzsun.mpos.Global.Global.DATAPath;
import static com.hzsun.mpos.Global.Global.ZYTKFacePath;
import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Global.Global.g_OddKeyInfo;
import static com.hzsun.mpos.Global.Global.g_RecordInfo;
import static com.hzsun.mpos.Global.Global.g_StationInfo;
import static com.hzsun.mpos.Global.Global.g_SystemInfo;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;
import static com.hzsun.mpos.Public.Publicfun.RelayControlDeal;
import static com.hzsun.mpos.Public.Publicfun.RunShellCmd;
import static com.hzsun.mpos.Public.ToastUtils.BOTTOM;
import static com.hzsun.mpos.Public.ToastUtils.CENTER;
import static com.hzsun.mpos.Public.ToastUtils.WARN;

public class SetPayAllActivity extends BaseActivity implements
        AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    private String TAG = getClass().getSimpleName();
    private ImageView localBusinessSetImage;
    private TextView titleTv;
    private TextView tvCheckedNum;
    private ListView setPayAllListview;
    private ArrayList<String> stringList;
    private MenuListviewAdapter menuListviewAdapter;
    public Handler UISetPayHandler;
    private int image = R.mipmap.s_local_set;
    private String title = null;
    private int positionId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pay_all);
        isShowPerScreen(true);
        initViews();
        initListView();

        UISetPayHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        g_WorkInfo.cFaceInitState = 2;
                        ToastUtils.showText(SetPayAllActivity.this, "加载人脸特征数据完成！", WARN, BOTTOM, Toast.LENGTH_LONG);
                        break;
                    case 100:
                        ToastUtils.showText(SetPayAllActivity.this, "加载人脸特征数据失败！", WARN, BOTTOM, Toast.LENGTH_LONG);
                        break;
                }
            }
        };
    }

    private void initViews() {
        localBusinessSetImage = ((ImageView) findViewById(R.id.local_business_set_image));
        titleTv = ((TextView) findViewById(R.id.title_tv));
        tvCheckedNum = ((TextView) findViewById(R.id.tv_checked_num));
        setPayAllListview = ((ListView) findViewById(R.id.set_pay_all_listview));
        localBusinessSetImage.setImageResource(image);
        title = getResources().getString(R.string.set);
        titleTv.setText(title);
    }

    private void initListView() {
        stringList = new ArrayList<>();
        stringList.add(getResources().getString(R.string.local_Twomenu_set_1));
        stringList.add(getResources().getString(R.string.local_Twomenu_set_2));
        stringList.add(getResources().getString(R.string.local_Twomenu_set_3));
        stringList.add(getResources().getString(R.string.local_Twomenu_set_4));
        stringList.add(getResources().getString(R.string.local_Twomenu_set_5));
        stringList.add(getResources().getString(R.string.local_Twomenu_set_6));
        stringList.add(getResources().getString(R.string.local_Twomenu_set_7));
        stringList.add(getResources().getString(R.string.local_Twomenu_set_8));
        stringList.add(getResources().getString(R.string.local_Twomenu_set_9));
        stringList.add(getResources().getString(R.string.local_Twomenu_set_10));
        stringList.add(getResources().getString(R.string.local_Twomenu_set_11));
        stringList.add(getResources().getString(R.string.local_Twomenu_set_12));
        stringList.add(getResources().getString(R.string.local_Twomenu_set_13));
        stringList.add(getResources().getString(R.string.local_Twomenu_set_14));
        stringList.add(getResources().getString(R.string.local_Twomenu_set_15));
        stringList.add(getResources().getString(R.string.local_Twomenu_set_16));
        stringList.add(getResources().getString(R.string.local_Twomenu_set_17));
        menuListviewAdapter = new MenuListviewAdapter(this, stringList);
        setPayAllListview.setAdapter(menuListviewAdapter);
        setPayAllListview.setOnItemSelectedListener(this);
        setPayAllListview.setOnItemClickListener(this);
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

    //1.设置交易模式
    private void SetTradingMode(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "交易模式";

        //"1普通,2单键,3定额"
        OptionItemList.add("普通");
        OptionItemList.add("单键");
        OptionItemList.add("定额");
        iSelectItem = g_LocalInfo.cInputMode - 1;

        startActivityForResult(new Intent(this, SetMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iSelectItem)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
    }

    //显示单键值
    private void ShowOddKeyValue() {
        int i;
        int cResult;
        int iType = 100;
        int image = R.mipmap.s_local_check;
        ArrayList<String> OptionItemList = new ArrayList<String>();
        String strTemp = "";
        String strTitle = "单键金额值";

        //单键金额值
        for (i = 0; i < 5; i++) {
            strTemp = String.format(" 键%d-->%d.%02d    |    键%d-->%d.%02d", i * 2, g_OddKeyInfo.wKeyMoney[i * 2] / 100, g_OddKeyInfo.wKeyMoney[i * 2] % 100
                    , i * 2 + 1, g_OddKeyInfo.wKeyMoney[i * 2 + 1] / 100, g_OddKeyInfo.wKeyMoney[i * 2 + 1] % 100);
            OptionItemList.add(strTemp);
        }

        startActivity(new Intent(this, CheckMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", 0)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image));
        return;
    }

    //设置定额值
    private void SetBookMoney() {
        int iType = 50;
        int image = R.mipmap.s_local_senior;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "设置定额值(单位:分)";
        //设定前定额值
        for (int i = 0; i < 6; i++) {
            //String strTemp=String.format("%d.%02d",(g_LocalInfo.wBookMoney[i]/100),(g_LocalInfo.wBookMoney[i]%100));
            String strTemp = String.format("%d", g_LocalInfo.wBookMoney[i]);
            OptionItemList.add(strTemp);
        }
        startActivityForResult(new Intent(this, SetBookMoney.class)

                .putExtra("Type", iType)
                .putExtra("Position", 0)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
        return;
    }

    //2.设置定额结账模式
    private void SetBookSureMode(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "定额结账模式";

        //"1自动确认,2手动确认"
        OptionItemList.add("自动确认");
        OptionItemList.add("手动确认");

        iSelectItem = g_LocalInfo.cBookSureMode;//定额方式是否需要确认 0:不需要 1:需要

        startActivityForResult(new Intent(this, SetMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iSelectItem)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
    }

    //3 设置键盘锁定(输入密码)
    private void SetKeyboardLock_Pwd(int iPosition) {
        GotoPassWordActivity(iPosition);//进入密码输入界面
    }

    //3 设置键盘锁定
    private void SetKeyboardLock(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "键盘锁定";

        //"1自动确认,2手动确认"
        OptionItemList.add("不锁定");
        OptionItemList.add("锁定");

        iSelectItem = g_LocalInfo.cKeyLockState;//键盘锁定  0:不锁定 1:锁定

        startActivityForResult(new Intent(this, SetMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iSelectItem)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
    }

    //4.日餐累清零
    private void ClearDayAmountTotal(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "日餐累计清零";

        //是否允许查汇总
        if (g_StationInfo.cCanPermitStat == 1) {
            //是否清零当日累计？
            OptionItemList.add("是否清零当日当餐累计？");
            startActivityForResult(new Intent(this, ConfirmActivity.class)

                    .putExtra("Type", iType)
                    .putExtra("Position", iPosition)
                    .putExtra("strTitle", strTitle)
                    .putStringArrayListExtra("OptionItemList", OptionItemList)
                    .putExtra("Image", image), iType);
        } else {
            //不允许操作此类
            OptionItemList.add("不允许操作此类");
            startActivity(new Intent(this, ConfirmActivity.class)

                    .putExtra("Type", iType)
                    .putExtra("Position", iPosition)
                    .putExtra("strTitle", strTitle)
                    .putStringArrayListExtra("OptionItemList", OptionItemList)
                    .putExtra("Image", image));
        }
    }

    //5.设置小票打印模式
    private void SetPrinterMode(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "设置小票打印";

        //设置小票打印  0:不打印 1:直接打印
        OptionItemList.add("不打印");
        OptionItemList.add("直接打印");

        iSelectItem = g_LocalInfo.cPrinterMode;//设置小票打印  0:不打印 1:直接打印

        startActivityForResult(new Intent(this, SetMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iSelectItem)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
    }

    //6.上传记录笔数
    private void SetUpRecordCount(int iPosition) {
        int iType = iPosition;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "上传交易记录笔数";
        iPosition = g_LocalInfo.cUpPamentSum;
        OptionItemList.add("上传记录笔数(1-10)");

        startActivityForResult(new Intent(SetPayAllActivity.this, EditTextActivity.class)
                .putExtra("Type", iType)
                .putExtra("Position", iPosition)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);

        return;
    }

    //设置音量大小
    private void SetVolumeLevel(int iPosition) {
        int cResult;
        int iType = iPosition;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();
        String strTemp = "";
        String strTitle = "音量设置";

        OptionItemList.add("" + g_LocalInfo.cVolumeLevel);

        startActivityForResult(new Intent(SetPayAllActivity.this, ProgressbarActivity.class)
                .putExtra("Type", iType)
                .putExtra("Position", iPosition)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
    }

    //7.设置播放具体金额语音
    private void SetVoiceMoneyMode(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "设置播放金额语音";

        //是否播放具体金额 0: 不启用 1:启用
        OptionItemList.add("普通模式");
        OptionItemList.add("数字金额模式");

        iSelectItem = g_LocalInfo.cPlayVoiceMoneyFlag;

        startActivityForResult(new Intent(this, SetMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iSelectItem)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
    }

    //8.设置人脸识别模式
    private void SetFaceRecognitionMode(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "设置人脸识别模式";

        // 0:手动模式  1:自动模式
        OptionItemList.add("手动模式");
        OptionItemList.add("自动模式");

        iSelectItem = g_LocalInfo.cFaceModeFlag;// 0:手动模式  1:自动模式

        startActivityForResult(new Intent(this, SetMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iSelectItem)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
    }

    //9 WIFI选项
    private void WIFIOptions(int position) {
        startActivity(new Intent(SetPayAllActivity.this, WifiActivity.class)
                .putExtra("TwoMenuTitle", title)
                .putExtra("SetMenuTitle", stringList.get(position))
                .putExtra("Image", image)
                .putExtra("ThreeType", position));
    }

    //10 设置人脸特征
    private void SetFaceCode(int iPosition) {

        int iType = iPosition;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "设置人脸特征";
        OptionItemList.add("重新加载人脸特征码");
        OptionItemList.add("重新下载人脸特征码");

        if (g_SystemInfo.cFaceDetectFlag != 1) {
            ToastUtils.showText(this, "此设备不支持人脸识别！", WARN, BOTTOM, Toast.LENGTH_LONG);
            return;
        }
        iSelectItem = 0;
        startActivityForResult(new Intent(this, SetMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iSelectItem)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
    }

    //11 显示姓名设置
    private void SetCardName(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
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
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);

    }


    //12 设置是否代扣
    private void WithholdSet(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "设置是否启用代扣";

        // 0:手动模式  1:自动模式
        OptionItemList.add("不启用");
        OptionItemList.add("启用");

        iSelectItem = g_LocalInfo.iWithholdState;// 0:不启用  1:启用

        startActivityForResult(new Intent(this, SetMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iSelectItem)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
    }

    //13 设置是否显示商户二维码
    private void SetShoperQRShow(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "设置启用商户二维码";

        // 0:不启用  1:启用
        OptionItemList.add("不启用");
        OptionItemList.add("启用");

        iSelectItem = g_LocalInfo.iBusinessQRState;// 0:不启用  1:启用

        startActivityForResult(new Intent(this, SetMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iSelectItem)
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

        startActivityForResult(new Intent(SetPayAllActivity.this, EditTextActivity.class)
                .putExtra("Type", iType)
                .putExtra("Position", iPosition)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);

        return;
    }

    //设置显示红外
    private void SetDisplayInfrared(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        if (CAMERA_NUM != 2) {
            ToastUtils.showText(this, "此设备不支持红外双目！", WARN, BOTTOM, Toast.LENGTH_LONG);
            return;
        }
        String strTitle = "设置显示红外";

        // 0:不显示  1:显示
        OptionItemList.add("不显示");
        OptionItemList.add("显示");

        iSelectItem = g_LocalInfo.iDisplayInfr;// 0:不显示  1:显示

        startActivityForResult(new Intent(this, SetMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iSelectItem)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
    }

    //设置省电时长
    private void SetPowerSaveTime(int iPosition) {
        int iType = iPosition;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "设置省电时长";
        iPosition = g_LocalInfo.cPowerSaveTimeA;
        OptionItemList.add("省电时长(1-30)分钟");

        startActivityForResult(new Intent(SetPayAllActivity.this, EditTextActivity.class)
                .putExtra("Type", iType)
                .putExtra("Position", iPosition)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);

        return;
    }

    //设置继电器
    private void SetRelayStatus(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "设置继电器";

        //继电器设置   0:关闭 1:开启
        OptionItemList.add("关闭");
        OptionItemList.add("开启");

        iSelectItem = g_LocalInfo.iRelayState;//继电器设置   0:打开活体 1:关闭活体

        startActivityForResult(new Intent(this, SetMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iSelectItem)
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

    //进入输入密码界面 iType:
    private void GotoPassWordActivity(int iPosition) {
        int i;
        int iType = 100;
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
                .putExtra("Position", iPosition)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
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
        if ((KeyValue == KeyEvent.KEYCODE_ENTER) && (event.getAction() == 0)) {
            switch (positionId) {
                case 0:  //交易模式
                    SetTradingMode(positionId);
                    break;
                case 1:  //定额结账模式
                    SetBookSureMode(positionId);
                    break;
                case 2:  //数字键盘锁定
                    SetKeyboardLock_Pwd(positionId);
                    break;
                case 3:  //日累清零
                    ClearDayAmountTotal(positionId);
                    break;
                case 4:  //打印小票
                    SetPrinterMode(positionId);
                    break;
                case 5:  //音量设置
                    SetVolumeLevel(positionId);
                    break;
                case 6:  //设置播放金额语音
                    SetVoiceMoneyMode(positionId);
                    break;
                case 7:  //人脸识别模式
                    SetFaceRecognitionMode(positionId);
                    break;
                case 8:  //WIFI选项
                    WIFIOptions(positionId);
                    break;
                case 9:  //重新加载人脸特征
                    SetFaceCode(positionId);
                    break;
                case 10:  //显示姓名设置
                    SetCardName(positionId);
                    break;
                case 11:  //设置是否代扣
                    WithholdSet(positionId);
                    break;
                case 12:  //设置是否显示商户二维码
                    SetShoperQRShow(positionId);
                    break;
                case 13:    //设置支付成功显示时长
                    SetPayShowTime(positionId);
                    break;
                case 14:    //设置显示红外
                    SetDisplayInfrared(positionId);
                    break;
                case 15:    //设置省电时长
                    SetPowerSaveTime(positionId);
                    break;
                case 16:  //设置继电器
                    SetRelayStatus(positionId);
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
        //Log.e(TAG, "KeyValue:" + KeyValue);
        if (KeyValue == KeyEvent.KEYCODE_PERIOD ||
                KeyValue == KeyEvent.KEYCODE_NUMPAD_MULTIPLY ||
                KeyValue == KeyEvent.KEYCODE_NUMPAD_ADD) {
            return true;
        }

        switch (KeyValue) {
            case KeyEvent.KEYCODE_1:  //交易模式
                SetTradingMode(KeyValue - KeyEvent.KEYCODE_1);
                break;
            case KeyEvent.KEYCODE_2:  //定额结账模式
                SetBookSureMode(KeyValue - KeyEvent.KEYCODE_1);
                break;
            case KeyEvent.KEYCODE_3:  //数字键盘锁定
                SetKeyboardLock_Pwd(KeyValue - KeyEvent.KEYCODE_1);
                break;
            case KeyEvent.KEYCODE_4:  //日累清零
                ClearDayAmountTotal(KeyValue - KeyEvent.KEYCODE_1);
                break;
            case KeyEvent.KEYCODE_5:  //打印小票
                SetPrinterMode(KeyValue - KeyEvent.KEYCODE_1);
                break;
//            case KeyEvent.KEYCODE_6:  //上传记录笔数
//                SetUpRecordCount(KeyValue - KeyEvent.KEYCODE_1);
//                break;
            case KeyEvent.KEYCODE_6:  //设置音量大小
                SetVolumeLevel(KeyValue - KeyEvent.KEYCODE_1);
                break;
            case KeyEvent.KEYCODE_7:  //设置播放金额语音
                SetVoiceMoneyMode(KeyValue - KeyEvent.KEYCODE_1);
                break;
            case KeyEvent.KEYCODE_8:  //人脸识别模式
                SetFaceRecognitionMode(KeyValue - KeyEvent.KEYCODE_1);
                break;
            case KeyEvent.KEYCODE_9:  //WIFI选项
                WIFIOptions(KeyValue - KeyEvent.KEYCODE_1);
                break;
            case KeyEvent.KEYCODE_0:  //重新加载人脸特征
                SetFaceCode(KeyValue - KeyEvent.KEYCODE_1);
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
            case 0:  //交易模式
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                g_LocalInfo.cInputMode = (short) (iRet + 1);
                LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
                if (iRet == 1) {
                    ShowOddKeyValue();//单键
                } else if (iRet == 2) {
                    SetBookMoney();//定额
                }
                break;

            case 1:  //定额结账模式
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                g_LocalInfo.cBookSureMode = (short) iRet;
                LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
                break;

            case 2:  //数字键盘锁定
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                g_LocalInfo.cKeyLockState = (short) iRet;
                LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
                break;

            case 3:  //日累清零
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                Log.i(TAG, "日餐累计清零");
                //记录当日交易笔数、金额
                g_RecordInfo.wTodayPaymentSum = 0;
                g_RecordInfo.lngTodayPaymentMoney = 0;
                //记录当餐交易笔数、金额
                g_RecordInfo.wTotalBusinessSum = 0;
                g_RecordInfo.lngTotalBusinessMoney = 0;
                //记录交易完成后日累，餐累，末笔营业，累计总金额参数
                RecordInfoRW.WriteAllRecordInfo(g_RecordInfo);
                break;

            case 4:  //打印小票
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                g_LocalInfo.cPrinterMode = (short) iRet;
                LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
                break;

            case 5:  //音量设置
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                g_LocalInfo.cVolumeLevel = (short) iRet;
                LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
                break;

            case 6:  //设置播放金额语音
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                g_LocalInfo.cPlayVoiceMoneyFlag = (short) iRet;
                LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
                break;

            case 7:  //人脸识别模式
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                g_LocalInfo.cFaceModeFlag = (short) iRet;
                LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
                break;

            case 8:  //WIFI选项
                break;

            case 9:  //设置人脸特征
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                if (iRet == 0) {
                    //是否重新加载人脸特征
                    if ((g_WorkInfo.cFaceInitState == 1) || (g_WorkInfo.cFaceInitState == 2)) {
                        LoadFeatureThread ThreadLoadFeature = new LoadFeatureThread(UISetPayHandler);
                        ThreadLoadFeature.start();
                    }
                } else {
                    //重新下载人脸特征码
                    ToastUtils.showText(SetPayAllActivity.this, "重新下载人脸特征码！", WARN, BOTTOM, Toast.LENGTH_LONG);

                    RunShellCmd("rm -r " + ZYTKFacePath + "*");
                    RunShellCmd("rm -r " + DATAPath + "FaceCodeInfo.dat");
                    ResetDevice(300);
                }
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

            case 12:  //是否启用商户二维码
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                g_LocalInfo.iBusinessQRState = (short) iRet;
                LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
                break;

            case 13:  //支付成功显示时长
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                if ((iRet > 10) || (iRet == 0))
                    iRet = 5;
                g_LocalInfo.iPayShowTime = (short) iRet;
                LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
                break;

            case 14:  //是否显示红外
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                g_LocalInfo.iDisplayInfr = (short) iRet;
                LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
                ResetDevice(300);
                break;

            case 15:  //省电时长
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                if (iRet > 30)
                    iRet = 30;
                if (iRet < 1)
                    iRet = 1;
                g_LocalInfo.cPowerSaveTimeA = (short) iRet;
                LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
                break;

            case 16:    //设置继电器
                iRet = data.getExtras().getInt("result");
                g_LocalInfo.iRelayState = iRet;
                LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
                if (iRet == 1) {
                    startActivity(new Intent(this, RelayActivity.class));
                }
                break;

            case 50:  //定额值
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                ArrayList<String> BookMoneyList = new ArrayList<String>(); //选项内容
                BookMoneyList = data.getStringArrayListExtra("BookMoneyList");
                for (int i = 0; i < BookMoneyList.size(); i++) {
                    String strtemp = BookMoneyList.get(i);
                    try {
                        g_LocalInfo.wBookMoney[i] = Integer.parseInt(strtemp);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
                Publicfun.CompareBusinessDate(g_WorkInfo.cCurDateTime);
                break;
				
			case 100: //数字键盘锁定密码
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                int iPosition = data.getExtras().getInt("Position");
                Log.i(TAG, "返回的结果:" + iRet+" iPosition:"+iPosition);
                if (iRet == 0) {
                    if(iPosition == 2)
                        SetKeyboardLock(iPosition);
                } else if (iRet == 1){
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
